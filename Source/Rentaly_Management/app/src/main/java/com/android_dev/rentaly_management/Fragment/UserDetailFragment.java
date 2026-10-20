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

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_USER_ID = "user_id";

    private static final String ROLE_TENANT = "TENANT";
    private static final String ROLE_LANDLORD = "LANDLORD";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    private static final String GENDER_MALE = "MALE";
    private static final String GENDER_FEMALE = "FEMALE";
    private static final String GENDER_OTHER = "OTHER";

    private static final String MESSAGE_CONNECTION_FAILED =
            "Không thể kết nối đến máy chủ";

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private RadioGroup roleGroup;
    private RadioGroup statusGroup;

    // -------------------------------------------------------------------------
    // Fragment lifecycle
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(
                R.layout.fragment_user_detail,
                container,
                false
        );

        bindViews();
        setupClickListeners();

        String userId = requireArguments().getString(ARG_USER_ID);

        if (isBlank(userId)) {
            showMessage("Không tìm thấy mã người dùng");
            return rootView;
        }

        loadUser(userId);

        return rootView;
    }

    private void bindViews() {
        roleGroup = rootView.findViewById(
                R.id.user_detail_role
        );

        statusGroup = rootView.findViewById(
                R.id.user_detail_status
        );
    }

    // -------------------------------------------------------------------------
    // Click listeners
    // -------------------------------------------------------------------------

    private void setupClickListeners() {
        rootView.findViewById(R.id.user_detail_back)
                .setOnClickListener(
                        view -> goBack()
                );

        rootView.findViewById(R.id.user_detail_update)
                .setOnClickListener(
                        view -> updateUser()
                );

        rootView.findViewById(R.id.user_detail_delete)
                .setOnClickListener(
                        view -> deleteUser()
                );
    }

    // -------------------------------------------------------------------------
    // Load user
    // -------------------------------------------------------------------------

    private void loadUser(@NonNull String userId) {
        ApiClient.api.managedUser(userId).enqueue(
                new Callback<UserTenant>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<UserTenant> call,
                            @NonNull Response<UserTenant> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải thông tin người dùng"
                            );
                            return;
                        }

                        displayUser(response.body());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<UserTenant> call,
                            @NonNull Throwable throwable
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        showMessage(MESSAGE_CONNECTION_FAILED);
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Display user information
    // -------------------------------------------------------------------------

    private void displayUser(@NonNull UserTenant user) {
        displayPersonalInformation(user);
        displayIdentityInformation(user);
        displayEmergencyContact(user);
        displayAdditionalInformation(user);
        selectUserOptions(user);
    }

    private void displayPersonalInformation(
            @NonNull UserTenant user
    ) {
        setText(
                R.id.user_full_name,
                "Họ và tên: " + value(user.getFull_name())
        );

        setText(
                R.id.user_date_of_birth,
                "Ngày sinh: " + value(user.getDate_of_birth())
        );

        setText(
                R.id.user_phone,
                "Số điện thoại: " + value(user.getPhone())
        );

        setText(
                R.id.user_email,
                "Email: " + value(user.getEmail())
        );

        setText(
                R.id.user_username,
                "Tên đăng nhập: " + value(user.getUser_name())
        );

        setText(
                R.id.user_account_summary,
                value(user.getRole()) + " · " + value(user.getStatus())
        );

        setText(
                R.id.user_address,
                "Địa chỉ: " + value(user.getPermanent_address())
        );
    }

    private void displayIdentityInformation(
            @NonNull UserTenant user
    ) {
        setText(
                R.id.user_identity_type,
                "Loại giấy tờ: " + value(user.getIdentityType())
        );

        setText(
                R.id.user_identity_number,
                "Số giấy tờ: " + value(user.getIdentity_number())
        );

        setText(
                R.id.user_identity_issued_date,
                "Ngày cấp: " + value(user.getIdentity_issued_date())
        );

        setText(
                R.id.user_identity_issued_place,
                "Nơi cấp: " + value(user.getIdentity_issued_place())
        );
    }

    private void displayEmergencyContact(
            @NonNull UserTenant user
    ) {
        setText(
                R.id.user_emergency_name,
                "Họ tên: " + value(user.getEmergency_contact_name())
        );

        setText(
                R.id.user_emergency_phone,
                "Số điện thoại: "
                        + value(user.getEmergency_contact_phone())
        );
    }

    private void displayAdditionalInformation(
            @NonNull UserTenant user
    ) {
        setText(
                R.id.user_note,
                "Ghi chú: " + value(user.getAdditional_note())
        );
    }

    // -------------------------------------------------------------------------
    // Select user options
    // -------------------------------------------------------------------------

    private void selectUserOptions(@NonNull UserTenant user) {
        selectGender(user.getGender());
        selectRole(user.getRole());
        selectStatus(user.getStatus());
    }

    private void selectGender(@Nullable String gender) {
        int genderId;

        if (GENDER_MALE.equalsIgnoreCase(gender)) {
            genderId = R.id.gender_male;
        } else if (GENDER_FEMALE.equalsIgnoreCase(gender)) {
            genderId = R.id.gender_female;
        } else {
            genderId = R.id.gender_other;
        }

        RadioButton genderButton = rootView.findViewById(genderId);

        if (genderButton != null) {
            genderButton.setChecked(true);
        }
    }

    private void selectRole(@Nullable String role) {
        int roleId = ROLE_TENANT.equalsIgnoreCase(role)
                ? R.id.role_tenant
                : R.id.role_landlord;

        RadioButton roleButton = rootView.findViewById(roleId);

        if (roleButton != null) {
            roleButton.setChecked(true);
        }
    }

    private void selectStatus(@Nullable String status) {
        int statusId = STATUS_ACTIVE.equalsIgnoreCase(status)
                ? R.id.status_active
                : R.id.status_inactive;

        RadioButton statusButton = rootView.findViewById(statusId);

        if (statusButton != null) {
            statusButton.setChecked(true);
        }
    }

    // -------------------------------------------------------------------------
    // Update user
    // -------------------------------------------------------------------------

    private void updateUser() {
        String userId = getUserId();

        if (userId == null) {
            showMessage("Không tìm thấy mã người dùng");
            return;
        }

        if (roleGroup == null || statusGroup == null) {
            return;
        }

        int selectedRoleId = roleGroup.getCheckedRadioButtonId();
        int selectedStatusId = statusGroup.getCheckedRadioButtonId();

        if (selectedRoleId == -1 || selectedStatusId == -1) {
            showMessage("Vui lòng chọn role và trạng thái");
            return;
        }

        String selectedRole = selectedRoleId == R.id.role_landlord
                ? ROLE_LANDLORD
                : ROLE_TENANT;

        String selectedStatus = selectedStatusId == R.id.status_active
                ? STATUS_ACTIVE
                : STATUS_INACTIVE;

        UserUpdateRequest request = new UserUpdateRequest(
                selectedRole,
                selectedStatus
        );

        submitUserUpdate(userId, request);
    }

    private void submitUserUpdate(
            @NonNull String userId,
            @NonNull UserUpdateRequest request
    ) {
        ApiClient.api.updateManagedUser(userId, request).enqueue(
                new Callback<UserTenant>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<UserTenant> call,
                            @NonNull Response<UserTenant> response
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            showMessage("Đã cập nhật người dùng");
                            return;
                        }

                        showMessage(
                                errorMessage(
                                        response,
                                        "Không thể cập nhật người dùng"
                                )
                        );
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
    // Delete user
    // -------------------------------------------------------------------------

    private void deleteUser() {
        String userId = getUserId();

        if (userId == null) {
            showMessage("Không tìm thấy mã người dùng");
            return;
        }

        ApiClient.api.deleteManagedUser(userId).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            goBack();
                            return;
                        }

                        showMessage(
                                errorMessage(
                                        response,
                                        "Không thể xóa người dùng"
                                )
                        );
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
                }
        );
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void goBack() {
        if (!isAdded()) {
            return;
        }

        NavHostFragment.findNavController(this).navigateUp();
    }

    // -------------------------------------------------------------------------
    // View helpers
    // -------------------------------------------------------------------------

    private void setText(
            int viewId,
            @NonNull String text
    ) {
        if (rootView == null) {
            return;
        }

        TextView textView = rootView.findViewById(viewId);

        if (textView != null) {
            textView.setText(text);
        }
    }

    // -------------------------------------------------------------------------
    // Data helpers
    // -------------------------------------------------------------------------

    @Nullable
    private String getUserId() {
        Bundle arguments = getArguments();

        if (arguments == null) {
            return null;
        }

        String userId = arguments.getString(ARG_USER_ID);

        return isBlank(userId) ? null : userId;
    }

    @NonNull
    private String value(@Nullable String value) {
        return isBlank(value)
                ? "Chưa cập nhật"
                : value;
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
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

            String responseBody = response.errorBody().string();

            if (isBlank(responseBody)) {
                return fallback;
            }

            String message = new JSONObject(responseBody)
                    .optString("message", "");

            return isBlank(message) ? fallback : message;

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
        rootView = null;
        roleGroup = null;
        statusGroup = null;

        super.onDestroyView();
    }
}