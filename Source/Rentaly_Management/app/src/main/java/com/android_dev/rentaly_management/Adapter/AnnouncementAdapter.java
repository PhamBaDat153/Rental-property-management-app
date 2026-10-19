package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.Announcement;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Adapter hiển thị nội dung và trạng thái của thông báo. */
public class AnnouncementAdapter extends ArrayAdapter<Announcement> {

    public AnnouncementAdapter(Context context, List<Announcement> announcements) {
        super(context, 0, announcements);
    }

    /** Tạo hoặc tái sử dụng item rồi gán nội dung thông báo. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_announcement, parent, false);
        }

        Announcement announcement = getItem(position);
        if (announcement == null) {
            return row;
        }

        TextView contentText = row.findViewById(R.id.announcement_item_content);
        TextView metaText = row.findViewById(R.id.announcement_item_meta);

        contentText.setText(value(announcement.content));
        metaText.setText(
                ("SENTED".equalsIgnoreCase(announcement.status)
                        ? "Đã gửi" : "Bản nháp")
                        + " | Người nhận: " + recipientCount(announcement)
        );

        return row;
    }

    private static int recipientCount(Announcement announcement) {
        return announcement.recipient_ids == null
                ? 0
                : announcement.recipient_ids.size();
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
