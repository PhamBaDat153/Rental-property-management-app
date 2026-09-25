package com.android_dev.rentaly_management.Fragment;

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

public class UserListAdapter extends ArrayAdapter<UserTenant> {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserListAdapter(android.content.Context context, List<UserTenant> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView == null
                ? LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false)
                : convertView;
        UserTenant user = getItem(position);
        ImageView avatar = view.findViewById(R.id.user_avatar);
        TextView name = view.findViewById(R.id.user_name);
        TextView username = view.findViewById(R.id.user_username);
        TextView status = view.findViewById(R.id.user_status);

        name.setText(user.displayName());
        username.setText("Tên đăng nhập: " + (user.getUser_name() == null || user.getUser_name().isBlank()
                ? "Chưa cập nhật" : user.getUser_name()));
        status.setText("Trạng thái: " + ("ACTIVE".equalsIgnoreCase(user.getStatus())
                ? "Đang hoạt động" : "Không hoạt động"));
        avatar.setTag(user.getAvatar_url());
        avatar.setImageResource(R.drawable.profile_icon);
        String avatarUrl = user.getAvatar_url();
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            new Thread(() -> {
                try {
                    final android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(new URL(avatarUrl).openStream());
                    if (bitmap != null) mainHandler.post(() -> {
                        if (avatarUrl.equals(avatar.getTag())) avatar.setImageBitmap(bitmap);
                    });
                } catch (Exception ignored) { }
            }).start();
        }
        return view;
    }
}
