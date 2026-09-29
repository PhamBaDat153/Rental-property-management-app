package com.android_dev.rentaly_management.DTO;
import java.util.List;import java.util.UUID;
public class AnnouncementRequest{public String content,announcement_type;public boolean send;public List<UUID> recipient_ids;public AnnouncementRequest(String content,String type,boolean send,List<UUID> recipients){this.content=content;announcement_type=type;this.send=send;recipient_ids=recipients;}}
