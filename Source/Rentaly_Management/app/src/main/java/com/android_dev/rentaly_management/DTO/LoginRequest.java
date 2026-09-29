package com.android_dev.rentaly_management.DTO;

public class LoginRequest {
    private final String username;
    private final String password;
    private final String loginType;

    public LoginRequest(String username, String password, String loginType) {
        this.username = username;
        this.password = password;
        this.loginType = loginType;
    }
}
