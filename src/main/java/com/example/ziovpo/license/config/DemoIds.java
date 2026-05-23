package com.example.ziovpo.license.config;

import java.util.UUID;

/**
 * Фиксированные UUID для демо и Postman (стабильные между перезапусками).
 */
public final class DemoIds {

    public static final UUID ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    public static final UUID CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111102");
    public static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222201");
    public static final UUID TYPE_STANDARD_ID = UUID.fromString("33333333-3333-3333-3333-333333333301");
    public static final UUID TYPE_ANNUAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333302");

    private DemoIds() {
    }
}
