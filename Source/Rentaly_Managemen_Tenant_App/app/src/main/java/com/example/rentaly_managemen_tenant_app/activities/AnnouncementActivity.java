package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Announcement;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.AnnouncementRepository;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementActivity extends TenantBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Thông báo");
        new AnnouncementRepository().getAnnouncements(new ApiCallback<List<Announcement>>() {
            @Override
            public void onSuccess(List<Announcement> announcements) {
                showLoaded();
                List<Announcement> visible = new ArrayList<>();
                for (Announcement item : announcements) {
                    if (item.recipient_ids == null || item.recipient_ids.isEmpty()
                            || item.recipient_ids.contains(java.util.UUID.fromString(session.getUserId()))
                            || (session.getTenantId() != null && item.recipient_ids.contains(
                                    java.util.UUID.fromString(session.getTenantId())))) {
                        visible.add(item);
                    }
                }
                if (visible.isEmpty()) {
                    showEmpty("Hiện chưa có thông báo dành cho bạn.");
                    return;
                }
                for (Announcement item : visible) {
                    addSection(value(item.announcement_type) + " · " + value(item.sent_at));
                    addLine(value(item.content));
                    if (item.status != null) addLine("Trạng thái: " + item.status);
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
