package com.android_dev.rentaly_management.DTO;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
public class Maintenance { public UUID request_id, user_id, room_id; public String title, description, priority, status; public LocalDateTime created_at, updated_at, completed_at; public List<String> image_urls; }
