package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.MeterReading;
import com.android_dev.rentaly_management.R;

import java.net.URL;
import java.util.List;

/** Hiển thị lịch sử các lần ghi chỉ số của đồng hồ. */
public class MeterReadingAdapter extends BaseAdapter {

    public interface Listener {
        void onClick(MeterReading reading);
    }

    private final Context context;
    private final List<MeterReading> readings;
    private final Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MeterReadingAdapter(
            Context context,
            List<MeterReading> readings,
            Listener listener
    ) {
        this.context = context;
        this.readings = readings;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return readings.size();
    }

    @Override
    public MeterReading getItem(int position) {
        return readings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Tạo item, hiển thị số liệu và tải ảnh bằng luồng nền nếu có. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context)
                    .inflate(R.layout.item_meter_reading, parent, false);
        }

        MeterReading reading = getItem(position);
        ImageView evidence = row.findViewById(R.id.meter_reading_evidence);
        TextView details = row.findViewById(R.id.meter_reading_details);

        evidence.setTag(reading.evidence_url);
        evidence.setImageResource(R.drawable.profile_icon);
        details.setText(
                value(reading.reading_at)
                        + "\nCũ: " + value(reading.previous_value)
                        + " | Mới: " + value(reading.current_value)
                        + "\nDùng: " + value(reading.quantity)
        );

        loadEvidence(evidence, reading.evidence_url);
        row.setOnClickListener(view -> listener.onClick(reading));
        return row;
    }

    private void loadEvidence(ImageView image, String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                android.graphics.Bitmap bitmap =
                        BitmapFactory.decodeStream(new URL(url).openStream());
                if (bitmap != null) {
                    mainHandler.post(() -> {
                        if (url.equals(image.getTag())) {
                            image.setImageBitmap(bitmap);
                        }
                    });
                }
            } catch (Exception ignored) {
                // Giữ ảnh mặc định nếu URL không tải được.
            }
        }).start();
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
