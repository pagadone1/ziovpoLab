package com.example.photoprintapplication1.service;

import com.example.photoprintapplication1.dto.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SignatureServiceTest {

    @Autowired
    private SignatureService signatureService;

    @Test
    void signAndVerifyTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.now());
        ticket.setTicketLifetimeSeconds(SignatureService.defaultTicketLifetimeSeconds());
        ticket.setFirstActivationDate(LocalDate.now());
        ticket.setExpirationDate(LocalDate.now().plusDays(30));
        ticket.setUserId(1L);
        ticket.setDeviceId(2L);
        ticket.setBlocked(false);

        String signature = signatureService.signTicket(ticket);
        assertTrue(signatureService.verifyTicket(ticket, signature));
    }
}
