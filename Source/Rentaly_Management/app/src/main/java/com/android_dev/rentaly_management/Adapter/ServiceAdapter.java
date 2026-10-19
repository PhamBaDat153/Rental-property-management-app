package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Adapter hiển thị danh mục dịch vụ. */
public class ServiceAdapter extends ArrayAdapter<Service> {

    /** Cung cấp cách hiển thị trạng thái dịch vụ. */
    public interface Formatter {
        String status(String value);
    }

    private final Formatter formatter;

    public ServiceAdapter(Context context, List<Service> services, Formatter formatter) {
        super(context, 0, services);
        this.formatter = formatter;
    }

    /** Gán tên, trạng thái và thông tin tính giá của dịch vụ vào item. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_service, parent, false);
        }

        Service service = getItem(position);
        if (service == null) {
            return row;
        }

        TextView nameText = row.findViewById(R.id.service_item_name);
        TextView statusText = row.findViewById(R.id.service_item_status);
        TextView detailsText = row.findViewById(R.id.service_item_details);

        nameText.setText(value(service.name));
        statusText.setText(formatter.status(service.room_status));
        detailsText.setText(
                value(service.unit)
                        + " | " + value(service.calculation_method)
                        + " | " + value(service.default_unit_price)
        );

        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
