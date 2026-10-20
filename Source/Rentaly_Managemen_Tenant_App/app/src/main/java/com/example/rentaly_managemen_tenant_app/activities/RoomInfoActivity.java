package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Location;
import com.example.rentaly_managemen_tenant_app.models.Room;
import com.example.rentaly_managemen_tenant_app.models.RoomService;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.RoomRepository;

import java.util.List;

public class RoomInfoActivity extends TenantBaseActivity {
    private RoomRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Thông tin phòng");
        String roomId = getIntent().getStringExtra("room_id");
        if (roomId == null) {
            showFailure("Không tìm thấy phòng được gắn với hợp đồng.");
            return;
        }
        repository = new RoomRepository();
        repository.getRoom(roomId, new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room room) {
                showLoaded();
                addSection(value(room.room_name) + "  ·  " + value(room.room_code));
                addSection("Thông tin phòng");
                addLine("Trạng thái: " + value(room.status));
                addLine("Tầng: " + value(room.floor));
                addLine("Diện tích: " + value(room.area_m2) + " m²");
                addLine("Sức chứa: " + value(room.max_occupants));
                addLine("Giá thuê niêm yết: " + value(room.rent_price));
                if (room.description != null) addLine("Mô tả: " + room.description);
                if (room.location_id != null) {
                    addSection("Địa chỉ");
                    loadLocation(room.location_id.toString());
                }
                loadServices(roomId);
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }

    private void loadLocation(String id) {
        repository.getLocation(id, new ApiCallback<Location>() {
            @Override
            public void onSuccess(Location location) {
                String address = value(location.address_line);
                if (location.province_name != null) address += ", " + location.province_name;
                if (location.district_name != null) address += ", " + location.district_name;
                if (location.ward_name != null) address += ", " + location.ward_name;
                addLine("Địa chỉ: " + address);
            }
            @Override
            public void onError(String error) { addLine("Không tải được địa chỉ: " + error); }
        });
    }

    private void loadServices(String roomId) {
        repository.getRoomServices(roomId, new ApiCallback<List<RoomService>>() {
            @Override
            public void onSuccess(List<RoomService> services) {
                addSection("Dịch vụ đang áp dụng");
                if (services.isEmpty()) addLine("Chưa có dịch vụ được gán.");
                for (RoomService service : services) {
                    addSection(value(service.name));
                    addLine("Đơn giá: " + value(service.default_unit_price) + " / " + value(service.unit));
                    if (Boolean.FALSE.equals(service.is_active)) addLine("Trạng thái: Ngừng hoạt động");
                }
            }
            @Override
            public void onError(String error) { addLine("Không tải được dịch vụ phòng: " + error); }
        });
    }
}
