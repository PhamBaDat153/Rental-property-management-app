package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Invoice;
import com.example.rentaly_managemen_tenant_app.models.InvoiceItem;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.InvoiceRepository;

public class InvoiceDetailActivity extends TenantBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Chi tiết hóa đơn");
        String invoiceId = getIntent().getStringExtra("invoice_id");
        if (invoiceId == null) {
            showFailure("Thiếu mã hóa đơn.");
            return;
        }
        new InvoiceRepository().getInvoice(invoiceId, new ApiCallback<Invoice>() {
            @Override
            public void onSuccess(Invoice invoice) {
                showLoaded();
                addSection(value(invoice.invoice_number));
                addLine("Trạng thái: " + value(invoice.status));
                addLine("Ngày phát hành: " + value(invoice.issued_at));
                addLine("Hạn thanh toán: " + value(invoice.due_date));
                addLine("Tạm tính: " + value(invoice.subtotal));
                addLine("Giảm giá: " + value(invoice.discount_amount));
                addLine("Thuế: " + value(invoice.tax_amount));
                addLine("Tổng cộng: " + value(invoice.total_amount));
                if (invoice.note != null) addLine("Ghi chú: " + invoice.note);
                addSection("Chi tiết khoản thu");
                if (invoice.items == null || invoice.items.isEmpty()) addLine("Không có khoản thu chi tiết.");
                else for (InvoiceItem item : invoice.items) {
                    addLine(value(item.description) + " — " + value(item.amount)
                            + (item.unit == null ? "" : " / " + item.unit));
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
