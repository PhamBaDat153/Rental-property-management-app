package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.ContractTenantAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.ContractTenantRequest;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.RentalContractRequest;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình xem, chỉnh sửa hợp đồng và quản lý người thuê.
 */
public class ContractDetailFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_CONTRACT_ID = "contract_id";

    private static final int DOCUMENT_PICKER_REQUEST = 703;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    private static final String UNKNOWN_VALUE = "Chưa cập nhật";

    private static final String MIME_PDF = "application/pdf";

    private static final String MIME_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String EMPTY_TENANTS_MESSAGE =
            "Chưa có người thuê trong hợp đồng";

    private static final String TENANT_LOAD_ERROR =
            "Không thể tải danh sách người thuê";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private RentalContract contract;

    private final List<Room> rooms = new ArrayList<>();
    private final List<ContractTenant> assignments = new ArrayList<>();
    private final List<UserTenant> tenants = new ArrayList<>();

    private ContractTenantAdapter tenantAdapter;

    private Uri selectedDocument;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

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
                R.layout.fragment_contract_detail,
                container,
                false
        );

        setupClickListeners();

        String contractId = requireArguments()
                .getString(ARG_CONTRACT_ID);

        loadContract(contractId);

        return rootView;
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.contract_detail_back)
                .setOnClickListener(view ->
                        NavHostFragment.findNavController(this).navigateUp()
                );

        rootView.findViewById(R.id.contract_detail_edit)
                .setOnClickListener(view -> editContract());

        rootView.findViewById(R.id.contract_detail_delete)
                .setOnClickListener(view -> confirmContractDelete());

        rootView.findViewById(R.id.contract_detail_view_document)
                .setOnClickListener(view -> openDocument());

        rootView.findViewById(R.id.contract_detail_choose_document)
                .setOnClickListener(view -> chooseDocument());

        rootView.findViewById(R.id.contract_detail_start)
                .setOnClickListener(view ->
                        chooseDate(R.id.contract_detail_start)
                );

        rootView.findViewById(R.id.contract_detail_end)
                .setOnClickListener(view ->
                        chooseDate(R.id.contract_detail_end)
                );

        rootView.findViewById(R.id.contract_detail_signed)
                .setOnClickListener(view ->
                        chooseDate(R.id.contract_detail_signed)
                );

        rootView.findViewById(R.id.contract_detail_terminated)
                .setOnClickListener(view ->
                        chooseDate(R.id.contract_detail_terminated)
                );

        rootView.findViewById(R.id.contract_tenant_add)
                .setOnClickListener(view -> showTenantForm(null));
    }

    // -------------------------------------------------------------------------
    // Load contract
    // -------------------------------------------------------------------------

    /**
     * Tải thông tin hợp đồng trước khi tải các dữ liệu liên quan.
     */
    private void loadContract(@Nullable String contractId) {
        if (contractId == null || contractId.trim().isEmpty()) {
            showMessage("Không tìm thấy mã hợp đồng");
            return;
        }

        ApiClient.api.contract(contractId).enqueue(
                new Callback<RentalContract>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RentalContract> call,
                            @NonNull Response<RentalContract> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải hợp đồng");
                            return;
                        }

                        contract = response.body();

                        bindContractDetails();
                        loadRooms();
                        loadTenants();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RentalContract> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    /**
     * Tải danh sách phòng để hiển thị thông tin phòng của hợp đồng.
     */
    private void loadRooms() {
        ApiClient.api.rooms().enqueue(
                new Callback<List<Room>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Room>> call,
                            @NonNull Response<List<Room>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        rooms.clear();
                        rooms.addAll(response.body());

                        bindContractDetails();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Room>> call,
                            @NonNull Throwable throwable
                    ) {
                        // Không chặn việc hiển thị các thông tin hợp đồng khác.
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Load contract tenants
    // -------------------------------------------------------------------------

    /**
     * Tải danh sách người thuê thuộc hợp đồng hiện tại.
     */
    private void loadTenants() {
        if (contract == null || contract.contract_id == null) {
            return;
        }

        setTenantLoading(true);

        ApiClient.api.contractTenants().enqueue(
                new Callback<List<ContractTenant>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<ContractTenant>> call,
                            @NonNull Response<List<ContractTenant>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showTenantError();
                            return;
                        }

                        assignments.clear();

                        for (ContractTenant assignment : response.body()) {
                            if (assignment == null
                                    || assignment.contract_id == null) {
                                continue;
                            }

                            if (contract.contract_id.equals(
                                    assignment.contract_id
                            )) {
                                assignments.add(assignment);
                            }
                        }

                        loadTenantProfiles();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<ContractTenant>> call,
                            @NonNull Throwable throwable
                    ) {
                        showTenantError();
                    }
                }
        );
    }

    /**
     * Tải thông tin người thuê để hiển thị tên thay cho tenant ID.
     */
    private void loadTenantProfiles() {
        ApiClient.api.managedUsers(null).enqueue(
                new Callback<List<UserTenant>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<UserTenant>> call,
                            @NonNull Response<List<UserTenant>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showTenantError();
                            return;
                        }

                        tenants.clear();
                        tenants.addAll(response.body());

                        setTenantLoading(false);
                        renderTenants();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<UserTenant>> call,
                            @NonNull Throwable throwable
                    ) {
                        showTenantError();
                    }
                }
        );
    }

    private void setTenantLoading(boolean isLoading) {
        if (rootView == null) {
            return;
        }

        View loadingView = rootView.findViewById(
                R.id.contract_tenant_loading
        );

        loadingView.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    private void showTenantError() {
        if (rootView == null) {
            return;
        }

        setTenantLoading(false);

        TextView stateView = rootView.findViewById(
                R.id.contract_tenant_state
        );

        stateView.setVisibility(View.VISIBLE);
        stateView.setText(TENANT_LOAD_ERROR);
    }

    // -------------------------------------------------------------------------
    // Tenant lookup
    // -------------------------------------------------------------------------

    @Nullable
    private UserTenant findTenantById(@Nullable UUID tenantId) {
        if (tenantId == null) {
            return null;
        }

        for (UserTenant tenant : tenants) {
            if (tenant != null
                    && tenantId.equals(tenant.getTenant_id())) {
                return tenant;
            }
        }

        return null;
    }

    private String getTenantName(@Nullable UUID tenantId) {
        UserTenant tenant = findTenantById(tenantId);

        return tenant == null
                ? "Chưa cập nhật người thuê"
                : tenant.displayName();
    }

    // -------------------------------------------------------------------------
    // Render tenant list
    // -------------------------------------------------------------------------

    private void renderTenants() {
        if (rootView == null) {
            return;
        }

        ListView tenantList = rootView.findViewById(
                R.id.contract_tenant_list
        );

        TextView stateView = rootView.findViewById(
                R.id.contract_tenant_state
        );

        stateView.setText(EMPTY_TENANTS_MESSAGE);

        stateView.setVisibility(
                assignments.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        tenantAdapter = new ContractTenantAdapter(
                requireContext(),
                assignments,
                createTenantListener(),
                createTenantFormatter()
        );

        tenantList.setAdapter(tenantAdapter);
    }

    private ContractTenantAdapter.Listener createTenantListener() {
        return new ContractTenantAdapter.Listener() {

            @Override
            public void onEdit(ContractTenant assignment) {
                showTenantForm(assignment);
            }

            @Override
            public void onDelete(ContractTenant assignment) {
                confirmTenantDelete(assignment);
            }
        };
    }

    private ContractTenantAdapter.Formatter createTenantFormatter() {
        return new ContractTenantAdapter.Formatter() {

            @Override
            public String tenantName(ContractTenant assignment) {
                return getTenantName(assignment.tenant_id);
            }

            @Override
            public String status(String value) {
                return getStatusLabel(value);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Tenant form
    // -------------------------------------------------------------------------

    /**
     * Mở biểu mẫu thêm hoặc sửa người thuê.
     *
     * Khi sửa, tenant hiện tại vẫn được giữ nguyên và không thể đổi sang
     * người thuê khác từ spinner.
     */
    private void showTenantForm(
            @Nullable ContractTenant currentAssignment
    ) {
        if (contract == null || contract.contract_id == null) {
            showMessage("Hợp đồng chưa tải xong");
            return;
        }

        List<UserTenant> availableTenants =
                getAvailableTenants(currentAssignment);

        if (currentAssignment == null && availableTenants.isEmpty()) {
            showMessage("Không còn người thuê phù hợp để thêm");
            return;
        }

        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_contract_tenant,
                null
        );

        Spinner tenantSpinner = formView.findViewById(
                R.id.contract_tenant_form_tenant
        );

        EditText moveInInput = formView.findViewById(
                R.id.contract_tenant_form_move_in
        );

        EditText moveOutInput = formView.findViewById(
                R.id.contract_tenant_form_move_out
        );

        RadioButton representativeYes = formView.findViewById(
                R.id.contract_tenant_form_yes
        );

        RadioButton representativeNo = formView.findViewById(
                R.id.contract_tenant_form_no
        );

        setupTenantSpinner(
                tenantSpinner,
                availableTenants,
                currentAssignment
        );

        setupTenantDateInputs(
                moveInInput,
                moveOutInput,
                currentAssignment
        );

        setupRepresentativeOptions(
                representativeYes,
                representativeNo,
                currentAssignment
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        currentAssignment == null
                                ? "Thêm người thuê"
                                : "Sửa người thuê"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        currentAssignment == null ? "Thêm" : "Lưu",
                        null
                )
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view ->
                                saveTenant(
                                        dialog,
                                        tenantSpinner,
                                        availableTenants,
                                        moveInInput,
                                        moveOutInput,
                                        representativeYes,
                                        currentAssignment
                                )
                        )
        );

        dialog.show();
    }

    private List<UserTenant> getAvailableTenants(
            @Nullable ContractTenant currentAssignment
    ) {
        List<UserTenant> availableTenants = new ArrayList<>();

        for (UserTenant tenant : tenants) {
            if (tenant == null || tenant.getTenant_id() == null) {
                continue;
            }

            boolean alreadyAssigned = false;

            for (ContractTenant assignment : assignments) {
                if (assignment == null || assignment.tenant_id == null) {
                    continue;
                }

                boolean sameTenant = assignment.tenant_id.equals(
                        tenant.getTenant_id()
                );

                boolean isCurrentTenant = currentAssignment != null
                        && assignment.tenant_id.equals(
                        currentAssignment.tenant_id
                );

                if (sameTenant && !isCurrentTenant) {
                    alreadyAssigned = true;
                    break;
                }
            }

            if (!alreadyAssigned) {
                availableTenants.add(tenant);
            }
        }

        // Đưa người thuê hiện tại lên đầu danh sách khi chỉnh sửa.
        if (currentAssignment != null) {
            UserTenant currentTenant = findTenantById(
                    currentAssignment.tenant_id
            );

            if (currentTenant != null) {
                availableTenants.remove(currentTenant);
                availableTenants.add(0, currentTenant);
            }
        }

        return availableTenants;
    }

    private void setupTenantSpinner(
            @NonNull Spinner spinner,
            @NonNull List<UserTenant> availableTenants,
            @Nullable ContractTenant currentAssignment
    ) {
        List<String> tenantLabels = new ArrayList<>();

        for (UserTenant tenant : availableTenants) {
            tenantLabels.add(tenant.displayName());
        }

        spinner.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        tenantLabels
                )
        );

        spinner.setEnabled(currentAssignment == null);
    }

    private void setupTenantDateInputs(
            @NonNull EditText moveInInput,
            @NonNull EditText moveOutInput,
            @Nullable ContractTenant currentAssignment
    ) {
        moveInInput.setText(
                currentAssignment == null
                        ? ""
                        : getValue(currentAssignment.move_in_date)
        );

        moveOutInput.setText(
                currentAssignment == null
                        ? ""
                        : getValue(currentAssignment.move_out_date)
        );

        moveInInput.setOnClickListener(
                view -> chooseTenantDate(moveInInput)
        );

        moveOutInput.setOnClickListener(
                view -> chooseTenantDate(moveOutInput)
        );
    }

    private void setupRepresentativeOptions(
            @NonNull RadioButton representativeYes,
            @NonNull RadioButton representativeNo,
            @Nullable ContractTenant currentAssignment
    ) {
        boolean isRepresentative = currentAssignment != null
                && Boolean.TRUE.equals(
                currentAssignment.is_representative
        );

        representativeYes.setChecked(isRepresentative);
        representativeNo.setChecked(!isRepresentative);
    }

    // -------------------------------------------------------------------------
    // Save tenant
    // -------------------------------------------------------------------------

    private void saveTenant(
            @NonNull AlertDialog dialog,
            @NonNull Spinner tenantSpinner,
            @NonNull List<UserTenant> availableTenants,
            @NonNull EditText moveInInput,
            @NonNull EditText moveOutInput,
            @NonNull RadioButton representativeYes,
            @Nullable ContractTenant currentAssignment
    ) {
        try {
            LocalDate moveInDate = parseRequiredDate(
                    moveInInput.getText().toString()
            );

            LocalDate moveOutDate = parseOptionalDate(
                    moveOutInput.getText().toString()
            );

            validateTenantDates(moveInDate, moveOutDate);

            UUID tenantId;

            if (currentAssignment != null) {
                tenantId = currentAssignment.tenant_id;
            } else {
                int selectedPosition =
                        tenantSpinner.getSelectedItemPosition();

                if (selectedPosition < 0
                        || selectedPosition >= availableTenants.size()) {
                    showMessage("Vui lòng chọn người thuê");
                    return;
                }

                tenantId = availableTenants
                        .get(selectedPosition)
                        .getTenant_id();
            }

            if (tenantId == null) {
                showMessage("Không xác định được người thuê");
                return;
            }

            ContractTenantRequest request = new ContractTenantRequest(
                    contract.contract_id,
                    tenantId,
                    representativeYes.isChecked(),
                    moveInDate,
                    moveOutDate
            );

            saveTenantRequest(
                    dialog,
                    currentAssignment,
                    tenantId,
                    request
            );

        } catch (Exception exception) {
            showMessage(
                    "Vui lòng chọn ngày vào ở và ngày rời đi hợp lệ"
            );
        }
    }

    private void validateTenantDates(
            @NonNull LocalDate moveInDate,
            @Nullable LocalDate moveOutDate
    ) {
        if (moveOutDate != null && moveOutDate.isBefore(moveInDate)) {
            throw new IllegalArgumentException(
                    "Ngày rời đi không được trước ngày vào ở"
            );
        }
    }

    private void saveTenantRequest(
            @NonNull AlertDialog dialog,
            @Nullable ContractTenant currentAssignment,
            @NonNull UUID tenantId,
            @NonNull ContractTenantRequest request
    ) {
        Call<ContractTenant> call;

        if (currentAssignment == null) {
            call = ApiClient.api.createContractTenant(request);
        } else {
            call = ApiClient.api.updateContractTenant(
                    currentAssignment.contract_id.toString(),
                    tenantId.toString(),
                    request
            );
        }

        call.enqueue(new Callback<ContractTenant>() {

            @Override
            public void onResponse(
                    @NonNull Call<ContractTenant> call,
                    @NonNull Response<ContractTenant> response
            ) {
                if (!response.isSuccessful()) {
                    showMessage(
                            response.code() == 409
                                    ? "Người thuê đã có trong hợp đồng"
                                    : "Không thể lưu người thuê"
                    );
                    return;
                }

                dialog.dismiss();

                showMessage(
                        currentAssignment == null
                                ? "Đã thêm người thuê"
                                : "Đã cập nhật người thuê"
                );

                loadTenants();
            }

            @Override
            public void onFailure(
                    @NonNull Call<ContractTenant> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Delete tenant
    // -------------------------------------------------------------------------

    private void confirmTenantDelete(
            @NonNull ContractTenant assignment
    ) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa người thuê?")
                .setMessage(
                        "Người thuê sẽ bị gỡ khỏi hợp đồng này."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteTenant(assignment)
                )
                .show();
    }

    private void deleteTenant(
            @NonNull ContractTenant assignment
    ) {
        if (assignment.contract_id == null
                || assignment.tenant_id == null) {
            showMessage("Không xác định được người thuê hoặc hợp đồng");
            return;
        }

        ApiClient.api.deleteContractTenant(
                assignment.contract_id.toString(),
                assignment.tenant_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    showMessage("Đã xóa người thuê");
                    loadTenants();
                } else {
                    showMessage("Không thể xóa người thuê");
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Edit contract
    // -------------------------------------------------------------------------

    private void editContract() {
        if (contract == null || contract.contract_id == null) {
            showMessage("Hợp đồng chưa tải xong");
            return;
        }

        try {
            EditText startInput = findEditText(
                    R.id.contract_detail_start
            );

            EditText endInput = findEditText(
                    R.id.contract_detail_end
            );

            EditText rentInput = findEditText(
                    R.id.contract_detail_rent
            );

            EditText depositInput = findEditText(
                    R.id.contract_detail_deposit
            );

            EditText billingInput = findEditText(
                    R.id.contract_detail_billing_day
            );

            EditText dueDaysInput = findEditText(
                    R.id.contract_detail_due_days
            );

            EditText termsInput = findEditText(
                    R.id.contract_detail_terms
            );

            EditText terminatedInput = findEditText(
                    R.id.contract_detail_terminated
            );

            LocalDate startDate = parseStartDate(
                    startInput.getText().toString()
            );

            LocalDate endDate = parseOptionalDate(
                    endInput.getText().toString()
            );

            BigDecimal rentAmount = parseRequiredAmount(
                    rentInput.getText().toString()
            );

            BigDecimal depositAmount = parseRequiredAmount(
                    depositInput.getText().toString()
            );

            Integer billingDay = parseOptionalInteger(
                    billingInput.getText().toString()
            );

            Integer dueDays = Integer.valueOf(
                    dueDaysInput.getText().toString().trim()
            );

            LocalDateTime terminatedAt = parseOptionalDateTime(
                    terminatedInput.getText().toString()
            );

            int statusId = getSelectedContractStatus();

            validateContract(
                    startDate,
                    endDate,
                    rentAmount,
                    depositAmount,
                    billingDay,
                    dueDays,
                    statusId
            );

            RentalContractRequest request = new RentalContractRequest(
                    contract.room_id,
                    startDate,
                    endDate,
                    parseSignedAt(
                            findEditText(R.id.contract_detail_signed)
                    ),
                    rentAmount,
                    depositAmount,
                    billingDay,
                    dueDays,
                    statusId == R.id.contract_detail_status_active
                            ? STATUS_ACTIVE
                            : STATUS_INACTIVE,
                    getOptionalText(termsInput),
                    terminatedAt,
                    getOptionalText(
                            findEditText(R.id.contract_detail_lifecycle)
                    )
            );

            updateContract(request);

        } catch (Exception exception) {
            showMessage("Vui lòng kiểm tra các trường hợp đồng");
        }
    }

    @NonNull
    private EditText findEditText(int viewId) {
        return rootView.findViewById(viewId);
    }

    @Nullable
    private LocalDate parseStartDate(@NonNull String text) {
        String value = text.trim();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        return LocalDate.parse(value);
    }

    @NonNull
    private LocalDate parseRequiredDate(@Nullable String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Ngày không được để trống"
            );
        }

        String dateText = text.trim();

        if (dateText.isEmpty() || UNKNOWN_VALUE.equals(dateText)) {
            throw new IllegalArgumentException(
                    "Ngày không được để trống"
            );
        }

        return LocalDate.parse(dateText);
    }

    @Nullable
    private LocalDate parseOptionalDate(@Nullable String text) {
        if (text == null) {
            return null;
        }

        String value = text.trim();

        if (value.isEmpty() || UNKNOWN_VALUE.equals(value)) {
            return null;
        }

        return LocalDate.parse(value);
    }

    @NonNull
    private BigDecimal parseRequiredAmount(@NonNull String text) {
        String value = text.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Giá trị không được để trống");
        }

        return new BigDecimal(value);
    }

    @Nullable
    private Integer parseOptionalInteger(@Nullable String text) {
        if (text == null) {
            return null;
        }

        String value = text.trim();

        if (value.isEmpty() || UNKNOWN_VALUE.equals(value)) {
            return null;
        }

        return Integer.valueOf(value);
    }

    @Nullable
    private LocalDateTime parseOptionalDateTime(
            @Nullable String text
    ) {
        LocalDate date = parseOptionalDate(text);

        return date == null ? null : date.atStartOfDay();
    }

    private int getSelectedContractStatus() {
        RadioGroup statusGroup = rootView.findViewById(
                R.id.contract_detail_status
        );

        return statusGroup.getCheckedRadioButtonId();
    }

    private void validateContract(
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate,
            @NonNull BigDecimal rentAmount,
            @NonNull BigDecimal depositAmount,
            @Nullable Integer billingDay,
            int dueDays,
            int statusId
    ) {
        if (statusId == -1) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái hợp đồng"
            );
        }

        if (rentAmount.signum() < 0 || depositAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Tiền thuê và tiền đặt cọc không được âm"
            );
        }

        if (dueDays < 0) {
            throw new IllegalArgumentException(
                    "Số ngày thanh toán không được âm"
            );
        }

        if (billingDay != null
                && (billingDay < 1 || billingDay > 31)) {
            throw new IllegalArgumentException(
                    "Ngày thanh toán phải từ 1 đến 31"
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (startDate == null) {
                throw new IllegalArgumentException(
                        "Ngày bắt đầu không hợp lệ"
                );
            }

            if (endDate != null && endDate.isBefore(startDate)) {
                throw new IllegalArgumentException(
                        "Ngày kết thúc trước ngày bắt đầu"
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // Update contract API
    // -------------------------------------------------------------------------

    private void updateContract(
            @NonNull RentalContractRequest request
    ) throws Exception {
        RequestBody body = RequestBody.create(
                ApiClient.gson.toJson(request),
                MediaType.parse("application/json")
        );

        MultipartBody.Part document = documentPart();

        View editButton = rootView.findViewById(
                R.id.contract_detail_edit
        );

        editButton.setEnabled(false);

        ApiClient.api.updateContract(
                contract.contract_id.toString(),
                body,
                document
        ).enqueue(new Callback<RentalContract>() {

            @Override
            public void onResponse(
                    @NonNull Call<RentalContract> call,
                    @NonNull Response<RentalContract> response
            ) {
                if (rootView != null) {
                    rootView.findViewById(
                            R.id.contract_detail_edit
                    ).setEnabled(true);
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showMessage("Không thể cập nhật hợp đồng");
                    return;
                }

                contract = response.body();

                // Chỉ xóa tài liệu đã chọn sau khi máy chủ lưu thành công.
                selectedDocument = null;

                bindContractDetails();

                showMessage("Đã cập nhật hợp đồng");
            }

            @Override
            public void onFailure(
                    @NonNull Call<RentalContract> call,
                    @NonNull Throwable throwable
            ) {
                if (rootView != null) {
                    rootView.findViewById(
                            R.id.contract_detail_edit
                    ).setEnabled(true);
                }

                showMessage(
                        "Không thể kết nối đến máy chủ: "
                                + throwable.getMessage()
                );
            }
        });
    }

    // -------------------------------------------------------------------------
    // Render contract details
    // -------------------------------------------------------------------------

    private void bindContractDetails() {
        if (contract == null || rootView == null) {
            return;
        }

        setText(
                R.id.contract_detail_room,
                ContractDisplay.roomCode(contract, rooms)
        );

        bindContractStatus();

        setEditText(R.id.contract_detail_start, getValue(contract.start_date));
        setEditText(R.id.contract_detail_end, getValue(contract.end_date));
        setEditText(R.id.contract_detail_signed, getDateValue(contract.signed_at));

        setEditText(R.id.contract_detail_rent, getValue(contract.rent_amount));
        setEditText(R.id.contract_detail_deposit, getValue(contract.deposit_required));

        setEditText(R.id.contract_detail_billing_day, getValue(contract.billing_day));
        setEditText(R.id.contract_detail_due_days, getValue(contract.payment_due_days));

        setEditText(R.id.contract_detail_terms, getValue(contract.terms));
        setEditText(R.id.contract_detail_terminated, getDateValue(contract.terminated_at));

        setEditText(
                R.id.contract_detail_lifecycle,
                getValue(contract.termination_reason)
        );
    }

    private void bindContractStatus() {
        RadioGroup statusGroup = rootView.findViewById(
                R.id.contract_detail_status
        );

        statusGroup.clearCheck();

        int statusViewId = STATUS_ACTIVE.equalsIgnoreCase(contract.status)
                ? R.id.contract_detail_status_active
                : R.id.contract_detail_status_inactive;

        RadioButton statusButton = rootView.findViewById(statusViewId);
        statusButton.setChecked(true);
    }

    private void setText(int viewId, @NonNull String text) {
        TextView textView = rootView.findViewById(viewId);
        textView.setText(text);
    }

    private void setEditText(int viewId, @NonNull String text) {
        EditText editText = rootView.findViewById(viewId);
        editText.setText(text);
    }

    // -------------------------------------------------------------------------
    // Document management
    // -------------------------------------------------------------------------

    private void openDocument() {
        if (!hasDocument()) {
            showMessage("Hợp đồng chưa có tài liệu PDF hoặc DOCX");
            return;
        }

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(contract.document_url)
        );

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(
                    Intent.createChooser(
                            intent,
                            "Mở tài liệu hợp đồng"
                    )
            );
        } catch (Exception exception) {
            showMessage(
                    "Thiết bị không có ứng dụng hoặc trình duyệt để mở tài liệu"
            );
        }
    }

    /**
     * Tải tài liệu hợp đồng vào thư mục Download của ứng dụng.
     */
    private void downloadDocument() {
        if (!hasDocument()) {
            showMessage("Hợp đồng chưa có tài liệu PDF hoặc DOCX");
            return;
        }

        try {
            DownloadManager.Request request =
                    new DownloadManager.Request(
                            Uri.parse(contract.document_url)
                    );

            request.setTitle("Tài liệu hợp đồng");
            request.setDescription("Đang tải tài liệu hợp đồng");
            request.setMimeType(getDocumentMime());

            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            String fileName = "hop-dong-"
                    + contract.contract_id
                    + "."
                    + getDocumentExtension();

            request.setDestinationInExternalFilesDir(
                    requireContext(),
                    "Download",
                    fileName
            );

            DownloadManager manager =
                    (DownloadManager) requireContext()
                            .getSystemService(Context.DOWNLOAD_SERVICE);

            if (manager == null) {
                throw new IllegalStateException(
                        "DownloadManager không khả dụng"
                );
            }

            manager.enqueue(request);

            showMessage("Đã bắt đầu tải tài liệu");

        } catch (Exception exception) {
            showMessage("Không thể bắt đầu tải tài liệu");
        }
    }

    private String getDocumentMime() {
        String documentUrl = contract.document_url.toLowerCase(
                Locale.ROOT
        );

        return documentUrl.contains(".docx")
                ? MIME_DOCX
                : MIME_PDF;
    }

    private String getDocumentExtension() {
        return MIME_DOCX.equals(getDocumentMime())
                ? "docx"
                : "pdf";
    }

    private boolean hasDocument() {
        return contract != null
                && contract.document_url != null
                && !contract.document_url.trim().isEmpty();
    }

    private MultipartBody.Part documentPart() throws Exception {
        if (selectedDocument == null) {
            return null;
        }

        String contentType = requireContext()
                .getContentResolver()
                .getType(selectedDocument);

        String fileName = selectedDocument.getLastPathSegment() == null
                ? ""
                : selectedDocument.getLastPathSegment()
                .toLowerCase(Locale.ROOT);

        boolean isPdf = MIME_PDF.equals(contentType)
                || fileName.endsWith(".pdf");

        boolean isDocx = MIME_DOCX.equals(contentType)
                || fileName.endsWith(".docx");

        if (!isPdf && !isDocx) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ tài liệu PDF hoặc DOCX"
            );
        }

        byte[] fileBytes = readDocumentBytes();

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Tài liệu được chọn không có dữ liệu"
            );
        }

        String mimeType = isPdf ? MIME_PDF : MIME_DOCX;
        String extension = isPdf ? "pdf" : "docx";

        RequestBody requestBody = RequestBody.create(
                fileBytes,
                MediaType.parse(mimeType)
        );

        return MultipartBody.Part.createFormData(
                "document",
                "contract." + extension,
                requestBody
        );
    }

    private byte[] readDocumentBytes() throws Exception {
        try (
                InputStream input = requireContext()
                        .getContentResolver()
                        .openInputStream(selectedDocument);

                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            if (input == null) {
                throw new IllegalArgumentException(
                        "Không thể đọc tài liệu đã chọn"
                );
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        }
    }

    private void chooseDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("*/*");

        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{
                        MIME_PDF,
                        MIME_DOCX
                }
        );

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                intent,
                DOCUMENT_PICKER_REQUEST
        );
    }

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != DOCUMENT_PICKER_REQUEST
                || resultCode != Activity.RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        selectedDocument = data.getData();

        int grantedFlags = data.getFlags();

        if ((grantedFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                requireContext()
                        .getContentResolver()
                        .takePersistableUriPermission(
                                selectedDocument,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (SecurityException ignored) {
                // URI có thể không hỗ trợ cấp quyền đọc lâu dài.
            }
        }

        showMessage("Đã chọn tài liệu mới");
    }

    // -------------------------------------------------------------------------
    // Delete contract
    // -------------------------------------------------------------------------

    private void confirmContractDelete() {
        if (contract == null || contract.contract_id == null) {
            showMessage("Hợp đồng chưa tải xong");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa hợp đồng?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa hợp đồng",
                        (dialog, which) -> deleteContract()
                )
                .show();
    }

    private void deleteContract() {
        if (contract == null || contract.contract_id == null) {
            showMessage("Hợp đồng chưa tải xong");
            return;
        }

        ApiClient.api.deleteContract(
                contract.contract_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    NavHostFragment.findNavController(
                            ContractDetailFragment.this
                    ).navigateUp();

                    return;
                }

                showMessage(
                        response.code() == 409
                                ? "Không thể xóa vì hợp đồng đang có người thuê hoặc hóa đơn"
                                : "Không thể xóa hợp đồng"
                );
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Date pickers
    // -------------------------------------------------------------------------

    private void chooseDate(int viewId) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                requireContext(),
                (dialog, year, month, day) -> {
                    String selectedDate = String.format(
                            Locale.ROOT,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    setEditText(viewId, selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void chooseTenantDate(@NonNull EditText targetInput) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                requireContext(),
                (dialog, year, month, day) -> {
                    String selectedDate = String.format(
                            Locale.ROOT,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    targetInput.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // -------------------------------------------------------------------------
    // Formatting and validation helpers
    // -------------------------------------------------------------------------

    @Nullable
    private LocalDateTime parseSignedAt(@NonNull EditText input) {
        String text = input.getText().toString().trim();

        if (text.isEmpty() || UNKNOWN_VALUE.equals(text)) {
            return null;
        }

        return LocalDate.parse(text.substring(0, 10)).atStartOfDay();
    }

    @Nullable
    private String getOptionalText(@NonNull EditText input) {
        String text = input.getText().toString().trim();

        if (text.isEmpty() || UNKNOWN_VALUE.equals(text)) {
            return null;
        }

        return text;
    }

    @NonNull
    private String getDateValue(@Nullable Object value) {
        String text = getValue(value);

        return text.length() > 10
                ? text.substring(0, 10)
                : text;
    }

    @NonNull
    private String getValue(@Nullable Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return UNKNOWN_VALUE;
        }

        return value.toString();
    }

    @NonNull
    private String getStatusLabel(@Nullable String status) {
        return STATUS_ACTIVE.equalsIgnoreCase(status)
                ? "Đang hoạt động"
                : "Không hoạt động";
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
}