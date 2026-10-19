package com.android_dev.rentaly_management.Fragment;

import android.app.DatePickerDialog;
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

import com.android_dev.rentaly_management.Adapter.InvoiceAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Invoice;
import com.android_dev.rentaly_management.DTO.InvoiceRequest;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_CREATE_INVOICE = "create_invoice";

    private static final String STATUS_SENT = "SENTED";
    private static final String STATUS_RECEIVED = "RECEIVED";

    private static final String UNKNOWN_ROOM = "Phòng chưa cập nhật";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Invoice> invoices = new ArrayList<>();
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();

    private InvoiceAdapter invoiceAdapter;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private ProgressBar loadingIndicator;
    private TextView emptyState;
    private EditText searchInput;

    private TextView documentStatusView;

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
                R.layout.fragment_invoice,
                container,
                false
        );

        bindViews();
        setupInvoiceList();
        setupClickListeners();

        loadInvoices();

        if (getArguments() != null
                && getArguments().getBoolean(ARG_CREATE_INVOICE, false)) {
            rootView.post(() -> loadFormData(null));
        }

        return rootView;
    }

    private void bindViews() {
        loadingIndicator = rootView.findViewById(
                R.id.invoice_loading
        );

        emptyState = rootView.findViewById(
                R.id.invoice_empty
        );
    }

    private void setupInvoiceList() {
        ListView invoiceList = rootView.findViewById(
                R.id.invoice_list
        );

        invoiceAdapter = new InvoiceAdapter(
                requireContext(),
                invoices,
                this::getStatusLabel
        );

        invoiceList.setAdapter(invoiceAdapter);

        invoiceList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    if (position >= 0 && position < invoices.size()) {
                        showInvoiceActions(invoices.get(position));
                    }
                }
        );
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.invoice_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );

        rootView.findViewById(R.id.invoice_add)
                .setOnClickListener(
                        view -> loadFormData(null)
                );
    }

    // -------------------------------------------------------------------------
    // Load invoices
    // -------------------------------------------------------------------------

    private void loadInvoices() {
        setLoading(true);

        ApiClient.api.invoices(null, null).enqueue(
                new Callback<List<Invoice>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Invoice>> call,
                            @NonNull Response<List<Invoice>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải hóa đơn");
                            return;
                        }

                        invoices.clear();
                        invoices.addAll(response.body());

                        invoiceAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Invoice>> call,
                            @NonNull Throwable throwable
                    ) {
                        setLoading(false);

                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    private void updateEmptyState() {
        if (emptyState == null) {
            return;
        }

        boolean isEmpty = invoices.isEmpty();

        emptyState.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        if (isEmpty) {
            emptyState.setText("Chưa có hóa đơn");
        }
    }

    // -------------------------------------------------------------------------
    // Load form data
    // -------------------------------------------------------------------------

    private void loadFormData(@Nullable Invoice invoiceToEdit) {
        ApiClient.api.contracts().enqueue(
                new Callback<List<RentalContract>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Response<List<RentalContract>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải danh sách hợp đồng"
                            );
                            return;
                        }

                        contracts.clear();
                        contracts.addAll(response.body());

                        loadRoomsForForm(invoiceToEdit);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    private void loadRoomsForForm(
            @Nullable Invoice invoiceToEdit
    ) {
        ApiClient.api.rooms().enqueue(
                new Callback<List<Room>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Room>> call,
                            @NonNull Response<List<Room>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải danh sách phòng");
                            return;
                        }

                        rooms.clear();
                        rooms.addAll(response.body());

                        showInvoiceForm(invoiceToEdit);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Room>> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Invoice actions
    // -------------------------------------------------------------------------

    private void showInvoiceActions(@NonNull Invoice invoice) {
        new AlertDialog.Builder(requireContext())
                .setTitle(invoice.invoice_number)
                .setItems(
                        new String[]{
                                "Sửa hóa đơn",
                                "Xóa hóa đơn"
                        },
                        (dialog, selectedAction) -> {
                            if (selectedAction == 0) {
                                loadFormData(invoice);
                            } else {
                                confirmInvoiceDelete(invoice);
                            }
                        }
                )
                .show();
    }

    // -------------------------------------------------------------------------
    // Delete invoice
    // -------------------------------------------------------------------------

    private void confirmInvoiceDelete(@NonNull Invoice invoice) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa hóa đơn?")
                .setMessage(
                        "Hóa đơn và các dòng chi tiết sẽ bị xóa."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteInvoice(invoice)
                )
                .show();
    }

    private void deleteInvoice(@NonNull Invoice invoice) {
        if (invoice.invoice_id == null) {
            showMessage("Không xác định được hóa đơn");
            return;
        }

        ApiClient.api.deleteInvoice(
                invoice.invoice_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!response.isSuccessful()) {
                    showMessage("Không thể xóa hóa đơn");
                    return;
                }

                showMessage("Đã xóa hóa đơn");
                loadInvoices();
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
    // Invoice form
    // -------------------------------------------------------------------------

    private void showInvoiceForm(
            @Nullable Invoice invoiceToEdit
    ) {
        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_invoice,
                null
        );

        InvoiceForm form = new InvoiceForm(formView);

        setupContractSpinner(
                form.contractSpinner,
                invoiceToEdit
        );

        populateInvoiceForm(form, invoiceToEdit);
        setupDatePicker(form.dueDateInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        invoiceToEdit == null
                                ? "Thêm hóa đơn"
                                : "Sửa hóa đơn"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveInvoice(
                                        form,
                                        invoiceToEdit,
                                        dialog
                                )
                        )
        );

        dialog.show();
    }

    /**
     * Holds the views used by the invoice form.
     */
    private static final class InvoiceForm {

        final Spinner contractSpinner;

        final EditText invoiceNumberInput;
        final EditText dueDateInput;
        final EditText totalAmountInput;

        final TextView statusView;

        InvoiceForm(View root) {
            contractSpinner = root.findViewById(
                    R.id.invoice_form_contract
            );

            invoiceNumberInput = root.findViewById(
                    R.id.invoice_form_number
            );

            dueDateInput = root.findViewById(
                    R.id.invoice_form_due
            );

            totalAmountInput = root.findViewById(
                    R.id.invoice_form_total
            );

            statusView = root.findViewById(
                    R.id.invoice_form_status
            );
        }
    }

    // -------------------------------------------------------------------------
    // Contract spinner
    // -------------------------------------------------------------------------

    private void setupContractSpinner(
            @NonNull Spinner spinner,
            @Nullable Invoice invoiceToEdit
    ) {
        List<String> contractLabels = new ArrayList<>();

        for (RentalContract contract : contracts) {
            if (contract == null) {
                contractLabels.add(UNKNOWN_ROOM + " - Hợp đồng");
                continue;
            }

            contractLabels.add(
                    getRoomLabel(contract.room_id) + " - Hợp đồng"
            );
        }

        ArrayAdapter<String> contractAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contractLabels
        );

        spinner.setAdapter(contractAdapter);

        if (invoiceToEdit == null || invoiceToEdit.contract_id == null) {
            return;
        }

        for (int index = 0; index < contracts.size(); index++) {
            RentalContract contract = contracts.get(index);

            if (contract != null
                    && contract.contract_id != null
                    && contract.contract_id.equals(
                    invoiceToEdit.contract_id
            )) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Populate invoice form
    // -------------------------------------------------------------------------

    private void populateInvoiceForm(
            @NonNull InvoiceForm form,
            @Nullable Invoice invoice
    ) {
        if (invoice == null) {
            form.invoiceNumberInput.setText("");
            form.dueDateInput.setText("");
            form.totalAmountInput.setText("");
            form.statusView.setText("Đã gửi");
            return;
        }

        form.invoiceNumberInput.setText(
                getValue(invoice.invoice_number)
        );

        form.dueDateInput.setText(
                getValue(invoice.due_date)
        );

        form.totalAmountInput.setText(
                getValue(invoice.total_amount)
        );

        form.statusView.setText(
                getStatusLabel(invoice.status)
        );
    }

    // -------------------------------------------------------------------------
    // Save invoice
    // -------------------------------------------------------------------------

    private void saveInvoice(
            @NonNull InvoiceForm form,
            @Nullable Invoice invoiceToEdit,
            @NonNull AlertDialog dialog
    ) {
        try {
            int selectedPosition =
                    form.contractSpinner.getSelectedItemPosition();

            if (selectedPosition < 0
                    || selectedPosition >= contracts.size()) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn hợp đồng"
                );
            }

            RentalContract selectedContract =
                    contracts.get(selectedPosition);

            if (selectedContract == null
                    || selectedContract.contract_id == null) {
                throw new IllegalArgumentException(
                        "Hợp đồng được chọn không hợp lệ"
                );
            }

            String invoiceNumber = form.invoiceNumberInput
                    .getText()
                    .toString()
                    .trim();

            String dueDateText = form.dueDateInput
                    .getText()
                    .toString()
                    .trim();

            String totalAmountText = form.totalAmountInput
                    .getText()
                    .toString()
                    .trim();

            if (invoiceNumber.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập số hóa đơn"
                );
            }

            if (totalAmountText.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập tổng tiền hóa đơn"
                );
            }

            LocalDate dueDate = dueDateText.isEmpty()
                    ? null
                    : LocalDate.parse(dueDateText);

            BigDecimal totalAmount = new BigDecimal(
                    totalAmountText
            );

            if (totalAmount.signum() < 0) {
                throw new IllegalArgumentException(
                        "Tổng tiền không được âm"
                );
            }

            String currentStatus = invoiceToEdit == null
                    ? STATUS_SENT
                    : invoiceToEdit.status;

            InvoiceRequest request = new InvoiceRequest(
                    selectedContract.contract_id,
                    invoiceNumber,
                    dueDate,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    totalAmount,
                    null,
                    currentStatus
            );

            submitInvoice(
                    request,
                    invoiceToEdit,
                    dialog
            );

        } catch (Exception exception) {
            String message = exception.getMessage();

            showMessage(
                    message == null || message.trim().isEmpty()
                            ? "Vui lòng kiểm tra thông tin hóa đơn"
                            : message
            );
        }
    }

    private void submitInvoice(
            @NonNull InvoiceRequest request,
            @Nullable Invoice invoiceToEdit,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        Call<Invoice> call;

        if (invoiceToEdit == null) {
            call = ApiClient.api.createInvoice(request);
        } else {
            if (invoiceToEdit.invoice_id == null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage("Không xác định được hóa đơn");
                return;
            }

            call = ApiClient.api.updateInvoice(
                    invoiceToEdit.invoice_id.toString(),
                    request
            );
        }

        call.enqueue(new Callback<Invoice>() {

            @Override
            public void onResponse(
                    @NonNull Call<Invoice> call,
                    @NonNull Response<Invoice> response
            ) {
                if (!response.isSuccessful()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);

                    showMessage("Không thể lưu hóa đơn");
                    return;
                }

                dialog.dismiss();

                showMessage("Đã lưu hóa đơn");
                loadInvoices();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Invoice> call,
                    @NonNull Throwable throwable
            ) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Date picker
    // -------------------------------------------------------------------------

    private void setupDatePicker(@NonNull EditText dateInput) {
        dateInput.setOnClickListener(
                view -> chooseDate(dateInput)
        );
    }

    private void chooseDate(@NonNull EditText targetInput) {
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
    // Room lookup and formatting
    // -------------------------------------------------------------------------

    private String getRoomLabel(@Nullable UUID roomId) {
        if (roomId == null) {
            return UNKNOWN_ROOM;
        }

        for (Room room : rooms) {
            if (room == null
                    || room.room_id == null
                    || !room.room_id.equals(roomId)) {
                continue;
            }

            String roomCode = room.room_code == null
                    ? ""
                    : room.room_code.trim();

            String roomName = room.room_name == null
                    ? ""
                    : room.room_name.trim();

            if (roomName.isEmpty()) {
                return roomCode.isEmpty()
                        ? UNKNOWN_ROOM
                        : "Mã phòng: " + roomCode;
            }

            return roomCode.isEmpty()
                    ? roomName
                    : roomName + " (" + roomCode + ")";
        }

        return UNKNOWN_ROOM;
    }

    private String getStatusLabel(@Nullable String status) {
        return STATUS_RECEIVED.equalsIgnoreCase(status)
                ? "Đã nhận"
                : "Đã gửi";
    }

    private String getValue(@Nullable Object value) {
        return value == null ? "" : value.toString();
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
        searchInput = null;
        documentStatusView = null;
        invoiceAdapter = null;
    }
}