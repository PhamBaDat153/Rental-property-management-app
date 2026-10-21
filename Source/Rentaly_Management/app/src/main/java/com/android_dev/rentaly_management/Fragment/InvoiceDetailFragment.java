package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Invoice;
import com.android_dev.rentaly_management.DTO.InvoiceItem;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceDetailFragment extends Fragment {
    private Invoice invoice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = inflater.inflate(R.layout.fragment_invoice_detail, container, false);
        invoice = (Invoice) requireArguments().getSerializable("invoice");
        bind(view);
        return view;
    }

    private void bind(View view) {
        if (invoice == null) {
            showMessage("Không tìm thấy hóa đơn");
            return;
        }
        set(view, R.id.invoice_detail_number, value(invoice.invoice_number));
        set(view, R.id.invoice_detail_room, "Phòng: " + value(getArguments().getString("room_name")));
        set(view, R.id.invoice_detail_status, "Trạng thái: " + status(invoice.status));
        set(view, R.id.invoice_detail_due, "Hạn thanh toán: " + value(invoice.due_date));
        renderItems(view.findViewById(R.id.invoice_detail_items));
        set(view, R.id.invoice_detail_subtotal, "Tạm tính: " + money(invoice.subtotal));
        set(view, R.id.invoice_detail_discount, "Giảm giá: " + money(invoice.discount_amount));
        set(view, R.id.invoice_detail_tax, "Thuế: " + money(invoice.tax_amount));
        set(view, R.id.invoice_detail_total, "Tổng cộng: " + money(invoice.total_amount));
        view.findViewById(R.id.invoice_detail_back).setOnClickListener(v -> goBack());
        view.findViewById(R.id.invoice_detail_edit).setOnClickListener(v -> edit());
        view.findViewById(R.id.invoice_detail_delete).setOnClickListener(v -> confirmDelete());
    }

    private void renderItems(LinearLayout container) {
        container.removeAllViews();
        if (invoice.items == null || invoice.items.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có khoản chi tiết");
            empty.setTextColor(getResources().getColor(R.color.secondary));
            container.addView(empty);
            return;
        }
        for (InvoiceItem item : invoice.items) {
            TextView row = new TextView(requireContext());
            row.setText(value(item.description) + "\nĐơn vị: " + value(item.unit)
                    + " · Đơn giá: " + money(item.unit_price)
                    + "\nThành tiền: " + money(item.amount));
            row.setTextColor(getResources().getColor(R.color.dark_navy));
            row.setTextSize(15);
            row.setPadding(0, 10, 0, 10);
            container.addView(row);
        }
    }

    private void edit() {
        Bundle args = new Bundle();
        args.putBoolean("create_invoice", true);
        args.putSerializable("invoice", invoice);
        NavHostFragment.findNavController(this).navigate(R.id.invoiceFragment, args);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext()).setTitle("Xóa hóa đơn?")
                .setMessage("Hóa đơn và các khoản chi tiết sẽ bị xóa.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> delete()).show();
    }

    private void delete() {
        if (invoice.invoice_id == null) return;
        ApiClient.api.deleteInvoice(invoice.invoice_id.toString()).enqueue(new Callback<Void>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) { showMessage("Đã xóa hóa đơn"); goBack(); }
                else showMessage("Không thể xóa hóa đơn");
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) { showMessage("Không thể kết nối đến máy chủ"); }
        });
    }

    private void set(View v, int id, String text) { ((TextView) v.findViewById(id)).setText(text); }
    private String value(Object v) { return v == null ? "Chưa cập nhật" : String.valueOf(v); }
    private String money(BigDecimal value) { return value == null ? "0 VND" : value.toPlainString() + " VND"; }
    private String status(String value) { return "RECEIVED".equalsIgnoreCase(value) ? "Đã nhận" : "Đã gửi"; }
    private void goBack() { if (isAdded()) NavHostFragment.findNavController(this).navigateUp(); }
    private void showMessage(String message) { if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
