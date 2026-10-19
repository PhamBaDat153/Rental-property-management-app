package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.Meter;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Adapter hiển thị danh sách đồng hồ của một phòng. */
public class MeterAdapter extends ArrayAdapter<Meter> {

    /** Cung cấp cách hiển thị trạng thái đồng hồ. */
    public interface Formatter {
        String status(String value);
    }

    private final Formatter formatter;

    public MeterAdapter(Context context, List<Meter> meters, Formatter formatter) {
        super(context, 0, meters);
        this.formatter = formatter;
    }

    /** Gán loại đồng hồ và trạng thái vào item_meter.xml. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_meter, parent, false);
        }

        Meter meter = getItem(position);
        if (meter == null) {
            return row;
        }

        TextView nameText = row.findViewById(R.id.meter_item_name);
        TextView statusText = row.findViewById(R.id.meter_item_status);

        nameText.setText(value(meter.meter_type));
        statusText.setText(formatter.status(meter.status));

        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
