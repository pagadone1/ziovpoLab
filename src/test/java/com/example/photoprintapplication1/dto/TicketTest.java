package com.example.photoprintapplication1.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketTest {

    @Test
    void ticketHasAllRequiredFieldsFromAssignment() {
        Ticket ticket = new Ticket();
        LocalDateTime serverTime = LocalDateTime.of(2026, 5, 23, 12, 0);
        LocalDate activation = LocalDate.of(2026, 5, 1);
        LocalDate expiration = LocalDate.of(2026, 6, 1);

        ticket.setServerTime(serverTime);
        ticket.setTicketLifetimeSeconds(3600L);
        ticket.setFirstActivationDate(activation);
        ticket.setExpirationDate(expiration);
        ticket.setUserId(10L);
        ticket.setDeviceId(20L);
        ticket.setBlocked(false);

        assertEquals(serverTime, ticket.getServerTime());
        assertEquals(3600L, ticket.getTicketLifetimeSeconds());
        assertEquals(activation, ticket.getFirstActivationDate());
        assertEquals(expiration, ticket.getExpirationDate());
        assertEquals(10L, ticket.getUserId());
        assertEquals(20L, ticket.getDeviceId());
        assertFalse(ticket.isBlocked());

        ticket.setBlocked(true);
        assertTrue(ticket.isBlocked());
    }

    @Test
    void ticketResponseWrapsTicketAndSignature() {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.now());
        TicketResponse response = new TicketResponse(ticket, "base64-signature");

        assertEquals(ticket, response.getTicket());
        assertEquals("base64-signature", response.getSignature());
    }
}
