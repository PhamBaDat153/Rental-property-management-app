package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.MaintenanceAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private static final String UNKNOWN_DESCRIPTION = "Chưa có mô tả";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Maintenance> maintenanceRequests = new ArrayList<>();

    private MaintenanceAdapter maintenanceAdapter;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private ProgressBar loadingIndicator;
    private TextView emptyState;

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
                R.layout.fragment_maintenance,
                container,
                false
        );

        bindViews();
        setupMaintenanceList();
        setupClickListeners();

        loadMaintenanceRequests();

        return rootView;
    }

    private void bindViews() {
        loadingIndicator = rootView.findViewById(
                R.id.maintenance_loading
        );

        emptyState = rootView.findViewById(
                R.id.maintenance_empty
        );
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.maintenance_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );
    }

    // -------------------------------------------------------------------------
    // Maintenance list
    // -------------------------------------------------------------------------

    private void setupMaintenanceList() {
        ListView maintenanceList = rootView.findViewById(
                R.id.maintenance_list
        );

        maintenanceAdapter = new MaintenanceAdapter(
                requireContext(),
                maintenanceRequests,
                this::getStatusLabel
        );

        maintenanceList.setAdapter(maintenanceAdapter);
    }

    private void renderMaintenanceRequests() {
        if (maintenanceAdapter == null || rootView == null) {
            return;
        }

        maintenanceAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyState == null) {
            return;
        }

        boolean isEmpty = maintenanceRequests.isEmpty();

        emptyState.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );
    }

    // -------------------------------------------------------------------------
    // Load maintenance requests
    // -------------------------------------------------------------------------

    private void loadMaintenanceRequests() {
        setLoading(true);

        ApiClient.api.maintenance(null, null, null).enqueue(
                new Callback<List<Maintenance>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Maintenance>> call,
                            @NonNull Response<List<Maintenance>> response
                    ) {
                        if (rootView == null) {
                            return;
                        }

                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải yêu cầu bảo trì"
                            );
                            return;
                        }

                        maintenanceRequests.clear();
                        maintenanceRequests.addAll(response.body());

                        renderMaintenanceRequests();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Maintenance>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (rootView == null) {
                            return;
                        }

                        setLoading(false);

                        showMessage(
                                "Không thể kết nối đến máy chủ"
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Status and description formatting
    // -------------------------------------------------------------------------

    private String getStatusLabel(@Nullable String status) {
        if (STATUS_PROCESSING.equalsIgnoreCase(status)) {
            return "Đang xử lý";
        }

        if (STATUS_COMPLETED.equalsIgnoreCase(status)) {
            return "Đã hoàn tất";
        }

        return "Đang chờ";
    }

    private String getDescription(
            @Nullable String description
    ) {
        if (description == null || description.trim().isEmpty()) {
            return UNKNOWN_DESCRIPTION;
        }

        return description;
    }

    // -------------------------------------------------------------------------
    // Loading and messages
    // -------------------------------------------------------------------------

    private void setLoading(boolean isLoading) {
        if (loadingIndicator == null) {
            return;
        }

        loadingIndicator.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

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
        super.onDestroyView();

        rootView = null;
        loadingIndicator = null;
        emptyState = null;
        maintenanceAdapter = null;
    }
}