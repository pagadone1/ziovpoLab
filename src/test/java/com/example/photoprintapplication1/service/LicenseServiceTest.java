package com.example.photoprintapplication1.service;

import com.example.photoprintapplication1.dto.ActivateLicenseRequest;
import com.example.photoprintapplication1.dto.CheckLicenseRequest;
import com.example.photoprintapplication1.dto.LicenseCreateRequest;
import com.example.photoprintapplication1.dto.RenewLicenseRequest;
import com.example.photoprintapplication1.dto.TicketResponse;
import com.example.photoprintapplication1.models.Device;
import com.example.photoprintapplication1.models.License;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock private LicenseRepository licenseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private LicenseTypeRepository licenseTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private LicenseHistoryRepository historyRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private DeviceLicenseRepository deviceLicenseRepository;
    @Mock private SignatureService signatureService;

    @InjectMocks
    private LicenseService licenseService;

    private User admin;
    private User owner;
    private Product product;
    private LicenseType type;
    private License license;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        owner = new User();
        owner.setId(2L);
        owner.setRole(Role.USER);

        product = new Product();
        product.setId(10L);
        product.setName("Car Service Desktop");

        type = new LicenseType();
        type.setId(20L);
        type.setDefaultDurationInDays(30);

        license = new License();
        license.setId(100L);
        license.setCode("ABCD1234EFGH5678");
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setDeviceCount(5);
        license.setBlocked(false);
    }

    @Test
    void createLicenseSuccess() {
        LicenseCreateRequest request = new LicenseCreateRequest();
        request.setProductId(10L);
        request.setTypeId(20L);
        request.setOwnerId(2L);
        request.setDeviceCount(3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(licenseTypeRepository.findById(20L)).thenReturn(Optional.of(type));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(licenseRepository.save(any(License.class))).thenAnswer(inv -> {
            License saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        License created = licenseService.createLicense(request, 1L);

        assertNotNull(created.getCode());
        assertEquals(3, created.getDeviceCount());
        verify(historyRepository).save(any());
    }

    @Test
    void activateAndCheckLicenseFlow() throws Exception {
        license.setUser(owner);
        license.setFirstActivationDate(LocalDateTime.now().minusDays(1));
        license.setEndingDate(LocalDateTime.now().plusDays(20));

        Device device = new Device();
        device.setId(50L);
        device.setMacAddress("AA:BB:CC:DD:EE:FF");
        device.setUser(owner);

        ActivateLicenseRequest activateRequest = new ActivateLicenseRequest();
        activateRequest.setActivationKey(license.getCode());
        activateRequest.setDeviceMac(device.getMacAddress());
        activateRequest.setDeviceName("PC-1");

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(licenseRepository.findByCode(license.getCode())).thenReturn(Optional.of(license));
        when(deviceRepository.findByMacAddress(device.getMacAddress())).thenReturn(Optional.of(device));
        when(deviceLicenseRepository.existsByLicenseIdAndDeviceId(100L, 50L)).thenReturn(true);
        when(signatureService.signTicket(any())).thenReturn("signed-base64");

        TicketResponse activateResponse = licenseService.activateLicense(activateRequest, 2L);
        assertNotNull(activateResponse.getTicket());
        assertEquals("signed-base64", activateResponse.getSignature());
        assertNotNull(activateResponse.getTicket().getServerTime());
        assertTrue(activateResponse.getTicket().getTicketLifetimeSeconds() > 0);
        assertNotNull(activateResponse.getTicket().getFirstActivationDate());
        assertNotNull(activateResponse.getTicket().getExpirationDate());
        assertEquals(2L, activateResponse.getTicket().getUserId());
        assertEquals(50L, activateResponse.getTicket().getDeviceId());
        assertFalse(activateResponse.getTicket().isBlocked());

        CheckLicenseRequest checkRequest = new CheckLicenseRequest();
        checkRequest.setDeviceMac(device.getMacAddress());

        when(licenseRepository.findFirstByDeviceLicensesDeviceIdOrderByEndingDateDesc(50L))
                .thenReturn(Optional.of(license));
        when(deviceLicenseRepository.existsByLicenseIdAndDeviceId(100L, 50L)).thenReturn(true);
        when(signatureService.verifyTicket(any(), any())).thenReturn(true);

        TicketResponse checkResponse = licenseService.checkLicense(checkRequest);
        assertFalse(checkResponse.getTicket().isBlocked());
        assertTrue(signatureService.verifyTicket(checkResponse.getTicket(), checkResponse.getSignature()));
    }

    @Test
    void renewLicenseWhenExpiringSoon() throws Exception {
        license.setUser(owner);
        license.setFirstActivationDate(LocalDateTime.now().minusDays(25));
        license.setEndingDate(LocalDateTime.now().plusDays(3));

        RenewLicenseRequest renewRequest = new RenewLicenseRequest();
        renewRequest.setCode(license.getCode());

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(licenseRepository.findByCode(license.getCode())).thenReturn(Optional.of(license));
        when(licenseRepository.save(license)).thenReturn(license);
        when(signatureService.signTicket(any())).thenReturn("renewed-signature");

        TicketResponse response = licenseService.renewLicense(renewRequest, 1L);

        assertNotNull(response.getTicket());
        assertEquals("renewed-signature", response.getSignature());
        verify(historyRepository).save(any());
    }
}
