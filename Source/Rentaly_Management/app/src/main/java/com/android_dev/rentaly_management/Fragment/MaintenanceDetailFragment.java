package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.DTO.MaintenanceRequest;
import com.android_dev.rentaly_management.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceDetailFragment extends Fragment {
    private static final int IMAGE_PICKER_REQUEST = 501;
    private Maintenance maintenance;
    private Uri selectedImage;
    private Spinner statusSpinner;
    private LinearLayout imageContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = inflater.inflate(R.layout.fragment_maintenance_detail, container, false);
        maintenance = (Maintenance) requireArguments().getSerializable("maintenance");
        bind(view);
        return view;
    }

    private void bind(View view) {
        if (maintenance == null) {
            showMessage("Không tìm thấy yêu cầu bảo trì");
            return;
        }
        set(view, R.id.maintenance_detail_title, value(maintenance.title));
        set(view, R.id.maintenance_detail_description, value(maintenance.description));
        set(view, R.id.maintenance_detail_priority, "Ưu tiên: " + value(maintenance.priority));
        set(view, R.id.maintenance_detail_room, "Phòng: " + value(maintenance.room_id));
        set(view, R.id.maintenance_detail_created, "Tạo lúc: " + value(maintenance.created_at));
        set(view, R.id.maintenance_detail_completed, "Hoàn tất: " + value(maintenance.completed_at));

        statusSpinner = view.findViewById(R.id.maintenance_detail_status);
        statusSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Đang chờ", "Đang xử lý", "Đã hoàn tất"}));
        statusSpinner.setSelection(statusIndex(maintenance.status));

        imageContainer = view.findViewById(R.id.maintenance_detail_images);
        renderImages();

        view.findViewById(R.id.maintenance_detail_back).setOnClickListener(v -> goBack());
        view.findViewById(R.id.maintenance_detail_choose_image).setOnClickListener(v -> chooseImage());
        view.findViewById(R.id.maintenance_detail_save).setOnClickListener(v -> save());
    }

    private void renderImages() {
        imageContainer.removeAllViews();
        if (maintenance.image_urls == null || maintenance.image_urls.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có hình ảnh");
            imageContainer.addView(empty);
            return;
        }
        for (String url : maintenance.image_urls) {
            TextView image = new TextView(requireContext());
            image.setText(url);
            image.setTextColor(getResources().getColor(R.color.secondary));
            image.setPadding(0, 8, 0, 8);
            imageContainer.addView(image);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            selectedImage = data.getData();
            showMessage("Đã chọn hình ảnh");
        }
    }

    private void save() {
        if (maintenance.request_id == null) return;
        String status = new String[]{"PENDING", "PROCESSING", "COMPLETED"}
                [statusSpinner.getSelectedItemPosition()];
        MaintenanceRequest payload = new MaintenanceRequest(
                maintenance.user_id,
                maintenance.room_id,
                maintenance.title,
                maintenance.description,
                maintenance.priority,
                status
        );
        RequestBody json = RequestBody.create(ApiClient.gson.toJson(payload), MediaType.parse("application/json"));
        MultipartBody.Part requestPart = MultipartBody.Part.createFormData("request", "request.json", json);
        List<MultipartBody.Part> images = new ArrayList<>();
        try {
            if (selectedImage != null) {
                String mime = requireContext().getContentResolver().getType(selectedImage);
                if (mime == null || !mime.startsWith("image/")) throw new IllegalArgumentException("Tệp phải là hình ảnh");
                images.add(MultipartBody.Part.createFormData("images", "maintenance-image", 
                        RequestBody.create(readBytes(selectedImage), MediaType.parse(mime))));
            }
        } catch (Exception e) {
            showMessage("Không thể đọc hình ảnh");
            return;
        }
        ApiClient.api.updateMaintenanceWithImages(maintenance.request_id.toString(), requestPart, images)
                .enqueue(new Callback<Maintenance>() {
                    @Override public void onResponse(@NonNull Call<Maintenance> call, @NonNull Response<Maintenance> response) {
                        if (response.isSuccessful()) { showMessage("Đã cập nhật yêu cầu"); goBack(); }
                        else showMessage("Không thể cập nhật yêu cầu");
                    }
                    @Override public void onFailure(@NonNull Call<Maintenance> call, @NonNull Throwable t) {
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                });
    }

    private byte[] readBytes(Uri uri) throws Exception {
        try (InputStream input = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalArgumentException();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private int statusIndex(String status) {
        if ("PROCESSING".equalsIgnoreCase(status)) return 1;
        if ("COMPLETED".equalsIgnoreCase(status)) return 2;
        return 0;
    }

    private void set(View view, int id, String text) { ((TextView) view.findViewById(id)).setText(text); }
    private String value(Object value) { return value == null ? "Chưa cập nhật" : String.valueOf(value); }
    private void goBack() { if (isAdded()) NavHostFragment.findNavController(this).navigateUp(); }
    private void showMessage(String message) { if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
