package com.android_dev.rentaly_management.Adapter;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import java.net.URL;
import java.util.List;

/** Adapter hiển thị tài khoản và thông tin người thuê trong danh sách quản lý user. */
public class UserListAdapter extends ArrayAdapter<UserTenant> {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserListAdapter(android.content.Context context, List<UserTenant> users) {
        super(context, 0, users);
    }

    /** Tạo một dòng user và nạp avatar nếu user có URL ảnh. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView == null
                ? LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false)
                : convertView;
        UserTenant user = getItem(position);
        ImageView avatar = view.findViewById(R.id.user_avatar);
        TextView name = view.findViewById(R.id.user_name);
        TextView username = view.findViewById(R.id.user_username);
        TextView contact = view.findViewById(R.id.user_contact);
        TextView status = view.findViewById(R.id.user_status);

        name.setText(user.displayName());
        username.setText("Tên đăng nhập: " + value(user.getUser_name()));
        contact.setText(value(user.getPhone(), user.getEmail()));
        status.setText(value(user.getRole()) + " · " + ("ACTIVE".equalsIgnoreCase(user.getStatus())
                ? "Đang hoạt động" : "Không hoạt động"));
        // Đặt ảnh mặc định trước để tránh hiển thị ảnh của item khác
        // khi ListView tái sử dụng convertView.
        avatar.setTag(user.getAvatar_url());
        avatar.setImageResource(R.drawable.profile_icon);
        String avatarUrl = user.getAvatar_url();
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            new Thread(() -> {
                try {
                    final android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(new URL(avatarUrl).openStream());
                    if (bitmap != null) mainHandler.post(() -> {
                        // Chỉ cập nhật nếu row vẫn đang đại diện cho đúng user.
                        if (avatarUrl.equals(avatar.getTag())) avatar.setImageBitmap(bitmap);
                    });
                } catch (Exception ignored) { }
            }).start();
        }
        return view;
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value;
    }

    private String value(String primary, String secondary) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return value(secondary);
    }
}
