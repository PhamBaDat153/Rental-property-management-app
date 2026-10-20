package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import com.android_dev.rentaly_management.Adapter.ContractAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.RentalContractRequest;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;
import com.android_dev.rentaly_management.Util.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình quản lý danh sách hợp đồng thuê.
 *
 * Chức năng:
 * - Hiển thị và tìm kiếm hợp đồng.
 * - Tạo mới và chỉnh sửa hợp đồng.
 * - Chọn tài liệu PDF/DOCX để gửi lên máy chủ.
 * - Điều hướng đến màn hình chi tiết hợp đồng.
 */
public class ContractsFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int DOCUMENT_PICKER_REQUEST = 702;

    private static final String ARG_EDIT_CONTRACT_ID = "edit_contract_id";
    private static final String ARG_CREATE_CONTRACT = "create_contract";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    private static final String MIME_PDF = "application/pdf";

    private static final String MIME_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String UNKNOWN_VALUE = "Chưa cập nhật";

    private static final String LOAD_ERROR_MESSAGE =
            "Không thể tải dữ liệu hợp đồng hoặc phòng. Chạm để thử lại.";

    private static final String EMPTY_MESSAGE =
            "Chưa có hợp đồng phù hợp";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<RentalContract> visibleContracts = new ArrayList<>();

    private ContractAdapter contractAdapter;

    private Uri selectedDocument;

    private String pendingEditContractId;

    private boolean loadFailed;
    private boolean createContractPending;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView stateView;
    private TextView resultCountView;

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
                R.layout.fragment_contracts,
                container,
                false
        );

        bindViews();
        setupContractList();
        setupSearch();
        setupClickListeners();
        readNavigationArguments();

        loadContractsAndRooms();

        return rootView;
    }

    private void bindViews() {
        searchInput = rootView.findViewById(R.id.contract_search);
        loadingIndicator = rootView.findViewById(R.id.contract_loading);
        stateView = rootView.findViewById(R.id.contract_state);
        resultCountView = rootView.findViewById(R.id.contract_result_count);
    }

    private void setupContractList() {
        ListView contractList = rootView.findViewById(
                R.id.contract_list
        );

        contractAdapter = new ContractAdapter(
                requireContext(),
                visibleContracts,
                new ContractAdapter.Formatter() {

                    @Override
                    public String room(RentalContract contract) {
                        return ContractDisplay.roomCode(
                                contract,
                                rooms
                        );
                    }

                    @Override
                    public String status(String status) {
                        return getStatusLabel(status);
                    }
                }
        );

        contractList.setAdapter(contractAdapter);

        contractList.setOnItemClickListener(
                (parent, row, position, id) -> openContractDetails(position)
        );
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
                // No action required.
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                renderContracts();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required.
            }
        });
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.contract_add)
                .setOnClickListener(view -> showContractForm(null));
    }

    private void readNavigationArguments() {
        Bundle arguments = getArguments();

        if (arguments == null) {
            return;
        }

        pendingEditContractId = arguments.getString(
                ARG_EDIT_CONTRACT_ID
        );

        createContractPending = arguments.getBoolean(
                ARG_CREATE_CONTRACT,
                false
        );
    }

    private void openContractDetails(int position) {
        if (position < 0 || position >= visibleContracts.size()) {
            return;
        }

        RentalContract contract = visibleContracts.get(position);

        if (contract == null || contract.contract_id == null) {
            showMessage("Không xác định được hợp đồng");
            return;
        }

        Bundle arguments = new Bundle();

        arguments.putString(
                "contract_id",
                contract.contract_id.toString()
        );

        NavHostFragment.findNavController(this).navigate(
                R.id.contractDetailFragment,
                arguments
        );
    }

    // -------------------------------------------------------------------------
    // Load contracts and rooms
    // -------------------------------------------------------------------------

    private void loadContractsAndRooms() {
        setLoading(true);

        stateView.setVisibility(View.GONE);

        loadFailed = false;

        contracts.clear();
        rooms.clear();

        LoadTracker tracker = new LoadTracker();

        loadContracts(tracker);
        loadRooms(tracker);
    }

    private void loadContracts(@NonNull LoadTracker tracker) {
        ApiClient.api.contracts().enqueue(
                new Callback<List<RentalContract>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Response<List<RentalContract>> response
                    ) {
                        boolean succeeded = response.isSuccessful()
                                && response.body() != null;

                        if (succeeded) {
                            contracts.clear();
                            contracts.addAll(response.body());
                        }

                        tracker.completeRequest(!succeeded);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Throwable throwable
                    ) {
                        tracker.completeRequest(true);
                    }
                }
        );
    }

    private void loadRooms(@NonNull LoadTracker tracker) {
        ApiClient.api.rooms().enqueue(
                new Callback<List<Room>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Room>> call,
                            @NonNull Response<List<Room>> response
                    ) {
                        boolean succeeded = response.isSuccessful()
                                && response.body() != null;

                        if (succeeded) {
                            rooms.clear();
                            rooms.addAll(response.body());
                        }

                        tracker.completeRequest(!succeeded);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Room>> call,
                            @NonNull Throwable throwable
                    ) {
                        tracker.completeRequest(true);
                    }
                }
        );
    }

    private void finishLoading(boolean failed) {
        setLoading(false);

        loadFailed = failed;

        if (failed) {
            stateView.setVisibility(View.VISIBLE);
            stateView.setText(LOAD_ERROR_MESSAGE);
            stateView.setOnClickListener(
                    view -> loadContractsAndRooms()
            );

            renderContracts();
            return;
        }

        stateView.setOnClickListener(null);

        renderContracts();
        handlePendingActions();
    }

    /**
     * Theo dõi trạng thái hoàn tất của hai yêu cầu API.
     */
    private final class LoadTracker {

        private int completedRequests;
        private boolean failed;

        private void completeRequest(boolean requestFailed) {
            failed |= requestFailed;
            completedRequests++;

            if (completedRequests == 2) {
                finishLoading(failed);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pending navigation actions
    // -------------------------------------------------------------------------

    private void handlePendingActions() {
        if (pendingEditContractId != null) {
            openPendingContractForEditing();
            return;
        }

        if (createContractPending && !rooms.isEmpty()) {
            createContractPending = false;
            showContractForm(null);
        }
    }

    private void openPendingContractForEditing() {
        for (RentalContract contract : contracts) {
            if (contract == null || contract.contract_id == null) {
                continue;
            }

            if (pendingEditContractId.equals(
                    contract.contract_id.toString()
            )) {
                pendingEditContractId = null;
                showContractForm(contract);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Render contract list
    // -------------------------------------------------------------------------

    private void renderContracts() {
        if (contractAdapter == null || searchInput == null) {
            return;
        }

        String query = searchInput.getText()
                .toString()
                .trim();

        visibleContracts.clear();

        for (RentalContract contract : contracts) {
            boolean matchesQuery = ContractDisplay.matchesRoomCode(contract, rooms, query);
            if (matchesQuery) {
                visibleContracts.add(contract);
            }
        }

        contractAdapter.notifyDataSetChanged();

        if (loadingIndicator.getVisibility() == View.VISIBLE
                || loadFailed) {
            return;
        }

        boolean isEmpty = visibleContracts.isEmpty();

        stateView.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );
        if (resultCountView != null) {
            resultCountView.setVisibility(View.VISIBLE);
            resultCountView.setText(visibleContracts.size() + " hợp đồng");
        }

        if (isEmpty) {
            stateView.setText(query.isEmpty()
                    ? "Chưa có hợp đồng.\nThêm hợp đồng đầu tiên để bắt đầu quản lý."
                    : "Không tìm thấy hợp đồng phù hợp.\nXóa tìm kiếm để xem lại danh sách.");
            stateView.setOnClickListener(query.isEmpty()
                    ? view -> showContractForm(null)
                    : view -> searchInput.setText(""));
        }
    }

    // -------------------------------------------------------------------------
    // Contract form
    // -------------------------------------------------------------------------

    private void showContractForm(
            @Nullable RentalContract contractToEdit
    ) {
        if (rooms.isEmpty()) {
            showMessage("Chưa tải được danh sách phòng để chọn");
            return;
        }

        selectedDocument = null;

        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_rental_contract,
                null
        );

        ContractForm form = new ContractForm(formView);

        setupRoomSpinner(form.roomSpinner, contractToEdit);
        populateContractForm(form, contractToEdit);
        form.roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    @NonNull AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (position >= 0 && position < rooms.size()) {
                    Room room = rooms.get(position);
                    form.rentInput.setText(
                            room == null || room.rent_price == null
                                    ? ""
                                    : getValue(room.rent_price)
                    );
                }
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) {
                form.rentInput.setText("");
            }
        });
        if (contractToEdit == null && !rooms.isEmpty()) {
            form.rentInput.setText(getValue(rooms.get(0).rent_price));
        }
        setupDatePickers(form);
        setupDocumentPicker(form);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        contractToEdit == null
                                ? "Thêm hợp đồng"
                                : "Sửa hợp đồng"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu hợp đồng", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveContract(
                                        form,
                                        contractToEdit,
                                        dialog
                                )
                        )
        );

        dialog.show();
    }

    /**
     * Gom các thành phần của biểu mẫu vào một đối tượng để tránh
     * truyền nhiều tham số giữa các phương thức.
     */
    private static final class ContractForm {

        final View root;

        final Spinner roomSpinner;

        final EditText startDateInput;
        final EditText endDateInput;
        final EditText rentInput;
        final EditText depositInput;
        final EditText billingDayInput;
        final EditText dueDaysInput;
        final EditText termsInput;

        final RadioGroup statusGroup;
        final TextView documentStatus;

        ContractForm(View root) {
            this.root = root;

            roomSpinner = root.findViewById(R.id.contract_room);

            startDateInput = root.findViewById(R.id.contract_start);
            endDateInput = root.findViewById(R.id.contract_end);
            rentInput = root.findViewById(R.id.contract_rent);
            depositInput = root.findViewById(R.id.contract_deposit);
            billingDayInput = root.findViewById(R.id.contract_billing_day);
            dueDaysInput = root.findViewById(R.id.contract_due_days);
            termsInput = root.findViewById(R.id.contract_terms);

            statusGroup = root.findViewById(R.id.contract_status);

            documentStatus = root.findViewById(
                    R.id.contract_pdf_status
            );
        }
    }

    private void setupRoomSpinner(
            @NonNull Spinner spinner,
            @Nullable RentalContract contractToEdit
    ) {
        List<String> labels = new ArrayList<>();

        for (Room room : rooms) {
            labels.add(
                    room == null ? "" : getValue(room.room_code)
            );
        }

        spinner.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        labels
                )
        );

        if (contractToEdit == null) {
            return;
        }

        for (int index = 0; index < rooms.size(); index++) {
            Room room = rooms.get(index);

            if (room != null
                    && room.room_id != null
                    && room.room_id.equals(contractToEdit.room_id)) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    private void populateContractForm(
            @NonNull ContractForm form,
            @Nullable RentalContract contract
    ) {
        form.startDateInput.setText(
                contract == null ? "" : DateUtils.formatDate(contract.start_date)
        );

        form.endDateInput.setText(
                contract == null ? "" : DateUtils.formatDateOrOpen(contract.end_date)
        );

        form.rentInput.setText(
                contract == null ? "" : getValue(contract.rent_amount)
        );

        form.depositInput.setText(
                contract == null ? "" : getValue(contract.deposit_required)
        );

        form.billingDayInput.setText(
                contract == null ? "" : getValue(contract.billing_day)
        );

        form.dueDaysInput.setText(
                contract == null
                        ? "0"
                        : getValue(contract.payment_due_days)
        );

        form.termsInput.setText(
                contract == null ? "" : getValue(contract.terms)
        );

        String status = contract == null
                ? STATUS_ACTIVE
                : contract.status;

        int statusViewId = STATUS_ACTIVE.equalsIgnoreCase(status)
                ? R.id.contract_status_active
                : R.id.contract_status_inactive;

        RadioButton statusButton = form.root.findViewById(statusViewId);
        statusButton.setChecked(true);

        form.documentStatus.setText("Chưa chọn tài liệu mới");
    }

    private void setupDatePickers(@NonNull ContractForm form) {
        form.startDateInput.setOnClickListener(
                view -> showDatePicker(form.startDateInput)
        );

        form.endDateInput.setOnClickListener(
                view -> showDatePicker(form.endDateInput)
        );
    }

    // -------------------------------------------------------------------------
    // Document picker
    // -------------------------------------------------------------------------

    private void setupDocumentPicker(@NonNull ContractForm form) {
        form.root.findViewById(R.id.contract_choose_pdf)
                .setOnClickListener(view -> chooseDocument());
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

        showMessage("Đã chọn tài liệu PDF hoặc DOCX");
    }

    // -------------------------------------------------------------------------
    // Save contract
    // -------------------------------------------------------------------------

    private void saveContract(
            @NonNull ContractForm form,
            @Nullable RentalContract contractToEdit,
            @NonNull AlertDialog dialog
    ) {
        try {
            int roomIndex = form.roomSpinner.getSelectedItemPosition();

            if (roomIndex < 0 || roomIndex >= rooms.size()) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn phòng"
                );
            }

            Room selectedRoom = rooms.get(roomIndex);

            if (selectedRoom == null || selectedRoom.room_id == null) {
                throw new IllegalArgumentException(
                        "Phòng được chọn không hợp lệ"
                );
            }

            LocalDate startDate = parseRequiredDate(
                    form.startDateInput.getText().toString()
            );

            LocalDate endDate = parseOptionalDate(
                    form.endDateInput.getText().toString()
            );

            BigDecimal rentAmount = selectedRoom.rent_price;

            if (rentAmount == null) {
                throw new IllegalArgumentException(
                        "Phòng được chọn chưa có giá thuê"
                );
            }

            BigDecimal depositAmount = parseRequiredAmount(
                    form.depositInput.getText().toString()
            );

            Integer billingDay = parseOptionalInteger(
                    form.billingDayInput.getText().toString()
            );

            int paymentDueDays = Integer.parseInt(
                    form.dueDaysInput.getText().toString().trim()
            );

            int selectedStatusId =
                    form.statusGroup.getCheckedRadioButtonId();

            if (!ContractValidation.valid(
                    startDate,
                    endDate,
                    rentAmount,
                    depositAmount,
                    billingDay,
                    paymentDueDays
            )) {
                throw new IllegalArgumentException(
                        "Vui lòng kiểm tra ngày, giá thuê, tiền cọc "
                                + "và thông tin thanh toán"
                );
            }

            if (selectedStatusId == -1) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn trạng thái hợp đồng"
                );
            }

            String status = selectedStatusId
                    == R.id.contract_status_active
                    ? STATUS_ACTIVE
                    : STATUS_INACTIVE;

            RentalContractRequest request =
                    new RentalContractRequest(
                            selectedRoom.room_id,
                            startDate,
                            endDate,
                            null,
                            rentAmount,
                            depositAmount,
                            billingDay,
                            paymentDueDays,
                            status,
                            getOptionalText(form.termsInput),
                            null,
                            null
                    );

            RequestBody requestBody = RequestBody.create(
                    ApiClient.gson.toJson(request),
                    MediaType.parse("application/json")
            );

            MultipartBody.Part documentPart = createDocumentPart();

            submitContract(
                    requestBody,
                    documentPart,
                    contractToEdit,
                    dialog
            );

        } catch (Exception exception) {
            String message = exception.getMessage();

            showMessage(
                    message == null || message.trim().isEmpty()
                            ? "Vui lòng kiểm tra ngày và các trường bắt buộc"
                            : message
            );
        }
    }

    private void submitContract(
            @NonNull RequestBody requestBody,
            @Nullable MultipartBody.Part documentPart,
            @Nullable RentalContract contractToEdit,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText("Đang lưu...");

        Call<RentalContract> call;

        if (contractToEdit == null) {
            call = ApiClient.api.createContract(
                    requestBody,
                    documentPart
            );
        } else {
            call = ApiClient.api.updateContract(
                    contractToEdit.contract_id.toString(),
                    requestBody,
                    documentPart
            );
        }

        call.enqueue(new Callback<RentalContract>() {

            @Override
            public void onResponse(
                    @NonNull Call<RentalContract> call,
                    @NonNull Response<RentalContract> response
            ) {
                if (!response.isSuccessful()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setText("Lưu hợp đồng");

                    showMessage(
                            getErrorMessage(
                                    response,
                                    "Không thể lưu hợp đồng"
                            )
                    );

                    return;
                }

                dialog.dismiss();

                selectedDocument = null;

                showMessage(
                        contractToEdit == null
                                ? "Đã thêm hợp đồng"
                                : "Đã cập nhật hợp đồng"
                );

                loadContractsAndRooms();
            }

            @Override
            public void onFailure(
                    @NonNull Call<RentalContract> call,
                    @NonNull Throwable throwable
            ) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage(
                        "Không thể kết nối đến máy chủ: "
                                + throwable.getMessage()
                );
            }
        });
    }

    // -------------------------------------------------------------------------
    // Document upload
    // -------------------------------------------------------------------------

    @Nullable
    private MultipartBody.Part createDocumentPart() throws IOException {
        if (selectedDocument == null) {
            return null;
        }

        String mimeType = requireContext()
                .getContentResolver()
                .getType(selectedDocument);

        String fileName = selectedDocument.getLastPathSegment() == null
                ? ""
                : selectedDocument.getLastPathSegment()
                .toLowerCase(Locale.ROOT);

        boolean isPdf = MIME_PDF.equals(mimeType)
                || fileName.endsWith(".pdf");

        boolean isDocx = MIME_DOCX.equals(mimeType)
                || fileName.endsWith(".docx");

        if (!isPdf && !isDocx) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ tài liệu PDF hoặc DOCX"
            );
        }

        byte[] fileBytes = readDocument(selectedDocument);

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Tài liệu được chọn không có dữ liệu"
            );
        }

        String selectedMimeType = isPdf ? MIME_PDF : MIME_DOCX;
        String extension = isPdf ? "pdf" : "docx";

        RequestBody requestBody = RequestBody.create(
                fileBytes,
                MediaType.parse(selectedMimeType)
        );

        return MultipartBody.Part.createFormData(
                "document",
                "contract." + extension,
                requestBody
        );
    }

    private byte[] readDocument(@NonNull Uri uri) throws IOException {
        try (
                InputStream input = requireContext()
                        .getContentResolver()
                        .openInputStream(uri);

                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            if (input == null) {
                throw new IOException(
                        "Không thể đọc tài liệu được chọn"
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

    // -------------------------------------------------------------------------
    // Date picker
    // -------------------------------------------------------------------------

    private void showDatePicker(@NonNull EditText targetInput) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (dialog, year, month, day) -> targetInput.setText(String.format(
                        Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day
                )),
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
        if (targetInput.getId() == R.id.contract_start) {
            picker.getDatePicker().setMinDate(System.currentTimeMillis());
        } else {
            LocalDate start = DateUtils.parseDate(
                    ((EditText) targetInput.getRootView().findViewById(R.id.contract_start))
                            .getText().toString()
            );
            if (start != null) {
                picker.getDatePicker().setMinDate(start.toEpochDay() * 86400000L);
            }
        }
        picker.show();
    }

    // -------------------------------------------------------------------------
    // Parsing and validation helpers
    // -------------------------------------------------------------------------

    @NonNull
    private LocalDate parseRequiredDate(@Nullable String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập ngày bắt đầu"
            );
        }

        String value = text.trim();

        if (value.isEmpty() || UNKNOWN_VALUE.equals(value)) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập ngày bắt đầu"
            );
        }

        return DateUtils.parseDate(value);
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

        return DateUtils.parseDate(value);
    }

    @NonNull
    private BigDecimal parseRequiredAmount(@NonNull String text) {
        String value = text.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập giá trị tiền"
            );
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
    private String getOptionalText(@NonNull EditText input) {
        String value = input.getText().toString().trim();

        return value.isEmpty() ? null : value;
    }

    @NonNull
    private String getValue(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    @NonNull
    private String getStatusLabel(@Nullable String status) {
        return STATUS_ACTIVE.equalsIgnoreCase(status)
                ? "Đang hoạt động"
                : "Không hoạt động";
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
    // API error parsing
    // -------------------------------------------------------------------------

    @NonNull
    private String getErrorMessage(
            @NonNull Response<?> response,
            @NonNull String fallback
    ) {
        try {
            if (response.errorBody() == null) {
                return fallback;
            }

            String responseBody = response.errorBody().string();

            String message = new org.json.JSONObject(responseBody)
                    .optString("message");

            if (!message.trim().isEmpty()) {
                return message;
            }

        } catch (Exception ignored) {
            // Dùng thông báo mặc định nếu không đọc được lỗi từ máy chủ.
        }

        return fallback;
    }

    // -------------------------------------------------------------------------
    // Fragment cleanup
    // -------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        rootView = null;

        searchInput = null;
        loadingIndicator = null;
        stateView = null;
        resultCountView = null;

        contractAdapter = null;
    }
}
