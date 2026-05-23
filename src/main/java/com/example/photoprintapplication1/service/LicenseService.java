package com.example.photoprintapplication1.service;

import com.example.photoprintapplication1.dto.ActivateLicenseRequest;
import com.example.photoprintapplication1.dto.CheckLicenseRequest;
import com.example.photoprintapplication1.dto.LicenseCreateRequest;
import com.example.photoprintapplication1.dto.RenewLicenseRequest;
import com.example.photoprintapplication1.dto.Ticket;
import com.example.photoprintapplication1.dto.TicketResponse;
import com.example.photoprintapplication1.models.Device;
import com.example.photoprintapplication1.models.DeviceLicense;
import com.example.photoprintapplication1.models.License;
import com.example.photoprintapplication1.models.LicenseHistory;
import com.example.photoprintapplication1.models.LicenseType;
import com.example.photoprintapplication1.models.Product;
import com.example.photoprintapplication1.models.Role;
import com.example.photoprintapplication1.models.User;
import com.example.photoprintapplication1.repository.DeviceLicenseRepository;
import com.example.photoprintapplication1.repository.DeviceRepository;
import com.example.photoprintapplication1.repository.LicenseHistoryRepository;
import com.example.photoprintapplication1.repository.LicenseRepository;
import com.example.photoprintapplication1.repository.LicenseTypeRepository;
import com.example.photoprintapplication1.repository.ProductRepository;
import com.example.photoprintapplication1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseService {

    private static final int DEFAULT_DEVICE_LIMIT = 5;

    private final LicenseRepository licenseRepository;
    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final UserRepository userRepository;
    private final LicenseHistoryRepository historyRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final SignatureService signatureService;

    public LicenseService(
            LicenseRepository licenseRepository,
            ProductRepository productRepository,
            LicenseTypeRepository licenseTypeRepository,
            UserRepository userRepository,
            LicenseHistoryRepository historyRepository,
            DeviceRepository deviceRepository,
            DeviceLicenseRepository deviceLicenseRepository,
            SignatureService signatureService) {
        this.licenseRepository = licenseRepository;
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.signatureService = signatureService;
    }

    @Transactional
    public License createLicense(LicenseCreateRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("License type not found"));

        User owner = admin;
        if (request.getOwnerId() != null) {
            owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        }

        int deviceLimit = request.getDeviceCount() != null ? request.getDeviceCount() : DEFAULT_DEVICE_LIMIT;

        License license = new License();
        license.setCode(generateUniqueCode());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setUser(null);
        license.setFirstActivationDate(null);
        license.setEndingDate(null);
        license.setBlocked(false);
        license.setDeviceCount(deviceLimit);
        license.setDescription(request.getDescription());

        license = licenseRepository.save(license);

        saveHistory(license, admin, "CREATED", "Лицензия создана администратором");
        return license;
    }

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new IllegalArgumentException("License not found"));

        if (license.isBlocked()) {
            throw new IllegalStateException("License is blocked");
        }

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new IllegalStateException("License already activated by another user");
        }

        if (license.getUser() == null
                && !license.getOwner().getId().equals(userId)
                && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only owner or admin can activate this license");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setName(request.getDeviceName() != null ? request.getDeviceName() : request.getDeviceMac());
                    d.setMacAddress(request.getDeviceMac());
                    d.setUser(user);
                    return deviceRepository.save(d);
                });

        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Device belongs to another user");
        }

        if (deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId())) {
            return signedTicket(license, device);
        }

        long currentCount = deviceLicenseRepository.countByLicenseId(license.getId());
        int limit = license.getDeviceCount() > 0 ? license.getDeviceCount() : DEFAULT_DEVICE_LIMIT;
        if (currentCount >= limit) {
            throw new IllegalStateException("Device limit reached");
        }

        if (license.getUser() == null) {
            license.setUser(user);
            license.setFirstActivationDate(LocalDateTime.now());
            license.setEndingDate(LocalDateTime.now().plusDays(license.getType().getDefaultDurationInDays()));
            licenseRepository.save(license);
            saveHistory(license, user, "ACTIVATED", "Первая активация лицензии");
        }

        DeviceLicense deviceLicense = new DeviceLicense();
        deviceLicense.setLicense(license);
        deviceLicense.setDevice(device);
        deviceLicense.setActivationDate(LocalDateTime.now());
        deviceLicenseRepository.save(deviceLicense);

        saveHistory(license, user, "ACTIVATED", "Активация на устройстве " + device.getMacAddress());
        return signedTicket(license, device);
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(CheckLicenseRequest request) throws Exception {
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        License license = licenseRepository.findFirstByDeviceLicensesDeviceIdOrderByEndingDateDesc(device.getId())
                .orElseThrow(() -> new IllegalArgumentException("No license found for device"));

        validateActiveLicense(license, device);
        return signedTicket(license, device);
    }

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        License license = licenseRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("License not found"));

        if (license.isBlocked()) {
            throw new IllegalStateException("License is blocked");
        }

        if (license.getUser() != null
                && !license.getUser().getId().equals(userId)
                && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not allowed to renew this license");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean inactive = license.getUser() == null || license.getEndingDate() == null;
        boolean expiringSoon = license.getEndingDate() != null
                && !license.getEndingDate().isAfter(now.plusDays(7));

        if (!inactive && !expiringSoon) {
            throw new IllegalStateException("Renewal not allowed (more than 7 days left)");
        }

        LocalDateTime base = (license.getEndingDate() != null && license.getEndingDate().isAfter(now))
                ? license.getEndingDate()
                : now;
        long daysToAdd = license.getType().getDefaultDurationInDays();
        license.setEndingDate(base.plusDays(daysToAdd));

        if (license.getUser() == null) {
            license.setUser(user);
            license.setFirstActivationDate(now);
        }

        licenseRepository.save(license);
        saveHistory(license, user, "RENEWED", "Лицензия продлена на " + daysToAdd + " дней");

        return signedTicket(license, null);
    }

    private void validateActiveLicense(License license, Device device) {
        if (license.isBlocked()) {
            throw new IllegalStateException("License is blocked");
        }
        if (license.getEndingDate() == null || license.getEndingDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("License has expired");
        }
        if (license.getUser() == null) {
            throw new IllegalStateException("License is not activated");
        }
        if (!license.getUser().getId().equals(device.getUser().getId())) {
            throw new IllegalStateException("License not bound to this user");
        }
        if (!deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId())) {
            throw new IllegalStateException("License is not activated on this device");
        }
    }

    private TicketResponse signedTicket(License license, Device device) throws Exception {
        Ticket ticket = buildTicket(license, device);
        String signature = signatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    private Ticket buildTicket(License license, Device device) {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.now());
        ticket.setTicketLifetimeSeconds(SignatureService.defaultTicketLifetimeSeconds());
        ticket.setFirstActivationDate(toLocalDate(license.getFirstActivationDate()));
        ticket.setExpirationDate(toLocalDate(license.getEndingDate()));
        ticket.setUserId(license.getUser() != null ? license.getUser().getId() : null);
        ticket.setDeviceId(device != null ? device.getId() : null);
        ticket.setBlocked(license.isBlocked());
        return ticket;
    }

    private LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    private void saveHistory(License license, User user, String status, String description) {
        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(status);
        history.setDescription(description);
        historyRepository.save(history);
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
