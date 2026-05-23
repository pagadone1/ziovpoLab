package com.example.ziovpo.license.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ziovpo.license.dto.ActivateLicenseRequest;
import com.example.ziovpo.license.dto.CheckLicenseRequest;
import com.example.ziovpo.license.dto.CreateLicenseRequest;
import com.example.ziovpo.license.dto.LicenseResponse;
import com.example.ziovpo.license.dto.RenewLicenseRequest;
import com.example.ziovpo.license.dto.TicketResponse;
import com.example.ziovpo.license.service.LicenseService;
import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
@Slf4j
public class LicenseController {

    private final LicenseService licenseService;
    private final UsersRepository usersRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> createLicense(@RequestBody CreateLicenseRequest request) {
        Users admin = currentUser();
        LicenseResponse created = licenseService.createLicense(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> activateLicense(@RequestBody ActivateLicenseRequest request) {
        Users user = currentUser();
        log.debug("Activate from user={}", user.getUsername());
        return ResponseEntity.ok(licenseService.activateLicense(request, user.getId()));
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> checkLicense(@RequestBody CheckLicenseRequest request) {
        Users user = currentUser();
        return ResponseEntity.ok(licenseService.checkLicense(request, user.getId()));
    }

    @PostMapping("/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> renewLicense(@RequestBody RenewLicenseRequest request) {
        Users admin = currentUser();
        return ResponseEntity.ok(licenseService.renewLicense(request, admin.getId()));
    }

    private Users currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }
}
