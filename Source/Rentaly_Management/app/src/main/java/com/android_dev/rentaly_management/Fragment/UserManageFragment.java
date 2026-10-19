package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.UserListAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManageFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_USER_ID = "user_id";

    private static final String ROLE_TENANT = "TENANT";
    private static final String ROLE_LANDLORD = "LANDLORD";

    private static final String MESSAGE_CONNECTION_FAILED =
            "Không thể kết nối đến máy chủ";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<UserTenant> users = new ArrayList<>();

    private UserListAdapter adapter;
    private UserTenant selectedUser;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private View detailForm;
    private Spinner roleSpinner;

    // -------------------------------------------------------------------------
    // Request tracking
    // -------------------------------------------------------------------------

    private int userListRequestId = 0;

    // -------------------------------------------------------------------------
    // Fragment lifecycle
    // -------------------------------------------------------------------------

    public UserManageFragment() {
        // Required empty public constructor.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(
                R.layout.fragment_user_manage,
                container,
                false
        );

        bindViews();
        setupRoleSpinner();
        setupUserList();
        setupClickListeners();
        setupSearchListener();

        loadUsers("");

        return rootView;
    }

    private void bindViews() {
        searchInput = rootView.findViewById(
                R.id.user_search
        );

        loadingIndicator = rootView.findViewById(
                R.id.user_loading
        );

        detailForm = rootView.findViewById(
                R.id.user_detail_form
        );

        roleSpinner = rootView.findViewById(
                R.id.user_role
        );
    }

    // -------------------------------------------------------------------------
    // Role spinner
    // -------------------------------------------------------------------------

    private void setupRoleSpinner() {
        String[] roles = {
                ROLE_TENANT,
                ROLE_LANDLORD
        };

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );

        roleSpinner.setAdapter(roleAdapter);
    }

    @Nullable
    private String getSelectedRole() {
        if (roleSpinner == null
                || roleSpinner.getSelectedItem() == null) {
            return null;
        }

        return roleSpinner.getSelectedItem().toString();
    }

    private void selectRole(@Nullable String selectedRole) {
        if (roleSpinner == null || selectedRole == null) {
            return;
        }

        for (int index = 0; index < roleSpinner.getCount(); index++) {
            Object item = roleSpinner.getItemAtPosition(index);

            if (item != null
                    && selectedRole.equalsIgnoreCase(item.toString())) {
                roleSpinner.setSelection(index);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // User list
    // -------------------------------------------------------------------------

    private void setupUserList() {
        ListView userList = rootView.findViewById(
                R.id.user_list
        );

        adapter = new UserListAdapter(
                requireContext(),
                users
        );

        userList.setAdapter(adapter);

        userList.setOnItemClickListener(
                (parent, itemView, position, id) -> {
                    if (position < 0 || position >= users.size()) {
                        return;
                    }

                    openDetails(users.get(position));
                }
        );
    }

    private void refreshUserList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // -------------------------------------------------------------------------
    // Click listeners
    // -------------------------------------------------------------------------

    private void setupClickListeners() {
        rootView.findViewById(R.id.user_role_update)
                .setOnClickListener(
                        view -> {
                            String selectedRole = getSelectedRole();

                            if (selectedRole == null) {
                                showMessage("Vui lòng chọn role");
                                return;
                            }

                            updateRole(selectedUser, selectedRole);
                        }
                );

        rootView.findViewById(R.id.user_delete)
                .setOnClickListener(
                        view -> confirmDelete(selectedUser)
                );

        rootView.findViewById(R.id.button3)
                .setOnClickListener(
                        view -> showCreateDialog()
                );

        rootView.findViewById(R.id.user_announcements)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigate(R.id.announcementFragment)
                );
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence sequence,
                    int start,
                    int count,
                    int after
            ) {
                // No action required before text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence sequence,
                    int start,
                    int before,
                    int count
            ) {
                loadUsers(sequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required after text changes.
            }
        });
    }

    @NonNull
    private String getSearchTerm() {
        return searchInput == null
                ? ""
                : searchInput.getText().toString().trim();
    }

    // -------------------------------------------------------------------------
    // Load users
    // -------------------------------------------------------------------------

    private void loadUsers(@Nullable String term) {
        if (!isAdded() || rootView == null) {
            return;
        }

        String normalizedTerm = term == null
                ? ""
                : term.trim();

        int requestId = ++userListRequestId;

        setLoading(true);

        ApiClient.api.managedUsers(
                normalizedTerm.isEmpty() ? null : normalizedTerm
        ).enqueue(new Callback<List<UserTenant>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<UserTenant>> call,
                    @NonNull Response<List<UserTenant>> response
            ) {
                if (!isCurrentRequest(requestId)) {
                    return;
                }

                setLoading(false);

                if (!response.isSuccessful()
                        || response.body() == null) {
                    showMessage("Không thể tải danh sách người thuê");
                    return;
                }

                users.clear();
                users.addAll(response.body());

                refreshUserList();
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<UserTenant>> call,
                    @NonNull Throwable throwable
            ) {
                if (!isCurrentRequest(requestId)) {
                    return;
                }

                setLoading(false);
                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    private boolean isCurrentRequest(int requestId) {
        return isAdded()
                && rootView != null
                && requestId == userListRequestId;
    }

    private void setLoading(boolean isLoading) {
        if (loadingIndicator == null) {
            return;
        }

        loadingIndicator.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    // -------------------------------------------------------------------------
    // Create user dialog
    // -------------------------------------------------------------------------

    private void showCreateDialog() {
        View form = getLayoutInflater().inflate(
                R.layout.dialog_create_user,
                null
        );

        EditText usernameInput = form.findViewById(
                R.id.create_username
        );

        EditText passwordInput = form.findViewById(
                R.id.create_password
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Thêm người thuê")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Tạo",
                        (ignored, which) -> createUser(
                                usernameInput.getText().toString(),
                                passwordInput.getText().toString()
                        )
                )
                .create();

        dialog.show();

        // Apply the existing button colors.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                        requireContext().getResources().getColor(
                                R.color.primary
                        )
                );

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                        requireContext().getResources().getColor(
                                R.color.secondary
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Create user
    // -------------------------------------------------------------------------

    private void createUser(
            @NonNull String username,
            @NonNull String password
    ) {
        String normalizedUsername = username.trim();

        if (normalizedUsername.isEmpty()
                || password.trim().isEmpty()) {
            showMessage("Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }

        User request = new User(
                normalizedUsername,
                password
        );

        ApiClient.api.createManagedUser(request).enqueue(
                new Callback<UserTenant>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<UserTenant> call,
                            @NonNull Response<UserTenant> response
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        if (!response.isSuccessful()) {
                            showMessage("Không thể tạo người thuê");
                            return;
                        }

                        showMessage("Đã tạo người dùng");

                        loadUsers(getSearchTerm());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<UserTenant> call,
                            @NonNull Throwable throwable
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        showMessage(MESSAGE_CONNECTION_FAILED);
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Display user details in the inline form
    // -------------------------------------------------------------------------

    private void showDetails(@NonNull UserTenant user) {
        if (rootView == null) {
            return;
        }

        selectedUser = user;

        TextView detailsView = rootView.findViewById(
                R.id.user_detail
        );

        if (detailsView != null) {
            detailsView.setText(buildUserDetails(user));
        }

        selectRole(user.getRole());

        if (detailForm != null) {
            detailForm.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    private String buildUserDetails(@NonNull UserTenant user) {
        return "Tên đăng nhập: " + value(user.getUser_name())
                + "\nRole: " + value(user.getRole())
                + "\nHọ tên: " + value(user.getFull_name())
                + "\nSố điện thoại: " + value(user.getPhone())
                + "\nEmail: " + value(user.getEmail())
                + "\nẢnh đại diện: " + value(user.getAvatar_url())
                + "\nLoại giấy tờ: " + value(user.getIdentityType())
                + "\nSố giấy tờ: " + value(user.getIdentity_number())
                + "\nNgày cấp: " + value(user.getIdentity_issued_date())
                + "\nNơi cấp: " + value(user.getIdentity_issued_place())
                + "\nNgày sinh: " + value(user.getDate_of_birth())
                + "\nGiới tính: " + value(user.getGender())
                + "\nĐịa chỉ: " + value(user.getPermanent_address())
                + "\nLiên hệ khẩn cấp: "
                + value(user.getEmergency_contact_name())
                + " - " + value(user.getEmergency_contact_phone())
                + "\nGhi chú: " + value(user.getAdditional_note());
    }

    // -------------------------------------------------------------------------
    // Open user detail screen
    // -------------------------------------------------------------------------

    private void openDetails(@NonNull UserTenant user) {
        if (user.getUser_id() == null) {
            showMessage("Không xác định được người dùng");
            return;
        }

        Bundle arguments = new Bundle();

        arguments.putString(
                ARG_USER_ID,
                user.getUser_id().toString()
        );

        NavHostFragment.findNavController(this).navigate(
                R.id.userDetailFragment,
                arguments
        );
    }

    // -------------------------------------------------------------------------
    // Update user role
    // -------------------------------------------------------------------------

    private void updateRole(
            @Nullable UserTenant user,
            @NonNull String selectedRole
    ) {
        if (user == null || user.getUser_id() == null) {
            showMessage("Vui lòng chọn người dùng");
            return;
        }

        if (!ROLE_TENANT.equals(selectedRole)
                && !ROLE_LANDLORD.equals(selectedRole)) {
            showMessage("Vai trò không hợp lệ");
            return;
        }

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                selectedRole
        );

        ApiClient.api.updateManagedUserRole(
                user.getUser_id().toString(),
                request
        ).enqueue(new Callback<UserTenant>() {

            @Override
            public void onResponse(
                    @NonNull Call<UserTenant> call,
                    @NonNull Response<UserTenant> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    showMessage(
                            errorMessage(
                                    response,
                                    "Không thể cập nhật role"
                            )
                    );
                    return;
                }

                showMessage("Đã cập nhật role");
                loadUsers(getSearchTerm());
            }

            @Override
            public void onFailure(
                    @NonNull Call<UserTenant> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Delete user
    // -------------------------------------------------------------------------

    private void confirmDelete(@Nullable UserTenant user) {
        if (user == null || user.getUser_id() == null) {
            showMessage("Vui lòng chọn người dùng");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa người thuê?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteUser(user)
                )
                .show();
    }

    private void deleteUser(@NonNull UserTenant user) {
        if (user.getUser_id() == null) {
            showMessage("Không xác định được người dùng");
            return;
        }

        ApiClient.api.deleteManagedUser(
                user.getUser_id().toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    String fallback = response.code() == 409
                            ? "Không thể xóa vì tenant đã có hợp đồng"
                            : "Không thể xóa người thuê";

                    showMessage(errorMessage(response, fallback));
                    return;
                }

                selectedUser = null;

                showMessage("Đã xóa người dùng");
                loadUsers(getSearchTerm());
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------------------------

    @NonNull
    private String value(@Nullable String value) {
        return value == null || value.trim().isEmpty()
                ? "Chưa cập nhật"
                : value;
    }

    // -------------------------------------------------------------------------
    // API error handling
    // -------------------------------------------------------------------------

    @NonNull
    private String errorMessage(
            @NonNull Response<?> response,
            @NonNull String fallback
    ) {
        try {
            if (response.errorBody() == null) {
                return fallback;
            }

            String body = response.errorBody().string();

            if (body.trim().isEmpty()) {
                return fallback;
            }

            String message = new JSONObject(body)
                    .optString("message", "");

            return message.trim().isEmpty()
                    ? fallback
                    : message;

        } catch (Exception ignored) {
            return fallback;
        }
    }

    // -------------------------------------------------------------------------
    // User messages
    // -------------------------------------------------------------------------

    private void showMessage(@NonNull String message) {
        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // -------------------------------------------------------------------------
    // Fragment cleanup
    // -------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        // Ignore pending user-list responses after the view is destroyed.
        userListRequestId++;

        rootView = null;
        searchInput = null;
        loadingIndicator = null;
        detailForm = null;
        roleSpinner = null;
        adapter = null;
        selectedUser = null;

        super.onDestroyView();
    }
}