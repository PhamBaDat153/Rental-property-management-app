package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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

    private static final String MESSAGE_CONNECTION_FAILED =
            "Không thể kết nối đến máy chủ";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<UserTenant> users = new ArrayList<>();

    private UserListAdapter adapter;
    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private View stateView;
    private TextView stateMessage;
    private TextView resultCount;
    private android.widget.Button stateAction;

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

        stateView = rootView.findViewById(R.id.user_state);
        stateMessage = rootView.findViewById(R.id.user_state_message);
        resultCount = rootView.findViewById(R.id.user_result_count);
        stateAction = rootView.findViewById(R.id.user_state_action);
    }

    // -------------------------------------------------------------------------
    // Role spinner
    // -------------------------------------------------------------------------

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
                    showListState("Không thể tải danh sách người dùng.", "Thử lại");
                    return;
                }

                users.clear();
                users.addAll(response.body());

                refreshUserList();
                showListState(normalizedTerm.isEmpty() && users.isEmpty()
                        ? "Chưa có người dùng\nTạo tài khoản người dùng đầu tiên để bắt đầu quản lý."
                        : users.isEmpty()
                        ? "Không tìm thấy người dùng phù hợp."
                        : null,
                        users.isEmpty() && normalizedTerm.isEmpty()
                                ? "Thêm người dùng"
                                : users.isEmpty() ? "Xóa tìm kiếm" : null);
                if (resultCount != null && !users.isEmpty()) {
                    resultCount.setVisibility(View.VISIBLE);
                    resultCount.setText(users.size() + " người dùng");
                }
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
                showListState("Không thể kết nối đến máy chủ.", "Thử lại");
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
        if (isLoading && stateView != null) {
            stateView.setVisibility(View.GONE);
        }
    }

    private void showListState(@Nullable String message, @Nullable String action) {
        if (stateView == null || stateMessage == null || stateAction == null) {
            return;
        }

        boolean visible = message != null;
        stateView.setVisibility(visible ? View.VISIBLE : View.GONE);
        stateMessage.setText(visible ? message : "");
        stateAction.setVisibility(action == null ? View.GONE : View.VISIBLE);
        if (action != null) {
            stateAction.setText(action);
            stateAction.setOnClickListener(view -> {
                if ("Xóa tìm kiếm".contentEquals(action)) {
                    searchInput.setText("");
                } else if ("Thêm người dùng".contentEquals(action)) {
                    showCreateDialog();
                } else {
                    loadUsers(getSearchTerm());
                }
            });
        }
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
                .setTitle("Thêm người dùng")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                usernameInput.setError(null);
                passwordInput.setError(null);
                if (username.isEmpty()) {
                    usernameInput.setError("Nhập tên đăng nhập");
                    usernameInput.requestFocus();
                    return;
                }
                if (password.trim().isEmpty()) {
                    passwordInput.setError("Nhập mật khẩu");
                    passwordInput.requestFocus();
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Đang tạo...");
                createUser(username, password, dialog);
            });
        });

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
            @NonNull String password,
            @NonNull AlertDialog dialog
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
                            resetCreateDialog(dialog);
                            showMessage("Không thể tạo người dùng");
                            return;
                        }

                        dialog.dismiss();
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

                        resetCreateDialog(dialog);
                        showMessage(MESSAGE_CONNECTION_FAILED);
                    }
                }
        );
    }

    private void resetCreateDialog(@NonNull AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Tạo");
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
    // Delete user
    // -------------------------------------------------------------------------

    /* Delete is handled from UserDetailFragment. */
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
        stateView = null;
        stateMessage = null;
        resultCount = null;
        stateAction = null;
        adapter = null;
        super.onDestroyView();
    }
}