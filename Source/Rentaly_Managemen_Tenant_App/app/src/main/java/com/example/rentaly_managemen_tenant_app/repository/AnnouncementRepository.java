package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.Announcement;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.List;

public class AnnouncementRepository {
    public void getAnnouncements(ApiCallback<List<Announcement>> callback) {
        ApiRequests.enqueue(ApiClient.api.getAnnouncements(), callback);
    }
}
