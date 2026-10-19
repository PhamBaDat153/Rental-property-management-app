package com.example.rentaly_managemen_tenant_app.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Announcement {
    public UUID announcement_id;
    public String content;
    public String announcement_type;
    public String status;
    public LocalDateTime sent_at;
    public LocalDateTime created_at;
    public List<UUID> recipient_ids;
}
