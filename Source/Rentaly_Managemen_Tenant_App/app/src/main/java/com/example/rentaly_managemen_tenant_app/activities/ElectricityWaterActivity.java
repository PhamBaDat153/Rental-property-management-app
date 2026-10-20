package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Meter;
import com.example.rentaly_managemen_tenant_app.models.MeterReading;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.MeterRepository;

import java.util.List;

public class ElectricityWaterActivity extends TenantBaseActivity {
    private MeterRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Điện, nước và chỉ số");
        String roomId = getIntent().getStringExtra("room_id");
        if (roomId == null) {
            showFailure("Không tìm thấy phòng được gắn với hợp đồng.");
            return;
        }
        repository = new MeterRepository();
        repository.getMeters(roomId, new ApiCallback<List<Meter>>() {
            @Override
            public void onSuccess(List<Meter> meters) {
                showLoaded();
                if (meters.isEmpty()) {
                    showEmpty("Phòng chưa có đồng hồ điện hoặc nước.");
                    return;
                }
                for (Meter meter : meters) {
                    addSection(value(meter.meter_type));
                    addLine("Trạng thái đồng hồ: " + value(meter.status));
                    if (meter.meter_id != null) {
                        String id = meter.meter_id.toString();
                        addButton("Xem lịch sử chỉ số", v -> loadReadings(id));
                    }
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }

    private void loadReadings(String meterId) {
        repository.getReadings(meterId, new ApiCallback<List<MeterReading>>() {
            @Override
            public void onSuccess(List<MeterReading> readings) {
                addSection("Lịch sử ghi chỉ số");
                if (readings.isEmpty()) {
                    addLine("Chưa có lần ghi chỉ số nào.");
                    return;
                }
                for (MeterReading reading : readings) {
                    addSection(value(reading.reading_at));
                    addLine("Chỉ số trước: " + value(reading.previous_value));
                    addLine("Chỉ số mới: " + value(reading.current_value));
                    addLine("Mức tiêu thụ: " + value(reading.quantity));
                    if (reading.note != null && !reading.note.trim().isEmpty()) {
                        addLine("Ghi chú: " + reading.note);
                    }
                }
            }

            @Override
            public void onError(String error) { addLine(error); }
        });
    }
}
