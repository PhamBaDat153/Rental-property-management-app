package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Maintenance;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.MaintenanceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaintenanceListActivity extends TenantBaseActivity {
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Yêu cầu bảo trì");
        roomId = getIntent().getStringExtra("room_id");
        if (roomId == null) {
            showFailure("Không tìm thấy phòng được gắn với hợp đồng.");
            return;
        }
        addButton("+ Tạo yêu cầu bảo trì", v -> {
            Intent intent = new Intent(this, CreateMaintenanceActivity.class);
            intent.putExtra("room_id", roomId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (content != null && roomId != null) loadRequests();
    }

    private void loadRequests() {
        new MaintenanceRepository().getRequests(roomId, null, null, new ApiCallback<List<Maintenance>>() {
            @Override
            public void onSuccess(List<Maintenance> requests) {
                UUID currentUser = UUID.fromString(session.getUserId());
                List<Maintenance> mine = new ArrayList<>();
                for (Maintenance item : requests) {
                    if (item.user_id != null && item.user_id.equals(currentUser)) mine.add(item);
                }
                for (int index = content.getChildCount() - 1; index >= 1; index--) content.removeViewAt(index);
                showLoaded();
                if (mine.isEmpty()) {
                    addLine("Bạn chưa gửi yêu cầu bảo trì nào.");
                    return;
                }
                for (Maintenance item : mine) {
                    addSection(value(item.title) + " · " + value(item.status));
                    addLine("Ưu tiên: " + value(item.priority) + " | Tạo lúc: " + value(item.created_at));
                    addLine(value(item.description));
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
