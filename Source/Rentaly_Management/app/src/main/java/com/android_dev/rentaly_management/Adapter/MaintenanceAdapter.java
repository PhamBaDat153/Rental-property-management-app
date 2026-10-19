package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Adapter hiển thị các yêu cầu bảo trì. */
public class MaintenanceAdapter extends ArrayAdapter<Maintenance> {

    /** Cho phép Fragment dịch trạng thái bảo trì sang nội dung hiển thị. */
    public interface Formatter {
        String status(String value);
    }

    private final Formatter formatter;

    public MaintenanceAdapter(
            Context context,
            List<Maintenance> maintenanceRequests,
            Formatter formatter
    ) {
        super(context, 0, maintenanceRequests);
        this.formatter = formatter;
    }

    /** Gán tiêu đề, độ ưu tiên, trạng thái và mô tả vào một dòng. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_maintenance, parent, false);
        }

        Maintenance maintenance = getItem(position);
        if (maintenance == null) {
            return row;
        }

        TextView titleText = row.findViewById(R.id.maintenance_item_title);
        TextView metaText = row.findViewById(R.id.maintenance_item_meta);

        titleText.setText(value(maintenance.title));
        metaText.setText(
                value(maintenance.priority)
                        + " | " + formatter.status(maintenance.status)
                        + " | " + value(maintenance.description)
        );

        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
