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
import android.widget.LinearLayout;
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
import com.android_dev.rentaly_management.DTO.InvoiceItemRequest;
import com.android_dev.rentaly_management.DTO.RoomService;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.DTO.Meter;
import com.android_dev.rentaly_management.DTO.MeterReading;
import com.android_dev.rentaly_management.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        loadInvoiceLookups();
        loadInvoices();

        if (getArguments() != null
            && getArguments().getBoolean(ARG_CREATE_INVOICE, false)) {
            Invoice invoice = (Invoice) getArguments().getSerializable("invoice");
            rootView.post(() -> {
                if (invoice == null) {
                    loadFormData(null);
                } else {
                    showInvoiceForm(invoice);
                }
            });
        }

        return rootView;
    }

    private void loadInvoiceLookups() {
        ApiClient.api.contracts().enqueue(new Callback<List<RentalContract>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<RentalContract>> call,
                    @NonNull Response<List<RentalContract>> response
            ) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                    return;
                }

                contracts.clear();
                contracts.addAll(response.body());
                loadInvoiceRooms();
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<RentalContract>> call,
                    @NonNull Throwable throwable
            ) {
                // Invoice list remains usable without room metadata.
            }
        });
    }

    private void loadInvoiceRooms() {
        ApiClient.api.rooms().enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<Room>> call,
                    @NonNull Response<List<Room>> response
            ) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                    return;
                }

                rooms.clear();
                rooms.addAll(response.body());
                if (invoiceAdapter != null) {
                    invoiceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<Room>> call,
                    @NonNull Throwable throwable
            ) {
                // Invoice list remains usable without room metadata.
            }
        });
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
                new InvoiceAdapter.Formatter() {
                    @Override public String status(String value) { return getStatusLabel(value); }
                    @Override public String room(Invoice invoice) { return getInvoiceRoomLabel(invoice); }
                }
        );

        invoiceList.setAdapter(invoiceAdapter);

        invoiceList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    if (position >= 0 && position < invoices.size()) {
                        Bundle args = new Bundle();
                        args.putSerializable("invoice", invoices.get(position));
                        args.putString("room_name", getInvoiceRoomLabel(invoices.get(position)));
                        NavHostFragment.findNavController(this).navigate(R.id.invoiceDetailFragment, args);
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

        if (invoiceToEdit == null) {
            setupContractSpinner(form.contractSpinner, null);
        } else {
            form.createFields.setVisibility(View.GONE);
        }

        populateInvoiceForm(form, invoiceToEdit);
        setupDatePicker(form.dueDateInput);
        if (invoiceToEdit == null) {
            setupInvoiceCalculation(form);
            form.contractSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < contracts.size()) {
                        loadGeneratedItems(form, contracts.get(position));
                    }
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
            });
        }

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
        final LinearLayout createFields;

        final EditText invoiceNumberInput;
        final EditText dueDateInput;
        final TextView totalAmountView;
        final TextView subtotalView;
        final EditText discountInput;
        final Spinner taxRateSpinner;
        final TextView taxAmountView;
        final LinearLayout itemsView;
        final List<InvoiceItemRequest> items = new ArrayList<>();
        UUID rentServiceId;

        final Spinner statusView;

        InvoiceForm(View root) {
            createFields = root.findViewById(R.id.invoice_form_create_fields);
            contractSpinner = root.findViewById(
                    R.id.invoice_form_contract
            );

            invoiceNumberInput = root.findViewById(
                    R.id.invoice_form_number
            );

            dueDateInput = root.findViewById(
                    R.id.invoice_form_due
            );

            totalAmountView = root.findViewById(
                    R.id.invoice_form_total
            );
            subtotalView = root.findViewById(R.id.invoice_form_subtotal);
            discountInput = root.findViewById(R.id.invoice_form_discount);
            taxRateSpinner = root.findViewById(R.id.invoice_form_tax_rate);
            taxAmountView = root.findViewById(R.id.invoice_form_tax_amount);
            itemsView = root.findViewById(R.id.invoice_form_items);

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
            form.subtotalView.setText(formatMoney(BigDecimal.ZERO));
            form.discountInput.setText("0");
            form.taxRateSpinner.setSelection(0);
            setStatusOptions(form, STATUS_SENT);
            return;
        }

        form.invoiceNumberInput.setText(
                getValue(invoice.invoice_number)
        );

        form.dueDateInput.setText(
                getValue(invoice.due_date)
        );

        form.totalAmountView.setText(
                formatMoney(invoice.total_amount == null ? BigDecimal.ZERO : invoice.total_amount)
        );
        form.subtotalView.setText(formatMoney(invoice.subtotal == null ? BigDecimal.ZERO : invoice.subtotal));
        form.discountInput.setText(formatMoney(invoice.discount_amount == null ? BigDecimal.ZERO : invoice.discount_amount));

        setStatusOptions(form, invoice.status);
        List<InvoiceItemRequest> existingItems = new ArrayList<>();
        if (invoice.items != null) {
            for (com.android_dev.rentaly_management.DTO.InvoiceItem item : invoice.items) {
                existingItems.add(new InvoiceItemRequest(item.service_id, item.description,
                        item.unit_price, item.amount).withUnit(item.unit));
            }
        }
        renderInvoiceItems(form, existingItems);
    }

    private void renderInvoiceItems(@NonNull InvoiceForm form, @NonNull List<InvoiceItemRequest> items) {
        form.items.clear();
        form.items.addAll(items);
        form.itemsView.removeAllViews();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItemRequest item : items) {
            subtotal = subtotal.add(item.amount == null ? BigDecimal.ZERO : item.amount);
            TextView row = new TextView(requireContext());
            row.setText(item.description + "\n" + (item.unit == null ? "" : item.unit)
                    + " · " + formatMoney(item.amount == null ? BigDecimal.ZERO : item.amount));
            row.setTextColor(getResources().getColor(R.color.dark_navy));
            row.setTextSize(14);
            row.setPadding(0, 8, 0, 8);
            form.itemsView.addView(row);
        }
        if (items.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có khoản dịch vụ bổ sung");
            empty.setTextColor(getResources().getColor(R.color.secondary));
            form.itemsView.addView(empty);
        }
        form.subtotalView.setText(formatMoney(subtotal));
    }

    private void setStatusOptions(@NonNull InvoiceForm form, @Nullable String current) {
        form.statusView.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Đã gửi", "Đã nhận"}));
        form.statusView.setSelection("RECEIVED".equalsIgnoreCase(current) ? 1 : 0);
        form.statusView.setEnabled(!"RECEIVED".equalsIgnoreCase(current));
    }

    private void setupInvoiceCalculation(@NonNull InvoiceForm form) {
        ArrayAdapter<String> taxAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"0%", "5%", "10%", "15%", "20%", "25%", "30%"}
        );
        form.taxRateSpinner.setAdapter(taxAdapter);
        form.taxRateSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                recalculateInvoice(form);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {
                recalculateInvoice(form);
            }
        });
        form.discountInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recalculateInvoice(form); }
            @Override public void afterTextChanged(Editable s) { }
        });
        if (form.discountInput.getText().toString().isEmpty()) {
            form.discountInput.setText("0");
        }
        recalculateInvoice(form);
    }

    private void loadGeneratedItems(@NonNull InvoiceForm form, @Nullable RentalContract contract) {
        if (contract == null || contract.room_id == null) return;
        ApiClient.api.services().enqueue(new Callback<List<Service>>() {
            @Override public void onResponse(@NonNull Call<List<Service>> call, @NonNull Response<List<Service>> response) {
                UUID rentServiceId = null;
                if (response.isSuccessful() && response.body() != null) {
                    for (Service service : response.body()) {
                        if (service != null && "Rent".equalsIgnoreCase(service.name)) {
                            rentServiceId = service.service_id;
                            break;
                        }
                    }
                }
                loadRoomServiceItems(form, contract, rentServiceId);
            }
            @Override public void onFailure(@NonNull Call<List<Service>> call, @NonNull Throwable throwable) {
                loadRoomServiceItems(form, contract, null);
            }
        });
    }

    private void loadRoomServiceItems(@NonNull InvoiceForm form, @NonNull RentalContract contract,
                                      @Nullable UUID rentServiceId) {
        ApiClient.api.roomServices(contract.room_id.toString()).enqueue(new Callback<List<RoomService>>() {
            @Override public void onResponse(@NonNull Call<List<RoomService>> call, @NonNull Response<List<RoomService>> response) {
                List<RoomService> services = response.isSuccessful() && response.body() != null
                        ? response.body() : new ArrayList<>();
                loadMeterPreview(form, contract.room_id, contract.rent_amount, services, rentServiceId);
            }
            @Override public void onFailure(@NonNull Call<List<RoomService>> call, @NonNull Throwable throwable) {
                loadMeterPreview(form, contract.room_id, contract.rent_amount, new ArrayList<>(), rentServiceId);
            }
        });
    }

    private void loadMeterPreview(@NonNull InvoiceForm form, @NonNull UUID roomId,
                                  @Nullable BigDecimal rent, @NonNull List<RoomService> services,
                                  @Nullable UUID rentServiceId) {
        ApiClient.api.meters(roomId.toString()).enqueue(new Callback<List<Meter>>() {
            @Override public void onResponse(@NonNull Call<List<Meter>> call, @NonNull Response<List<Meter>> response) {
                List<Meter> meters = response.isSuccessful() && response.body() != null
                        ? response.body() : new ArrayList<>();
                loadMeterItems(form, rent, services, meters, rentServiceId, 0, new ArrayList<>());
            }
            @Override public void onFailure(@NonNull Call<List<Meter>> call, @NonNull Throwable throwable) {
                loadMeterItems(form, rent, services, new ArrayList<>(), rentServiceId, 0, new ArrayList<>());
            }
        });
    }

    private void loadMeterItems(@NonNull InvoiceForm form, @Nullable BigDecimal rent,
                                @NonNull List<RoomService> services, @NonNull List<Meter> meters,
                                @Nullable UUID rentServiceId, int index,
                                @NonNull List<InvoiceItemRequest> items) {
        if (index == 0 && rent != null) {
            items.add(new InvoiceItemRequest(rentServiceId, "Tiền thuê phòng", rent, rent)
                    .withUnit("tháng · theo hợp đồng"));
        }
        if (index >= services.size()) {
            renderInvoiceItems(form, items);
            recalculateInvoice(form);
            return;
        }

        RoomService service = services.get(index);
        if (service == null || Boolean.FALSE.equals(service.is_active)
                || service.default_unit_price == null || service.service_id == null) {
            loadMeterItems(form, rent, services, meters, rentServiceId, index + 1, items);
            return;
        }

        if (!"METER".equalsIgnoreCase(service.calculation_method)) {
            items.add(new InvoiceItemRequest(service.service_id, service.name,
                    service.default_unit_price, service.default_unit_price).withUnit(service.unit));
            loadMeterItems(form, rent, services, meters, rentServiceId, index + 1, items);
            return;
        }

        Meter meter = findMeter(meters, service.name);
        if (meter == null || meter.meter_id == null) {
            loadMeterItems(form, rent, services, meters, rentServiceId, index + 1, items);
            return;
        }

        ApiClient.api.meterReadings(meter.meter_id.toString()).enqueue(new Callback<List<MeterReading>>() {
            @Override public void onResponse(@NonNull Call<List<MeterReading>> call, @NonNull Response<List<MeterReading>> response) {
                MeterReading reading = latestReading(response.body());
                if (reading != null && reading.quantity != null) {
                    BigDecimal amount = reading.quantity.multiply(service.default_unit_price);
                    items.add(new InvoiceItemRequest(service.service_id, service.name,
                            service.default_unit_price, amount).withUnit(service.unit + " · " + reading.quantity));
                }
                loadMeterItems(form, rent, services, meters, rentServiceId, index + 1, items);
            }
            @Override public void onFailure(@NonNull Call<List<MeterReading>> call, @NonNull Throwable throwable) {
                loadMeterItems(form, rent, services, meters, rentServiceId, index + 1, items);
            }
        });
    }

    @Nullable
    private Meter findMeter(@NonNull List<Meter> meters, @Nullable String serviceName) {
        String normalized = serviceName == null ? "" : serviceName.toLowerCase(Locale.ROOT);
        String type = normalized.contains("water") || normalized.contains("nước")
                ? "WATER" : "ELECTRICITY";
        for (Meter meter : meters) {
            if (meter != null && type.equalsIgnoreCase(meter.meter_type)) return meter;
        }
        return null;
    }

    @Nullable
    private MeterReading latestReading(@Nullable List<MeterReading> readings) {
        MeterReading latest = null;
        if (readings == null) return null;
        for (MeterReading reading : readings) {
            if (reading == null || reading.reading_at == null) continue;
            if (latest == null || reading.reading_at.isAfter(latest.reading_at)) latest = reading;
        }
        return latest;
    }

    private void recalculateInvoice(@NonNull InvoiceForm form) {
        BigDecimal subtotal = parseMoney(form.subtotalView.getText().toString());
        BigDecimal discount = parseMoney(form.discountInput.getText().toString());
        BigDecimal taxRate = BigDecimal.valueOf(form.taxRateSpinner.getSelectedItemPosition() * 5L);
        BigDecimal tax = subtotal.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);
        form.taxAmountView.setText(formatMoney(tax));
        form.totalAmountView.setText(formatMoney(total));
    }

    private BigDecimal parseMoney(@Nullable String value) {
        try {
            String normalized = value == null ? "" : value.replace(" VND", "").trim();
            return normalized.isEmpty() ? BigDecimal.ZERO : new BigDecimal(normalized);
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private String formatMoney(@NonNull BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + " VND";
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
            if (invoiceToEdit != null) {
                saveInvoiceMetadataOnly(form, invoiceToEdit, dialog);
                return;
            }

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


            if (invoiceNumber.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập số hóa đơn"
                );
            }

            LocalDate dueDate = dueDateText.isEmpty()
                    ? null
                    : LocalDate.parse(dueDateText);

            if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                form.dueDateInput.setError("Hạn thanh toán phải từ hôm nay trở đi");
                form.dueDateInput.requestFocus();
                return;
            }

            String currentStatus = invoiceToEdit == null
                    ? STATUS_SENT
                    : (form.statusView.getSelectedItemPosition() == 1 ? STATUS_RECEIVED : STATUS_SENT);
            if (invoiceToEdit != null && STATUS_RECEIVED.equalsIgnoreCase(invoiceToEdit.status)
                    && !STATUS_RECEIVED.equalsIgnoreCase(currentStatus)) {
                throw new IllegalArgumentException("Hóa đơn đã nhận không thể chuyển lại trạng thái đã gửi");
            }

            BigDecimal subtotal = parseMoney(form.subtotalView.getText().toString());
            BigDecimal discount = parseMoney(form.discountInput.getText().toString());
            BigDecimal taxRate = BigDecimal.valueOf(form.taxRateSpinner.getSelectedItemPosition() * 5L);
            BigDecimal tax = subtotal.multiply(taxRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);
            if (discount.compareTo(subtotal.add(tax)) > 0) {
                form.discountInput.setError("Giảm giá không được lớn hơn tổng tiền");
                form.discountInput.requestFocus();
                return;
            }
            List<InvoiceItemRequest> items = new ArrayList<>();
            for (InvoiceItemRequest item : form.items) {
                boolean rentPreview = "Tiền thuê phòng".equals(item.description);
                if (!rentPreview) {
                    items.add(item);
                }
            }
            InvoiceRequest request = new InvoiceRequest(
                    selectedContract.contract_id, invoiceNumber, dueDate,
                    subtotal, discount, tax, total, null, currentStatus
            ).withItems(items);

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

    private void saveInvoiceMetadataOnly(
            @NonNull InvoiceForm form,
            @NonNull Invoice invoice,
            @NonNull AlertDialog dialog
    ) {
        String dueDateText = form.dueDateInput.getText().toString().trim();
        if (dueDateText.isEmpty()) {
            form.dueDateInput.setError("Vui lòng chọn hạn thanh toán");
            form.dueDateInput.requestFocus();
            return;
        }
        LocalDate dueDate = LocalDate.parse(dueDateText);

        if (dueDate != null
                && !dueDate.equals(invoice.due_date)
                && dueDate.isBefore(LocalDate.now())) {
            form.dueDateInput.setError("Hạn thanh toán phải từ hôm nay trở đi");
            form.dueDateInput.requestFocus();
            return;
        }

        String status = form.statusView.getSelectedItemPosition() == 1
                ? STATUS_RECEIVED
                : STATUS_SENT;
        if (STATUS_RECEIVED.equalsIgnoreCase(invoice.status)
                && !STATUS_RECEIVED.equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Hóa đơn đã nhận không thể chuyển lại trạng thái đã gửi");
        }

        InvoiceRequest request = new InvoiceRequest(
                invoice.contract_id,
                invoice.invoice_number,
                dueDate,
                invoice.subtotal == null ? BigDecimal.ZERO : invoice.subtotal,
                invoice.discount_amount == null ? BigDecimal.ZERO : invoice.discount_amount,
                invoice.tax_amount == null ? BigDecimal.ZERO : invoice.tax_amount,
                invoice.total_amount == null ? BigDecimal.ZERO : invoice.total_amount,
                invoice.note,
                status
        );
        submitInvoice(request, invoice, dialog);
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

        DatePickerDialog picker = new DatePickerDialog(
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
        );
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
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

    private String getInvoiceRoomLabel(@Nullable Invoice invoice) {
        if (invoice == null || invoice.contract_id == null) {
            return UNKNOWN_ROOM;
        }

        for (RentalContract contract : contracts) {
            if (contract != null
                    && invoice.contract_id.equals(contract.contract_id)) {
                return getRoomLabel(contract.room_id);
            }
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
