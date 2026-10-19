package com.android_dev.rentaly_management.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.R;

import java.util.List;

/** Adapter hiển thị các hợp đồng thuê trong ListView. */
public class ContractAdapter extends ArrayAdapter<RentalContract> {

    /** Cung cấp cách hiển thị phòng và trạng thái từ Fragment. */
    public interface Formatter {
        String room(RentalContract contract);

        String status(String value);
    }

    private final Formatter formatter;

    public ContractAdapter(
            Context context,
            List<RentalContract> contracts,
            Formatter formatter
    ) {
        super(context, 0, contracts);
        this.formatter = formatter;
    }

    /** Tạo hoặc tái sử dụng dòng hợp đồng và gán các thông tin cần hiển thị. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_contract, parent, false);
        }

        RentalContract contract = getItem(position);
        if (contract == null) {
            return row;
        }

        TextView primaryText = row.findViewById(R.id.contract_primary);
        TextView secondaryText = row.findViewById(R.id.contract_secondary);
        TextView metaText = row.findViewById(R.id.contract_meta);

        primaryText.setText("Phòng: " + formatter.room(contract));
        secondaryText.setText(
                "Thời hạn: " + value(contract.start_date)
                        + " - " + value(contract.end_date)
        );
        metaText.setText(
                "Giá thuê: " + value(contract.rent_amount)
                        + " | " + formatter.status(contract.status)
        );

        return row;
    }

    private static String value(Object value) {
        return value == null ? "Chưa cập nhật" : String.valueOf(value);
    }
}
