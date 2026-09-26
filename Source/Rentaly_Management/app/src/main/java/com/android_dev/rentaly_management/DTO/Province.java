package com.android_dev.rentaly_management.DTO;

import java.util.List;

public class Province {
    public int code;
    public String name;
    public List<Province> districts;
    public List<Province> wards;

    @Override
    public String toString() {
        return name == null || name.trim().isEmpty() ? "Chưa cập nhật" : name;
    }
}
