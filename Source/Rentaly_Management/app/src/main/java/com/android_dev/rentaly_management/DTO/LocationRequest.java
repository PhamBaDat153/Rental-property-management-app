package com.android_dev.rentaly_management.DTO;

public class LocationRequest {
    public String location_code;
    public String address_line;
    public String ward_name;
    public String district_name;
    public String province_name;
    public String status;
    public String description;

    public LocationRequest(String code, String address, String ward, String district,
                           String province, String status, String description) {
        location_code = code;
        address_line = address;
        ward_name = ward;
        district_name = district;
        province_name = province;
        this.status = status;
        this.description = description;
    }
}
