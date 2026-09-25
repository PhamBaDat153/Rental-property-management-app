package com.android_dev.rentaly_management.DTO;

import java.util.UUID;

public class UserTenant {
    private UUID user_id;
    private String user_name;
    private String role;
    private String status;
    private UUID tenant_id;
    private String full_name;
    private String date_of_birth;
    private String phone;
    private String email;
    private String gender;
    private String avatar_url;
    private String identityType;
    private String identity_number;
    private String identity_issued_date;
    private String identity_issued_place;
    private String permanent_address;
    private String emergency_contact_name;
    private String emergency_contact_phone;
    private String additional_note;

    public UUID getUser_id() { return user_id; }
    public String getUser_name() { return user_name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public UUID getTenant_id() { return tenant_id; }
    public String getFull_name() { return full_name; }
    public String getDate_of_birth() { return date_of_birth; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAvatar_url() { return avatar_url; }
    public String getGender() { return gender; }
    public String getIdentityType() { return identityType; }
    public String getIdentity_number() { return identity_number; }
    public String getIdentity_issued_date() { return identity_issued_date; }
    public String getIdentity_issued_place() { return identity_issued_place; }
    public String getPermanent_address() { return permanent_address; }
    public String getEmergency_contact_name() { return emergency_contact_name; }
    public String getEmergency_contact_phone() { return emergency_contact_phone; }
    public String getAdditional_note() { return additional_note; }

    public String displayName() {
        return full_name == null || full_name.isBlank() ? "Chưa cập nhật tên" : full_name;
    }
}
