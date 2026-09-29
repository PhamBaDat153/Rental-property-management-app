package com.android_dev.rentaly_management.DTO;

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
