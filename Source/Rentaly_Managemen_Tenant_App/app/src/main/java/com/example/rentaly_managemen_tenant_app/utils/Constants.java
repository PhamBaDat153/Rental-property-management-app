package com.example.rentaly_managemen_tenant_app.utils;

public final class Constants {
    private Constants() {}

    // Keep this URL aligned with the admin app's ApiClient. For a physical
    // device, replace 10.0.2.2 with the development machine's LAN address.
    public static final String API_BASE_URL = "http://10.0.2.2:8080/";
    // Backend LoginType enum values: Manage (landlord) and Use (tenant).
    public static final String LOGIN_TYPE_USE = "Use";
    public static final String SESSION_PREFS = "rentaly_tenant_session";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_TENANT_ID = "tenant_id";
}
