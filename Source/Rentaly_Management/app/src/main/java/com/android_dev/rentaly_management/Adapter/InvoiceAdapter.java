package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.Invoice;
import com.android_dev.rentaly_management.R;

import java.util.List;

/**
 * Chuyển từng Invoice thành một dòng hiển thị trong danh sách hóa đơn.
 */
public class InvoiceAdapter extends ArrayAdapter<Invoice> {

    /** Cho phép Fragment cung cấp cách dịch trạng thái hóa đơn. */
    public interface Formatter {
        String status(String value);
    }

    private final Formatter formatter;

    public InvoiceAdapter(Context context, List<Invoice> invoices, Formatter formatter) {
        super(context, 0, invoices);
        this.formatter = formatter;
    }

    /** Tạo hoặc tái sử dụng một dòng rồi gán dữ liệu hóa đơn vào giao diện. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_invoice, parent, false);
        }

        Invoice invoice = getItem(position);
        if (invoice == null) {
            return row;
        }

        TextView numberText = row.findViewById(R.id.invoice_item_number);
        TextView metaText = row.findViewById(R.id.invoice_item_meta);

        numberText.setText(value(invoice.invoice_number));
        metaText.setText(
                value(invoice.total_amount)
                        + " | " + formatter.status(invoice.status)
                        + " | Hạn: " + value(invoice.due_date)
        );

        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
