package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Hiển thị người thuê được gắn với một hợp đồng. */
public class ContractTenantAdapter extends BaseAdapter {

    public interface Listener {
        void onEdit(ContractTenant assignment);

        void onDelete(ContractTenant assignment);
    }

    public interface Formatter {
        String tenantName(ContractTenant assignment);

        String status(String value);
    }

    private final Context context;
    private final List<ContractTenant> assignments;
    private final Listener listener;
    private final Formatter formatter;

    public ContractTenantAdapter(
            Context context,
            List<ContractTenant> assignments,
            Listener listener,
            Formatter formatter
    ) {
        this.context = context;
        this.assignments = assignments;
        this.listener = listener;
        this.formatter = formatter;
    }

    @Override
    public int getCount() {
        return assignments.size();
    }

    @Override
    public ContractTenant getItem(int position) {
        return assignments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Tạo item và gắn các callback cho nút Sửa/Xóa. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context)
                    .inflate(R.layout.item_contract_tenant, parent, false);
        }

        ContractTenant assignment = getItem(position);
        TextView info = row.findViewById(R.id.contract_tenant_info);
        Button edit = row.findViewById(R.id.contract_tenant_edit);
        Button delete = row.findViewById(R.id.contract_tenant_delete);

        info.setText(
                formatter.tenantName(assignment)
                        + "\n"
                        + (Boolean.TRUE.equals(assignment.is_representative)
                        ? "Người đại diện" : "Người thuê")
                        + "\nVào ở: " + value(assignment.move_in_date)
                        + " | Rời đi: " + value(assignment.move_out_date)
                        + "\nTrạng thái: " + formatter.status(assignment.status)
        );

        edit.setOnClickListener(view -> listener.onEdit(assignment));
        delete.setOnClickListener(view -> listener.onDelete(assignment));
        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
