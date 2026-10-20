package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.Invoice;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.InvoiceRepository;

import java.util.List;

public class InvoiceListActivity extends TenantBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Hóa đơn của tôi");
        new InvoiceRepository().getMyInvoices(session.getTenantId(), new ApiCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> invoices) {
                showLoaded();
                if (invoices.isEmpty()) {
                    showEmpty("Chưa có hóa đơn.");
                    return;
                }
                addSection("Danh sách hóa đơn");
                for (Invoice invoice : invoices) {
                    String number = invoice.invoice_number == null
                            ? value(invoice.invoice_id) : invoice.invoice_number;
                    addButton(number + "  ·  " + value(invoice.status), v -> {
                                Intent intent = new Intent(InvoiceListActivity.this, InvoiceDetailActivity.class);
                                intent.putExtra("invoice_id", invoice.invoice_id.toString());
                                startActivity(intent);
                            });
                    addLine("Tổng thanh toán: " + value(invoice.total_amount));
                    addLine("Hạn thanh toán: " + value(invoice.due_date));
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
