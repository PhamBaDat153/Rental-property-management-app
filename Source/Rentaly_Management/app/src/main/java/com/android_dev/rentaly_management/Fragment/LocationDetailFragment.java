package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.Apis.ProvinceApiClient;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.Province;
import com.android_dev.rentaly_management.R;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationDetailFragment extends Fragment {
    private Location location;
    private Spinner province, district, ward;
    private boolean restoring;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_location_detail, container, false);
        province = view.findViewById(R.id.location_detail_province);
        district = view.findViewById(R.id.location_detail_district);
        ward = view.findViewById(R.id.location_detail_ward);
        view.findViewById(R.id.location_detail_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        province.setOnItemSelectedListener(new SelectListener(() -> { if (!restoring) loadDistricts(null); }));
        district.setOnItemSelectedListener(new SelectListener(() -> { if (!restoring) loadWards(null); }));
        view.findViewById(R.id.location_detail_save).setOnClickListener(v -> save(view));
        view.findViewById(R.id.location_detail_delete).setOnClickListener(v -> { if (location != null) confirmDelete(); });
        load(requireArguments().getString("location_id"), view);
        return view;
    }

    private void load(String id, View view) { ApiClient.api.location(id).enqueue(new Callback<Location>() {
        public void onResponse(Call<Location> c, Response<Location> r) {
            if (!r.isSuccessful() || r.body() == null) { error("Không thể tải địa điểm"); return; }
            location = r.body();
            ((EditText) view.findViewById(R.id.location_detail_code)).setText(location.location_code);
            ((EditText) view.findViewById(R.id.location_detail_address)).setText(location.address_line);
            ((EditText) view.findViewById(R.id.location_detail_description)).setText(location.description);
            ((RadioButton) view.findViewById("UNAVAILABLE".equals(location.status) ? R.id.location_status_unavailable : R.id.location_status_available)).setChecked(true);
            setLoading(province, "Đang tải tỉnh/thành phố...");
            ProvinceApiClient.api.provinces().enqueue(new Callback<List<Province>>() {
                public void onResponse(Call<List<Province>> c, Response<List<Province>> r) {
                    if (!r.isSuccessful() || r.body() == null) { error("Không thể tải tỉnh/thành phố"); return; }
                    restoring = true;
                    setItems(province, r.body());
                    Province selected = select(province, location.province_name);
                    restoring = false;
                    if (selected == null) { error("Không tìm thấy tỉnh/thành phố đã lưu"); return; }
                    loadDistricts(location.district_name);
                }
                public void onFailure(Call<List<Province>> c, Throwable t) { error("Không thể kết nối API địa chỉ"); }
            });
        }
        public void onFailure(Call<Location> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
    }); }

    private void loadDistricts(String selectedName) { Province p = selected(province); if (p == null) return; setLoading(district, "Đang tải quận/huyện..."); ProvinceApiClient.api.legacyProvince(p.code, 2).enqueue(new Callback<Province>() {
        public void onResponse(Call<Province> c, Response<Province> r) { if (!r.isSuccessful() || r.body() == null || r.body().districts == null) { error("Không thể tải quận/huyện"); return; } restoring = true; setItems(district, r.body().districts); Province selected = select(district, selectedName); restoring = false; if (selectedName != null && selected == null) { error("Không tìm thấy quận/huyện đã lưu"); return; } if (selected != null) loadWards(location == null ? null : location.ward_name); else setLoading(ward, "Chọn quận/huyện trước"); }
        public void onFailure(Call<Province> c, Throwable t) { error("Không thể tải quận/huyện"); }
    }); }
    private void loadWards(String selectedName) { Province d = selected(district); if (d == null) return; setLoading(ward, "Đang tải phường/xã..."); ProvinceApiClient.api.legacyDistrict(d.code, 2).enqueue(new Callback<Province>() {
        public void onResponse(Call<Province> c, Response<Province> r) { if (!r.isSuccessful() || r.body() == null || r.body().wards == null) { error("Không thể tải phường/xã"); return; } restoring = true; setItems(ward, r.body().wards); Province selected = select(ward, selectedName); restoring = false; if (selectedName != null && selected == null) error("Không tìm thấy phường/xã đã lưu"); }
        public void onFailure(Call<Province> c, Throwable t) { error("Không thể tải phường/xã"); }
    }); }

    private void save(View view) {
        if (location == null) return;
        EditText code = view.findViewById(R.id.location_detail_code), address = view.findViewById(R.id.location_detail_address), description = view.findViewById(R.id.location_detail_description);
        Province p = selected(province), d = selected(district), w = selected(ward);
        RadioGroup status = view.findViewById(R.id.location_detail_status);
        if (code.getText().toString().trim().isEmpty() || address.getText().toString().trim().isEmpty() || p == null || status.getCheckedRadioButtonId() == -1) { error("Vui lòng nhập đủ thông tin địa điểm"); return; }
        String selectedStatus = status.getCheckedRadioButtonId() == R.id.location_status_unavailable ? "UNAVAILABLE" : "AVAILABLE";
        LocationRequest request = new LocationRequest(code.getText().toString().trim(), address.getText().toString().trim(), d == null ? null : d.name, w == null ? null : w.name, p.name, selectedStatus, optional(description));
        ApiClient.api.updateLocation(location.location_id.toString(), request).enqueue(new Callback<Location>() {
            public void onResponse(Call<Location> c, Response<Location> r) { if (r.isSuccessful()) { location = r.body(); error("Đã cập nhật địa điểm"); } else error("Không thể cập nhật địa điểm"); }
            public void onFailure(Call<Location> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
        });
    }
    private void confirmDelete() { new AlertDialog.Builder(requireContext()).setTitle("Xóa địa điểm?").setMessage("Thao tác này không thể hoàn tác.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> ApiClient.api.deleteLocation(location.location_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) NavHostFragment.findNavController(LocationDetailFragment.this).navigateUp(); else error(r.code() == 409 ? "Không thể xóa vì địa điểm đang có phòng" : "Không thể xóa địa điểm"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } })).show(); }
    private Province selected(Spinner s) { Object item = s.getSelectedItem(); return item instanceof Province ? (Province) item : null; }
    private Province select(Spinner spinner, String name) {
        if (name == null || name.isBlank()) return null;
        String expected = normalizeAddressName(name);
        Province fallback = null;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (!(item instanceof Province)) continue;
            Province candidate = (Province) item;
            String actual = normalizeAddressName(candidate.name);
            if (name.trim().equalsIgnoreCase(candidate.name == null ? "" : candidate.name.trim())) {
                spinner.setSelection(i);
                return candidate;
            }
            if (expected.equals(actual) || actual.contains(expected) || expected.contains(actual)) fallback = candidate;
        }
        if (fallback != null) {
            spinner.setSelection(indexOf(spinner, fallback));
            return fallback;
        }
        return null;
    }

    private int indexOf(Spinner spinner, Province target) {
        for (int i = 0; i < spinner.getCount(); i++) if (spinner.getItemAtPosition(i) == target) return i;
        return 0;
    }

    private String normalizeAddressName(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        normalized = normalized.replace("thành phố", "")
                .replace("tp.", "")
                .replace("tp ", "")
                .replace("tỉnh", "")
                .replace("quận", "")
                .replace("huyện", "")
                .replace("thị xã", "")
                .replace("thành thị", "")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }
    private void setItems(Spinner spinner, List<Province> items) { spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)); }
    private void setLoading(Spinner spinner, String message) { spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{message})); }
    private String optional(EditText e) { String value = e.getText().toString().trim(); return value.isEmpty() ? null : value; }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
    private class SelectListener implements android.widget.AdapterView.OnItemSelectedListener { private final Runnable action; SelectListener(Runnable action) { this.action = action; } public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { action.run(); } public void onNothingSelected(android.widget.AdapterView<?> p) { } }
}
