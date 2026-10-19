package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

public class UserRepository {
    public void getProfile(String userId, ApiCallback<UserTenant> callback) {
        ApiRequests.enqueue(ApiClient.api.getUserById(userId), callback);
    }
}
