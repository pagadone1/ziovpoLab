package com.example.photoprintapplication1.service;

import com.example.photoprintapplication1.dto.Ticket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;

@Service
public class SignatureService {

    private static final long DEFAULT_TICKET_LIFETIME_SECONDS = 3600L;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final ObjectMapper objectMapper;

    public SignatureService(
            @Value("${signature.private-key-path}") Resource keystoreResource,
            @Value("${signature.keystore-password}") String keystorePassword,
            @Value("${signature.alias}") String alias) {

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (var inputStream = keystoreResource.getInputStream()) {
                keystore.load(inputStream, keystorePassword.toCharArray());
            }

            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keystore.getEntry(
                    alias,
                    new KeyStore.PasswordProtection(keystorePassword.toCharArray())
            );

            this.privateKey = entry.getPrivateKey();
            Certificate cert = keystore.getCertificate(alias);
            this.publicKey = cert.getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SignatureService", e);
        }

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String signTicket(Ticket ticket) throws Exception {
        String ticketJson = objectMapper.writeValueAsString(ticket);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(ticketJson.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public boolean verifyTicket(Ticket ticket, String signatureBase64) throws Exception {
        String ticketJson = objectMapper.writeValueAsString(ticket);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(ticketJson.getBytes(StandardCharsets.UTF_8));

        byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(sigBytes);
    }

    public static long defaultTicketLifetimeSeconds() {
        return DEFAULT_TICKET_LIFETIME_SECONDS;
    }
}
