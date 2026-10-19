package com.android_dev.rentaly_management.DTO;

import java.util.List;
import java.util.UUID;

public class MaintenanceRequest {

    public UUID user_id;
    public UUID room_id;

    public String title;
    public String description;
    public String priority;
    public String status;

    public List<String> image_urls;

    public MaintenanceRequest(
            UUID user,
            UUID room,
            String title,
            String description,
            String priority,
            String status
    ) {
        this.user_id = user;
        this.room_id = room;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }
}