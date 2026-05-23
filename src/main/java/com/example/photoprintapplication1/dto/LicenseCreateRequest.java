package com.example.photoprintapplication1.dto;

public class LicenseCreateRequest {
    private Long productId;
    private Long typeId;
    private Long ownerId;       // владелец админ или компания
    private Integer deviceCount;
    private String description;

    // Геттеры и сеттеры
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Integer getDeviceCount() { return deviceCount; }
    public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}