package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.R;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceFragment extends Fragment {
    private final List<Maintenance> items = new ArrayList<>();
    private ArrayAdapter<Maintenance> adapter;
    private ProgressBar loading;
    private TextView empty;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_maintenance, container, false);
        loading = view.findViewById(R.id.maintenance_loading); empty = view.findViewById(R.id.maintenance_empty);
        view.findViewById(R.id.maintenance_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        adapter = new ArrayAdapter<Maintenance>(requireContext(), R.layout.item_maintenance, R.id.maintenance_item_title, items) {
            @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent); Maintenance item = getItem(position);
                ((TextView) row.findViewById(R.id.maintenance_item_title)).setText(item.title);
                ((TextView) row.findViewById(R.id.maintenance_item_meta)).setText(item.priority + " | " + status(item.status) + " | " + value(item.description)); return row;
            }
        };
        ((ListView) view.findViewById(R.id.maintenance_list)).setAdapter(adapter); load(); return view;
    }
    private void load() { loading.setVisibility(View.VISIBLE); ApiClient.api.maintenance(null, null, null).enqueue(new Callback<List<Maintenance>>() {
        public void onResponse(Call<List<Maintenance>> c, Response<List<Maintenance>> r) { loading.setVisibility(View.GONE); if (r.isSuccessful() && r.body() != null) { items.clear(); items.addAll(r.body()); adapter.notifyDataSetChanged(); empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE); } else error("Không thể tải yêu cầu bảo trì"); }
        public void onFailure(Call<List<Maintenance>> c, Throwable t) { loading.setVisibility(View.GONE); error("Không thể kết nối đến máy chủ"); }
    }); }
    private String status(String value) { if ("PROCESSING".equalsIgnoreCase(value)) return "Đang xử lý"; if ("COMPLETED".equalsIgnoreCase(value)) return "Đã hoàn tất"; return "Đang chờ"; }
    private String value(String value) { return value == null || value.trim().isEmpty() ? "Chưa có mô tả" : value; }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
