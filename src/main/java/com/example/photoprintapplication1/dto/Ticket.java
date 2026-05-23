package com.example.photoprintapplication1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Тикет с информацией о лицензии для клиента.
 */
public class Ticket {

    private LocalDateTime serverTime;
    private long ticketLifetimeSeconds;
    private LocalDate firstActivationDate;
    private LocalDate expirationDate;
    private Long userId;
    private Long deviceId;
    private boolean blocked;

    public Ticket() {
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public long getTicketLifetimeSeconds() {
        return ticketLifetimeSeconds;
    }

    public void setTicketLifetimeSeconds(long ticketLifetimeSeconds) {
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
    }

    public LocalDate getFirstActivationDate() {
        return firstActivationDate;
    }

    public void setFirstActivationDate(LocalDate firstActivationDate) {
        this.firstActivationDate = firstActivationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
