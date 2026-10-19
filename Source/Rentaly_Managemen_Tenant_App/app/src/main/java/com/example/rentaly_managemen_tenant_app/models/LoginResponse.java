package com.example.rentaly_managemen_tenant_app.models;

import java.util.UUID;

public class LoginResponse {
    private UUID user_id;
    private String user_name;

    public UUID getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }
}
