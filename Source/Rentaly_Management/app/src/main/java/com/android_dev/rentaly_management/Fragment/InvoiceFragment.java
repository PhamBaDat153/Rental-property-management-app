package com.android_dev.rentaly_management.Fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceFragment extends Fragment {
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private ArrayAdapter<Invoice> adapter;
    private ProgressBar loading;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_invoice, container, false);
        loading = view.findViewById(R.id.invoice_loading);
        empty = view.findViewById(R.id.invoice_empty);
        view.findViewById(R.id.invoice_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.invoice_add).setOnClickListener(v -> loadFormData(null));
        adapter = new ArrayAdapter<Invoice>(requireContext(), R.layout.item_invoice, R.id.invoice_item_number, invoices) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                Invoice invoice = getItem(position);
                ((TextView) row.findViewById(R.id.invoice_item_number)).setText(invoice.invoice_number);
                ((TextView) row.findViewById(R.id.invoice_item_meta)).setText(invoice.total_amount + " | " + status(invoice.status) + " | Hạn: " + value(invoice.due_date));
                return row;
            }
        };
        ListView list = view.findViewById(R.id.invoice_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> actions(invoices.get(position)));
        load();
        return view;
    }

    private void load() {
        loading.setVisibility(View.VISIBLE);
        ApiClient.api.invoices(null, null).enqueue(new Callback<List<Invoice>>() {
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                loading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    invoices.clear();
                    invoices.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    empty.setVisibility(invoices.isEmpty() ? View.VISIBLE : View.GONE);
                } else error("Không thể tải hóa đơn");
            }
            public void onFailure(Call<List<Invoice>> call, Throwable throwable) {
                loading.setVisibility(View.GONE);
                error("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void loadFormData(Invoice current) {
        ApiClient.api.contracts().enqueue(new Callback<List<RentalContract>>() {
            public void onResponse(Call<List<RentalContract>> call, Response<List<RentalContract>> response) {
                if (!response.isSuccessful() || response.body() == null) { error("Không thể tải danh sách hợp đồng"); return; }
                contracts.clear(); contracts.addAll(response.body());
                ApiClient.api.rooms().enqueue(new Callback<List<Room>>() {
                    public void onResponse(Call<List<Room>> c, Response<List<Room>> r) {
                        if (!r.isSuccessful() || r.body() == null) { error("Không thể tải danh sách phòng"); return; }
                        rooms.clear(); rooms.addAll(r.body());
                        form(current);
                    }
                    public void onFailure(Call<List<Room>> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
                });
            }
            public void onFailure(Call<List<RentalContract>> call, Throwable throwable) { error("Không thể kết nối đến máy chủ"); }
        });
    }

    private void actions(Invoice invoice) {
        new AlertDialog.Builder(requireContext()).setTitle(invoice.invoice_number)
                .setItems(new String[]{"Sửa hóa đơn", "Xóa hóa đơn"}, (dialog, which) -> {
                    if (which == 0) loadFormData(invoice);
                    else new AlertDialog.Builder(requireContext()).setTitle("Xóa hóa đơn?").setMessage("Hóa đơn và các dòng chi tiết sẽ bị xóa.")
                            .setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> ApiClient.api.deleteInvoice(invoice.invoice_id.toString()).enqueue(new Callback<Void>() {
                                public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { error("Đã xóa hóa đơn"); load(); } else error("Không thể xóa hóa đơn"); }
                                public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
                            })).show();
                }).show();
    }

    private void form(Invoice current) {
        View form = getLayoutInflater().inflate(R.layout.dialog_create_invoice, null);
        Spinner contractSpinner = form.findViewById(R.id.invoice_form_contract);
        EditText number = form.findViewById(R.id.invoice_form_number);
        EditText due = form.findViewById(R.id.invoice_form_due);
        EditText total = form.findViewById(R.id.invoice_form_total);
        TextView state = form.findViewById(R.id.invoice_form_status);
        List<String> labels = new ArrayList<>();
        for (RentalContract contract : contracts) labels.add(roomLabel(contract.room_id) + " - Hợp đồng");
        contractSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
        if (current != null) {
            for (int i = 0; i < contracts.size(); i++) if (contracts.get(i).contract_id.equals(current.contract_id)) contractSpinner.setSelection(i);
            number.setText(current.invoice_number);
            due.setText(value(current.due_date));
            total.setText(value(current.total_amount));
            state.setText(status(current.status));
        } else state.setText("Đã gửi");
        due.setOnClickListener(v -> chooseDate(due));
        new AlertDialog.Builder(requireContext()).setTitle(current == null ? "Thêm hóa đơn" : "Sửa hóa đơn").setView(form)
                .setNegativeButton("Hủy", null).setPositiveButton("Lưu", (dialog, which) -> {
                    try {
                        String dueText = due.getText().toString().trim();
                        RentalContract contract = contracts.get(contractSpinner.getSelectedItemPosition());
                        String currentStatus = current == null ? "SENTED" : current.status;
                        InvoiceRequest request = new InvoiceRequest(contract.contract_id, number.getText().toString().trim(), dueText.isEmpty() ? null : LocalDate.parse(dueText), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(total.getText().toString().trim()), null, currentStatus);
                        Call<Invoice> call = current == null ? ApiClient.api.createInvoice(request) : ApiClient.api.updateInvoice(current.invoice_id.toString(), request);
                        call.enqueue(new Callback<Invoice>() {
                            public void onResponse(Call<Invoice> c, Response<Invoice> r) { if (r.isSuccessful()) { error("Đã lưu hóa đơn"); load(); } else error("Không thể lưu hóa đơn"); }
                            public void onFailure(Call<Invoice> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
                        });
                    } catch (Exception e) { error("Vui lòng kiểm tra thông tin hóa đơn"); }
                }).show();
    }

    private void chooseDate(EditText field) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dialog, year, month, day) -> field.setText(String.format(java.util.Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String roomLabel(UUID roomId) {
        for (Room room : rooms) if (room.room_id != null && room.room_id.equals(roomId)) return room.room_name == null || room.room_name.trim().isEmpty() ? "Mã phòng: " + room.room_code : room.room_name + " (" + room.room_code + ")";
        return "Phòng chưa cập nhật";
    }
    private String status(String value) { return "RECEIVED".equalsIgnoreCase(value) ? "Đã nhận" : "Đã gửi"; }
    private String value(Object value) { return value == null ? "" : value.toString(); }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
