package com.android_dev.rentaly_management.DTO;

import java.util.UUID;

public class User {

    private UUID user_id;
    private String user_name;
    private String password;

    public User(UUID user_id, String user_name) {
        this.user_id = user_id;
        this.user_name = user_name;
    }

    public User(String user_name, String password) {
        this.user_name = user_name;
        this.password = password;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
