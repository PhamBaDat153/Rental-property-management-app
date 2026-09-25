package com.android_dev.rentaly_management.DTO;

public class UserUpdateRequest {
    private final String role;
    private final String status;

    public UserUpdateRequest(String role, String status) {
        this.role = role;
        this.status = status;
    }
}
