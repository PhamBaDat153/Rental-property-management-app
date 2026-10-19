package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.ServiceAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.DTO.ServiceRequest;
import com.android_dev.rentaly_management.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCatalogFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String MESSAGE_LOAD_FAILED =
            "Không thể tải danh mục dịch vụ";

    private static final String MESSAGE_CONNECTION_FAILED =
            "Không thể kết nối đến máy chủ";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Service> services = new ArrayList<>();

    private ServiceAdapter serviceAdapter;

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
                R.layout.fragment_service_catalog,
                container,
                false
        );

        bindViews();
        setupServiceList();
        setupClickListeners();

        loadServices();

        return rootView;
    }

    private void bindViews() {
        loadingIndicator = rootView.findViewById(
                R.id.service_loading
        );

        emptyState = rootView.findViewById(
                R.id.service_empty
        );
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.service_catalog_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );

        rootView.findViewById(R.id.service_add)
                .setOnClickListener(
                        view -> showServiceForm(null)
                );
    }

    // -------------------------------------------------------------------------
    // Service list
    // -------------------------------------------------------------------------

    private void setupServiceList() {
        ListView serviceList = rootView.findViewById(
                R.id.service_list
        );

        serviceAdapter = new ServiceAdapter(
                requireContext(),
                services,
                this::getStatusLabel
        );

        serviceList.setAdapter(serviceAdapter);

        serviceList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    if (position < 0 || position >= services.size()) {
                        return;
                    }

                    showServiceActions(services.get(position));
                }
        );
    }

    private void renderServices() {
        if (serviceAdapter == null || rootView == null) {
            return;
        }

        serviceAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyState == null) {
            return;
        }

        boolean isEmpty = services.isEmpty();

        emptyState.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );
    }

    // -------------------------------------------------------------------------
    // Load services
    // -------------------------------------------------------------------------

    private void loadServices() {
        setLoading(true);

        ApiClient.api.services().enqueue(
                new Callback<List<Service>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Service>> call,
                            @NonNull Response<List<Service>> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(MESSAGE_LOAD_FAILED);
                            return;
                        }

                        services.clear();
                        services.addAll(response.body());

                        renderServices();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Service>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        setLoading(false);
                        showMessage(MESSAGE_CONNECTION_FAILED);
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Service actions
    // -------------------------------------------------------------------------

    private void showServiceActions(@NonNull Service service) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getValue(service.name))
                .setItems(
                        new String[]{
                                "Sửa dịch vụ",
                                "Xóa dịch vụ"
                        },
                        (dialog, selectedAction) -> {
                            if (selectedAction == 0) {
                                showServiceForm(service);
                            } else if (selectedAction == 1) {
                                confirmServiceDelete(service);
                            }
                        }
                )
                .show();
    }

    // -------------------------------------------------------------------------
    // Delete service
    // -------------------------------------------------------------------------

    private void confirmServiceDelete(@NonNull Service service) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa dịch vụ?")
                .setMessage(
                        "Dịch vụ có thể đang được gán cho các phòng."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteService(service)
                )
                .show();
    }

    private void deleteService(@NonNull Service service) {
        if (service.service_id == null) {
            showMessage("Không xác định được dịch vụ");
            return;
        }

        ApiClient.api.deleteService(
                service.service_id.toString()
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
                    showMessage(
                            response.code() == 409
                                    ? "Không thể xóa dịch vụ đang được sử dụng"
                                    : "Không thể xóa dịch vụ"
                    );
                    return;
                }

                showMessage("Đã xóa dịch vụ");
                loadServices();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Service form
    // -------------------------------------------------------------------------

    private void showServiceForm(@Nullable Service serviceToEdit) {
        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_service,
                null
        );

        ServiceForm form = new ServiceForm(formView);

        populateServiceForm(form, serviceToEdit);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        serviceToEdit == null
                                ? "Thêm dịch vụ"
                                : "Sửa dịch vụ"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveService(
                                        form,
                                        serviceToEdit,
                                        dialog
                                )
                        )
        );

        dialog.show();
    }

    /**
     * Holds the input fields used by the service form.
     */
    private static final class ServiceForm {

        final EditText nameInput;
        final EditText unitInput;
        final EditText calculationInput;
        final EditText priceInput;

        final RadioButton availableButton;
        final RadioButton unavailableButton;

        ServiceForm(View root) {
            nameInput = root.findViewById(
                    R.id.service_form_name
            );

            unitInput = root.findViewById(
                    R.id.service_form_unit
            );

            calculationInput = root.findViewById(
                    R.id.service_form_calculation
            );

            priceInput = root.findViewById(
                    R.id.service_form_price
            );

            availableButton = root.findViewById(
                    R.id.service_form_available
            );

            unavailableButton = root.findViewById(
                    R.id.service_form_unavailable
            );
        }
    }

    private void populateServiceForm(
            @NonNull ServiceForm form,
            @Nullable Service service
    ) {
        if (service != null) {
            form.nameInput.setText(getValue(service.name));
            form.unitInput.setText(getValue(service.unit));

            form.calculationInput.setText(
                    getValue(service.calculation_method)
            );

            form.priceInput.setText(
                    getValue(service.default_unit_price)
            );
        } else {
            form.nameInput.setText("");
            form.unitInput.setText("");
            form.calculationInput.setText("");
            form.priceInput.setText("");
        }

        String status = service == null
                ? STATUS_AVAILABLE
                : service.room_status;

        boolean isUnavailable = STATUS_UNAVAILABLE.equalsIgnoreCase(
                status
        );

        form.unavailableButton.setChecked(isUnavailable);
        form.availableButton.setChecked(!isUnavailable);
    }

    // -------------------------------------------------------------------------
    // Save service
    // -------------------------------------------------------------------------

    private void saveService(
            @NonNull ServiceForm form,
            @Nullable Service serviceToEdit,
            @NonNull AlertDialog dialog
    ) {
        try {
            String name = getRequiredText(form.nameInput);
            String unit = getRequiredText(form.unitInput);
            String calculation = getRequiredText(
                    form.calculationInput
            );

            if (name.isEmpty() || unit.isEmpty() || calculation.isEmpty()) {
                showMessage(
                        "Vui lòng nhập tên, đơn vị và cách tính dịch vụ"
                );
                return;
            }

            BigDecimal price = parseRequiredPrice(
                    form.priceInput.getText().toString()
            );

            if (price.signum() < 0) {
                showMessage("Đơn giá không được âm");
                return;
            }

            String status = form.availableButton.isChecked()
                    ? STATUS_AVAILABLE
                    : STATUS_UNAVAILABLE;

            ServiceRequest request = new ServiceRequest(
                    name,
                    unit,
                    calculation,
                    price,
                    status
            );

            submitService(
                    request,
                    serviceToEdit,
                    dialog
            );

        } catch (NumberFormatException exception) {
            showMessage("Vui lòng nhập đơn giá hợp lệ");
        } catch (Exception exception) {
            showMessage("Vui lòng kiểm tra thông tin dịch vụ");
        }
    }

    private void submitService(
            @NonNull ServiceRequest request,
            @Nullable Service serviceToEdit,
            @NonNull AlertDialog dialog
    ) {
        if (serviceToEdit != null && serviceToEdit.service_id == null) {
            showMessage("Không xác định được dịch vụ");
            return;
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        Call<Service> call;

        if (serviceToEdit == null) {
            call = ApiClient.api.createService(request);
        } else {
            call = ApiClient.api.updateService(
                    serviceToEdit.service_id.toString(),
                    request
            );
        }

        call.enqueue(new Callback<Service>() {

            @Override
            public void onResponse(
                    @NonNull Call<Service> call,
                    @NonNull Response<Service> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);

                    showMessage("Không thể lưu dịch vụ");
                    return;
                }

                dialog.dismiss();

                showMessage("Đã lưu dịch vụ");
                loadServices();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Service> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Formatting and validation helpers
    // -------------------------------------------------------------------------

    @NonNull
    private String getStatusLabel(@Nullable String status) {
        return STATUS_AVAILABLE.equalsIgnoreCase(status)
                ? "Đang khả dụng"
                : "Không khả dụng";
    }

    @NonNull
    private String getValue(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    @NonNull
    private String getRequiredText(@NonNull EditText input) {
        return input.getText().toString().trim();
    }

    @NonNull
    private BigDecimal parseRequiredPrice(@NonNull String value) {
        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new NumberFormatException("Price is required");
        }

        return new BigDecimal(normalizedValue);
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
        rootView = null;
        loadingIndicator = null;
        emptyState = null;
        serviceAdapter = null;

        super.onDestroyView();
    }
}