package com.android_dev.rentaly_management.DTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;

public class Maintenance implements Serializable {

    public UUID request_id;
    public UUID user_id;
    public UUID room_id;

    public String title;
    public String description;
    public String priority;
    public String status;

    public LocalDateTime created_at;
    public LocalDateTime updated_at;
    public LocalDateTime completed_at;

    public List<String> image_urls;
}