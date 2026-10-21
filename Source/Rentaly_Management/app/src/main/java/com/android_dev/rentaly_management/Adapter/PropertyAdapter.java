package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;
import java.util.List;

public class PropertyAdapter extends ArrayAdapter<Object> {
    /** Các hàm định dạng dữ liệu do PropertiesFragment cung cấp. */
    public interface Formatter {
        String location(Object id);
        String status(String value);
        String roomName(Room room);
        String address(Location location);
    }

    private final Formatter formatter;

    public PropertyAdapter(
            Context context,
            List<Object> properties,
            Formatter formatter
    ) {
        super(context, 0, properties);
        this.formatter = formatter;
    }

    /**
     * Hiển thị Location hoặc Room trong cùng một danh sách.
     * Hai loại dữ liệu dùng chung item_resource.xml nhưng có nội dung khác nhau.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_resource, parent, false);
        }

        TextView primary = row.findViewById(R.id.resource_primary);
        TextView secondary = row.findViewById(R.id.resource_secondary);
        TextView meta = row.findViewById(R.id.resource_meta);
        Object item = getItem(position);

        if (item instanceof Location) { Location l = (Location) item; primary.setText(value(l.location_code)); secondary.setText(formatter.address(l)); meta.setText("Trạng thái: " + formatter.status(l.status)); }
        else {
            Room r = (Room) item;
            primary.setText(formatter.roomName(r));
            secondary.setText("Mã phòng: " + value(r.room_code)
                    + " · " + formatter.location(r.location_id));
            meta.setText(value(r.rent_price) + " VND/tháng · " + formatter.status(r.status));
        }

        return row;
    }

    private static String value(Object value) { return value == null ? "Chưa cập nhật" : String.valueOf(value); }
}
