package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserUpdateRequest;
import com.android_dev.rentaly_management.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailFragment extends Fragment {
    private UserTenant user;
    private RadioGroup role;
    private RadioGroup status;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_detail, container, false);
        role = view.findViewById(R.id.user_detail_role);
        status = view.findViewById(R.id.user_detail_status);
        String userId = requireArguments().getString("user_id");

        view.findViewById(R.id.user_detail_back).setOnClickListener(v -> goBack());
        view.findViewById(R.id.user_detail_update).setOnClickListener(v -> update(userId));
        view.findViewById(R.id.user_detail_delete).setOnClickListener(v -> delete(userId));
        load(userId, view);
        return view;
    }

    private void load(String id, View view) {
        ApiClient.api.managedUser(id).enqueue(new Callback<UserTenant>() {
            @Override
            public void onResponse(Call<UserTenant> call, Response<UserTenant> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Không thể tải thông tin người dùng");
                    return;
                }
                user = response.body();
                ((TextView) view.findViewById(R.id.user_detail_title)).setText(user.displayName());
                ((TextView) view.findViewById(R.id.user_detail_info)).setText(details(user));
                ((RadioButton) view.findViewById("TENANT".equalsIgnoreCase(user.getRole())
                        ? R.id.role_tenant : R.id.role_landlord)).setChecked(true);
                ((RadioButton) view.findViewById("ACTIVE".equalsIgnoreCase(user.getStatus())
                        ? R.id.status_active : R.id.status_inactive)).setChecked(true);
            }

            @Override
            public void onFailure(Call<UserTenant> call, Throwable t) {
                showError("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void update(String id) {
        int roleId = role.getCheckedRadioButtonId();
        int statusId = status.getCheckedRadioButtonId();
        if (roleId == -1 || statusId == -1) {
            showError("Vui lòng chọn role và trạng thái");
            return;
        }
        String selectedRole = roleId == R.id.role_landlord ? "LANDLORD" : "TENANT";
        String selectedStatus = statusId == R.id.status_active ? "ACTIVE" : "INACTIVE";
        ApiClient.api.updateManagedUser(id, new UserUpdateRequest(selectedRole, selectedStatus))
                .enqueue(new Callback<UserTenant>() {
                    @Override
                    public void onResponse(Call<UserTenant> call, Response<UserTenant> response) {
                        if (response.isSuccessful()) showError("Đã cập nhật người dùng");
                        else showError(errorMessage(response, "Không thể cập nhật người dùng"));
                    }

                    @Override
                    public void onFailure(Call<UserTenant> call, Throwable t) {
                        showError("Không thể kết nối đến máy chủ");
                    }
                });
    }

    private void delete(String id) {
        ApiClient.api.deleteManagedUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) goBack();
                else showError(errorMessage(response, "Không thể xóa người dùng"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError("Không thể kết nối đến máy chủ");
            }
        });
    }

    private String details(UserTenant user) {
        return "Tên đăng nhập: " + value(user.getUser_name())
                + "\nRole: " + value(user.getRole())
                + "\nTrạng thái: " + value(user.getStatus())
                + "\nHọ tên: " + value(user.getFull_name())
                + "\nSố điện thoại: " + value(user.getPhone())
                + "\nEmail: " + value(user.getEmail())
                + "\nĐịa chỉ: " + value(user.getPermanent_address())
                + "\nGhi chú: " + value(user.getAdditional_note());
    }

    private void goBack() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value;
    }

    private String errorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String message = new JSONObject(response.errorBody().string()).optString("message");
                if (!message.isBlank()) return message;
            }
        } catch (Exception ignored) { }
        return fallback;
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}
