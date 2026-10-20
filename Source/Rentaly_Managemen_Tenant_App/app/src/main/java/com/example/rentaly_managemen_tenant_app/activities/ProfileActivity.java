package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.UserRepository;

public class ProfileActivity extends TenantBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Hồ sơ người thuê");
        new UserRepository().getProfile(session.getUserId(), new ApiCallback<UserTenant>() {
            @Override
            public void onSuccess(UserTenant user) {
                showLoaded();
                addSection(value(user.full_name));
                addSection("Tài khoản");
                addLine("Tên đăng nhập: " + value(user.user_name));
                addLine("Trạng thái: " + value(user.status));

                addSection("Thông tin cá nhân");
                addLine("Điện thoại: " + value(user.phone));
                addLine("Email: " + value(user.email));
                addLine("Giới tính: " + value(user.gender));
                addLine("Ngày sinh: " + value(user.date_of_birth));

                addSection("Giấy tờ tùy thân");
                addLine("Loại giấy tờ: " + value(user.identityType));
                addLine("Số giấy tờ: " + value(user.identity_number));
                addLine("Ngày/nơi cấp: " + value(user.identity_issued_date) + " / " + value(user.identity_issued_place));

                addSection("Địa chỉ và liên hệ");
                addLine("Địa chỉ thường trú: " + value(user.permanent_address));
                addLine("Liên hệ khẩn cấp: " + value(user.emergency_contact_name)
                        + " — " + value(user.emergency_contact_phone));
                if (user.additional_note != null) addLine("Ghi chú: " + user.additional_note);
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
