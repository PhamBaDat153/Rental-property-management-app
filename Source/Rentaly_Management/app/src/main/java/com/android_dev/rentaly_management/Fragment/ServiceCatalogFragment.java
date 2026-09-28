package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.DTO.ServiceRequest;
import com.android_dev.rentaly_management.R;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCatalogFragment extends Fragment {
    private final List<Service> services = new ArrayList<>();
    private ArrayAdapter<Service> adapter;
    private ProgressBar loading;
    private TextView empty;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_service_catalog, container, false);
        loading = view.findViewById(R.id.service_loading); empty = view.findViewById(R.id.service_empty);
        view.findViewById(R.id.service_catalog_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.service_add).setOnClickListener(v -> showForm(null));
        adapter = new ArrayAdapter<Service>(requireContext(), R.layout.item_service, R.id.service_item_name, services) {
            @NonNull @Override public View getView(int position, View convert, @NonNull ViewGroup parent) {
                View row = super.getView(position, convert, parent); Service service = getItem(position);
                ((TextView) row.findViewById(R.id.service_item_name)).setText(service.name);
                ((TextView) row.findViewById(R.id.service_item_status)).setText(status(service.room_status));
                ((TextView) row.findViewById(R.id.service_item_details)).setText(service.unit + " | " + service.calculation_method + " | " + service.default_unit_price);
                return row;
            }
        };
        ListView list = view.findViewById(R.id.service_list); list.setAdapter(adapter); list.setOnItemClickListener((p, row, position, id) -> showActions(services.get(position)));
        loadServices(); return view;
    }
    private void loadServices() { loading.setVisibility(View.VISIBLE); ApiClient.api.services().enqueue(new Callback<List<Service>>() {
        public void onResponse(Call<List<Service>> c, Response<List<Service>> r) { loading.setVisibility(View.GONE); if (r.isSuccessful() && r.body() != null) { services.clear(); services.addAll(r.body()); adapter.notifyDataSetChanged(); empty.setVisibility(services.isEmpty() ? View.VISIBLE : View.GONE); } else error("Không thể tải danh mục dịch vụ"); }
        public void onFailure(Call<List<Service>> c, Throwable t) { loading.setVisibility(View.GONE); error("Không thể kết nối đến máy chủ"); }
    }); }
    private void showActions(Service service) { new AlertDialog.Builder(requireContext()).setTitle(service.name).setItems(new String[]{"Sửa dịch vụ", "Xóa dịch vụ"}, (d, which) -> { if (which == 0) showForm(service); else new AlertDialog.Builder(requireContext()).setTitle("Xóa dịch vụ?").setMessage("Dịch vụ có thể đang được gán cho các phòng.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (x, y) -> ApiClient.api.deleteService(service.service_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { error("Đã xóa dịch vụ"); loadServices(); } else error(r.code() == 409 ? "Không thể xóa dịch vụ đang được sử dụng" : "Không thể xóa dịch vụ"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } })).show(); }).show(); }
    private void showForm(Service current) {
        View form = getLayoutInflater().inflate(R.layout.dialog_create_service, null);
        EditText name = form.findViewById(R.id.service_form_name), unit = form.findViewById(R.id.service_form_unit), calculation = form.findViewById(R.id.service_form_calculation), price = form.findViewById(R.id.service_form_price);
        RadioButton available = form.findViewById(R.id.service_form_available), unavailable = form.findViewById(R.id.service_form_unavailable);
        if (current != null) { name.setText(current.name); unit.setText(current.unit); calculation.setText(current.calculation_method); price.setText(String.valueOf(current.default_unit_price)); }
        ("UNAVAILABLE".equalsIgnoreCase(current == null ? "AVAILABLE" : current.room_status) ? unavailable : available).setChecked(true);
        new AlertDialog.Builder(requireContext()).setTitle(current == null ? "Thêm dịch vụ" : "Sửa dịch vụ").setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", (d, w) -> {
            try { if (blank(name) || blank(unit) || blank(calculation)) throw new IllegalArgumentException(); BigDecimal value = new BigDecimal(price.getText().toString().trim()); if (value.signum() < 0) throw new IllegalArgumentException(); ServiceRequest request = new ServiceRequest(name.getText().toString().trim(), unit.getText().toString().trim(), calculation.getText().toString().trim(), value, available.isChecked() ? "AVAILABLE" : "UNAVAILABLE"); Call<Service> call = current == null ? ApiClient.api.createService(request) : ApiClient.api.updateService(current.service_id.toString(), request); call.enqueue(new Callback<Service>() { public void onResponse(Call<Service> c, Response<Service> r) { if (r.isSuccessful()) { error("Đã lưu dịch vụ"); loadServices(); } else error("Không thể lưu dịch vụ"); } public void onFailure(Call<Service> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); } catch (Exception e) { error("Vui lòng kiểm tra thông tin dịch vụ"); }
        }).show();
    }
    private boolean blank(EditText field) { return field.getText().toString().trim().isEmpty(); }
    private String status(String value) { return "AVAILABLE".equalsIgnoreCase(value) ? "Đang khả dụng" : "Không khả dụng"; }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
