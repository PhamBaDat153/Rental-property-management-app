package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.Meter;
import com.example.rentaly_managemen_tenant_app.models.MeterReading;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.List;

public class MeterRepository {
    public void getMeters(String roomId, ApiCallback<List<Meter>> callback) {
        ApiRequests.enqueue(ApiClient.api.getMeters(roomId), callback);
    }

    public void getReadings(String meterId, ApiCallback<List<MeterReading>> callback) {
        ApiRequests.enqueue(ApiClient.api.getMeterReadings(meterId), callback);
    }
}
