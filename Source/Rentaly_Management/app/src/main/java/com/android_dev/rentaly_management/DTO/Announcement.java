package com.android_dev.rentaly_management.DTO;
import java.time.LocalDateTime;import java.util.List;import java.util.UUID;
public class Announcement{public UUID announcement_id;public String content,announcement_type,status;public LocalDateTime sent_at,created_at;public List<UUID> recipient_ids;}
