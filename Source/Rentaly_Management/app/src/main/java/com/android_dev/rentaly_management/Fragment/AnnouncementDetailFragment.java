package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Announcement;
import com.android_dev.rentaly_management.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementDetailFragment extends Fragment {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String announcementId;
    private Announcement announcement;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_announcement_detail, container, false);
        bind(view);
        return view;
    }

    private void bind(@NonNull View view) {
        Bundle args = getArguments();
        if (args == null) {
            showMessage("Không tìm thấy thông báo");
            return;
        }

        announcementId = args.getString("announcement_id");
        announcement = new Announcement();
        announcement.announcement_id = parseId(announcementId);
        announcement.content = args.getString("announcement_content");
        announcement.announcement_type = args.getString("announcement_type");
        announcement.status = args.getString("announcement_status");
        announcement.created_at = parseDate(args.getString("announcement_created_at"));
        announcement.sent_at = parseDate(args.getString("announcement_sent_at"));
        ArrayList<UUID> recipientIds = (ArrayList<UUID>) args.getSerializable("announcement_recipient_ids");
        announcement.recipient_ids = recipientIds == null ? new ArrayList<>() : recipientIds;

        setText(view, R.id.announcement_detail_status,
                "SENTED".equalsIgnoreCase(announcement.status) ? "Đã gửi" : "Bản nháp");
        setText(view, R.id.announcement_detail_type,
                "Loại: " + value(announcement.announcement_type));
        setText(view, R.id.announcement_detail_created,
                "Tạo lúc: " + formatDate(announcement.created_at));
        setText(view, R.id.announcement_detail_sent,
                "Gửi lúc: " + formatDate(announcement.sent_at));
        setText(view, R.id.announcement_detail_content, value(announcement.content));
        setText(view, R.id.announcement_detail_recipients,
                announcement.recipient_ids.size() + " người nhận");

        view.findViewById(R.id.announcement_detail_back)
                .setOnClickListener(ignored -> goBack());
        view.findViewById(R.id.announcement_detail_edit)
                .setOnClickListener(ignored -> editAnnouncement());
        view.findViewById(R.id.announcement_detail_delete)
                .setOnClickListener(ignored -> confirmDelete());
    }

    private void editAnnouncement() {
        if (announcementId == null) {
            showMessage("Không xác định được thông báo");
            return;
        }

        Bundle args = new Bundle();
        args.putString("announcement_id", announcementId);
        args.putString("announcement_content", announcement.content);
        args.putString("announcement_type", announcement.announcement_type);
        args.putString("announcement_status", announcement.status);
        args.putSerializable("announcement_recipient_ids", new ArrayList<>(announcement.recipient_ids));
        args.putBoolean("create_announcement", true);
        NavHostFragment.findNavController(this).navigate(R.id.announcementFragment, args);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thông báo?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteAnnouncement())
                .show();
    }

    private void deleteAnnouncement() {
        if (announcementId == null) {
            showMessage("Không xác định được thông báo");
            return;
        }

        ApiClient.api.deleteAnnouncement(announcementId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    showMessage("Không thể xóa thông báo");
                    return;
                }
                showMessage("Đã xóa thông báo");
                goBack();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                if (isAdded()) showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void goBack() {
        if (isAdded()) NavHostFragment.findNavController(this).navigateUp();
    }

    private void setText(View view, int id, String text) {
        TextView target = view.findViewById(id);
        if (target != null) target.setText(text);
    }

    private String value(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa cập nhật" : value;
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "Chưa cập nhật" : date.format(DISPLAY_FORMAT);
    }

    private LocalDateTime parseDate(String value) {
        try {
            return value == null ? null : LocalDateTime.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private UUID parseId(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void showMessage(String message) {
        if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}
