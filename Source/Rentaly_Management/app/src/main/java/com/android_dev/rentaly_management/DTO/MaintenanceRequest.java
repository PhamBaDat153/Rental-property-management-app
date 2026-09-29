package com.android_dev.rentaly_management.DTO;
import java.util.List; import java.util.UUID;
public class MaintenanceRequest { public UUID user_id, room_id; public String title, description, priority, status; public List<String> image_urls; public MaintenanceRequest(UUID user, UUID room, String title, String description, String priority, String status) { user_id=user; room_id=room; this.title=title; this.description=description; this.priority=priority; this.status=status; } }
