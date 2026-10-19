package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.Maintenance;
import com.example.rentaly_managemen_tenant_app.models.MaintenanceRequest;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.List;

public class MaintenanceRepository {
    public void getRequests(String roomId, String status, String priority,
                            ApiCallback<List<Maintenance>> callback) {
        ApiRequests.enqueue(ApiClient.api.getMaintenance(roomId, status, priority), callback);
    }

    public void createRequest(MaintenanceRequest request, ApiCallback<Maintenance> callback) {
        ApiRequests.enqueue(ApiClient.api.createMaintenance(request), callback);
    }
}
