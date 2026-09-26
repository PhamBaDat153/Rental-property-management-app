package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.android_dev.rentaly_management.R;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;
import java.io.IOException;

public class UserManageFragment extends Fragment {
    private final List<UserTenant> users = new ArrayList<>();
    private UserListAdapter adapter;
    private EditText search;
    private ProgressBar loading;
    private View detailForm;
    private Spinner role;
    private UserTenant selectedUser;

    public UserManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_manage, container, false);
        search = view.findViewById(R.id.user_search);
        loading = view.findViewById(R.id.user_loading);
        detailForm = view.findViewById(R.id.user_detail_form);
        role = view.findViewById(R.id.user_role);
        String[] roles = {"TENANT", "LANDLORD"};
        role.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles));
        ListView list = view.findViewById(R.id.user_list);
        adapter = new UserListAdapter(requireContext(), users);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, itemView, position, id) -> openDetails(users.get(position)));
        view.findViewById(R.id.user_role_update).setOnClickListener(v -> updateRole(selectedUser,
                roles[role.getSelectedItemPosition()]));
        view.findViewById(R.id.user_delete).setOnClickListener(v -> confirmDelete(selectedUser));
        view.findViewById(R.id.button3).setOnClickListener(v -> showCreateDialog());
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { loadUsers(s.toString()); }
            public void afterTextChanged(android.text.Editable s) { }
        });
        loadUsers("");
        return view;
    }

    private void loadUsers(String term) {
        loading.setVisibility(View.VISIBLE);
        ApiClient.api.managedUsers(term.trim().isEmpty() ? null : term.trim()).enqueue(new Callback<List<UserTenant>>() {
            public void onResponse(Call<List<UserTenant>> call, Response<List<UserTenant>> response) {
                loading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Không thể tải danh sách người thuê");
                    return;
                }
                users.clear();
                users.addAll(response.body());
                adapter.notifyDataSetChanged();
            }
            public void onFailure(Call<List<UserTenant>> call, Throwable t) {
                loading.setVisibility(View.GONE);
                showError("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void showCreateDialog() {
        View form = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        EditText username = form.findViewById(R.id.create_username);
        EditText password = form.findViewById(R.id.create_password);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Thêm người thuê")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (ignored, which) -> createUser(username.getText().toString(), password.getText().toString()))
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.secondary));
    }

    private void createUser(String username, String password) {
        if (username.trim().isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }
        ApiClient.api.createManagedUser(new User(username.trim(), password)).enqueue(new Callback<UserTenant>() {
            public void onResponse(Call<UserTenant> call, Response<UserTenant> response) {
                if (response.isSuccessful()) { loadUsers(search.getText().toString()); }
                else showError("Không thể tạo người thuê");
            }
            public void onFailure(Call<UserTenant> call, Throwable t) { showError("Không thể kết nối đến máy chủ"); }
        });
    }

    private void showDetails(UserTenant user) {
        String details = "Tên đăng nhập: " + value(user.getUser_name()) +
                "\nRole: " + value(user.getRole()) +
                "\nHọ tên: " + value(user.getFull_name()) +
                "\nSố điện thoại: " + value(user.getPhone()) +
                "\nEmail: " + value(user.getEmail()) +
                "\nẢnh đại diện: " + value(user.getAvatar_url()) +
                "\nLoại giấy tờ: " + value(user.getIdentityType()) +
                "\nSố giấy tờ: " + value(user.getIdentity_number()) +
                "\nNgày cấp: " + value(user.getIdentity_issued_date()) +
                "\nNơi cấp: " + value(user.getIdentity_issued_place()) +
                "\nNgày sinh: " + value(user.getDate_of_birth()) +
                "\nGiới tính: " + value(user.getGender()) +
                "\nĐịa chỉ: " + value(user.getPermanent_address()) +
                "\nLiên hệ khẩn cấp: " + value(user.getEmergency_contact_name()) +
                " - " + value(user.getEmergency_contact_phone()) +
                "\nGhi chú: " + value(user.getAdditional_note());
        selectedUser = user;
        ((TextView) requireView().findViewById(R.id.user_detail)).setText(details);
        for (int i = 0; i < role.getCount(); i++) {
            if (role.getItemAtPosition(i).toString().equalsIgnoreCase(user.getRole())) role.setSelection(i);
        }
        detailForm.setVisibility(View.VISIBLE);
    }

    private void openDetails(UserTenant user) {
        Bundle args = new Bundle();
        args.putString("user_id", user.getUser_id().toString());
        NavHostFragment.findNavController(this).navigate(R.id.userDetailFragment, args);
    }

    private void updateRole(UserTenant user, String role) {
        if (user == null) return;
        ApiClient.api.updateManagedUserRole(user.getUser_id().toString(), new UserRoleUpdateRequest(role))
                .enqueue(new Callback<UserTenant>() {
                    public void onResponse(Call<UserTenant> call, Response<UserTenant> response) {
                        if (response.isSuccessful()) {
                            showError("Đã cập nhật role");
                            loadUsers(search.getText().toString());
                        } else showError(errorMessage(response, "Không thể cập nhật role"));
                    }
                    public void onFailure(Call<UserTenant> call, Throwable t) {
                        showError("Không thể kết nối đến máy chủ");
                    }
                });
    }

    private void confirmDelete(UserTenant user) {
        new AlertDialog.Builder(requireContext()).setTitle("Xóa người thuê?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user)).show();
    }

    private void deleteUser(UserTenant user) {
        ApiClient.api.deleteManagedUser(user.getUser_id().toString()).enqueue(new Callback<Void>() {
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadUsers(search.getText().toString());
                else showError(errorMessage(response, response.code() == 409
                        ? "Không thể xóa vì tenant đã có hợp đồng" : "Không thể xóa người thuê"));
            }
            public void onFailure(Call<Void> call, Throwable t) { showError("Không thể kết nối đến máy chủ"); }
        });
    }

    private String value(String value) { return value == null || value.isBlank() ? "Chưa cập nhật" : value; }
    private String errorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String message = new JSONObject(response.errorBody().string()).optString("message");
                if (!message.isBlank()) return message;
            }
        } catch (Exception ignored) { }
        return fallback;
    }
    private void showError(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
