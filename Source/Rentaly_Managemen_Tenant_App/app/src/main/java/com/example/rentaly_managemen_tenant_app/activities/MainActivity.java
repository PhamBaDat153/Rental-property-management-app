package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.ContractRepository;
import com.example.rentaly_managemen_tenant_app.repository.UserRepository;

import java.util.List;

public class MainActivity extends TenantBaseActivity {
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        createScreen("Khu vực của bạn", false);
        new UserRepository().getProfile(session.getUserId(), new ApiCallback<UserTenant>() {
            @Override
            public void onSuccess(UserTenant profile) {
                if (profile.tenant_id != null) session.saveTenantId(profile.tenant_id.toString());
                showLoaded();
                addSection("Xin chào, " + (profile.full_name == null
                        ? session.getUserName() : profile.full_name));
                addLine("Tài khoản: " + value(profile.user_name));
                addButton("Hồ sơ của tôi", v -> open(ProfileActivity.class));
                addButton("Hợp đồng thuê", v -> open(ContractActivity.class));
                addButton("Hóa đơn", v -> open(InvoiceListActivity.class));
                addButton("Thông báo", v -> open(AnnouncementActivity.class));
                addButton("Đăng xuất", v -> {
                    session.clear();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
                loadCurrentRoomActions();
            }

            @Override
            public void onError(String error) {
                showFailure(error);
                addButton("Đăng xuất", v -> {
                    session.clear();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
            }
        });
    }

    private void loadCurrentRoomActions() {
        new ContractRepository().getMyContracts(session.getTenantId(), new ApiCallback<List<RentalContract>>() {
            @Override
            public void onSuccess(List<RentalContract> contracts) {
                if (contracts.isEmpty()) {
                    addSection("Chưa có hợp đồng được liên kết với tài khoản này.");
                    return;
                }
                RentalContract current = contracts.get(0);
                for (RentalContract contract : contracts) {
                    if ("ACTIVE".equalsIgnoreCase(contract.status)) {
                        current = contract;
                        break;
                    }
                }
                roomId = current.room_id == null ? null : current.room_id.toString();
                addButton("Thông tin phòng", v -> openWithRoom(RoomInfoActivity.class));
                addButton("Điện, nước và chỉ số", v -> openWithRoom(ElectricityWaterActivity.class));
                addButton("Yêu cầu bảo trì", v -> openWithRoom(MaintenanceListActivity.class));
            }

            @Override
            public void onError(String error) { addLine(error); }
        });
    }

    private void open(Class<?> screen) {
        startActivity(new Intent(this, screen));
    }

    private void openWithRoom(Class<?> screen) {
        if (roomId == null) return;
        Intent intent = new Intent(this, screen);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
    }
}
