package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.Apis.ProvinceApiClient;
import com.android_dev.rentaly_management.DTO.Province;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.RoomRequest;
import com.android_dev.rentaly_management.R;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MultipartBody;

public class PropertiesFragment extends Fragment {
    private static final int IMAGE_PICKER = 401;
    private final List<Uri> selectedImages = new ArrayList<>();
    private AlertDialog openRoomDialog;
    private final List<Location> locations = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<Object> visible = new ArrayList<>();
    private ArrayAdapter<Object> adapter;
    private EditText search;
    private ProgressBar loading;
    private TextView empty;
    private boolean showingLocations = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_properties, container, false);
        search = view.findViewById(R.id.resource_search);
        loading = view.findViewById(R.id.resource_loading);
        empty = view.findViewById(R.id.resource_empty);
        ListView list = view.findViewById(R.id.resource_list);
        adapter = new ArrayAdapter<Object>(requireContext(), R.layout.item_resource, R.id.resource_primary, visible) {
            @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView primary = row.findViewById(R.id.resource_primary);
                TextView secondary = row.findViewById(R.id.resource_secondary);
                TextView meta = row.findViewById(R.id.resource_meta);
                Object item = visible.get(position);
                if (item instanceof Location) {
                    Location l = (Location) item;
                    primary.setText(value(l.location_code));
                    secondary.setText(address(l));
                    meta.setText("Trạng thái: " + status(l.status));
                } else {
                    Room r = (Room) item;
                    primary.setText(roomName(r));
                    secondary.setText("Mã phòng: " + value(r.room_code));
                    meta.setText("Địa điểm: " + locationLabel(r.location_id) + " | " + status(r.status));
                }
                return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> openDetails(visible.get(position)));
        view.findViewById(R.id.resource_locations).setOnClickListener(v -> selectLocations());
        view.findViewById(R.id.resource_rooms).setOnClickListener(v -> selectRooms());
        view.findViewById(R.id.resource_locations).setSelected(true);
        view.findViewById(R.id.resource_add).setOnClickListener(v -> {
            if (showingLocations) showLocationForm(null); else showRoomForm(null);
        });
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { render(); }
            public void afterTextChanged(Editable s) { }
        });
        loadLocations();
        loadRooms();
        String editLocationId = getArguments() == null ? null : getArguments().getString("edit_location_id");
        String editRoomId = getArguments() == null ? null : getArguments().getString("edit_room_id");
        if (editLocationId != null) ApiClient.api.location(editLocationId).enqueue(new Callback<Location>() {
            public void onResponse(Call<Location> c, Response<Location> r) { if (r.isSuccessful() && r.body() != null) showLocationForm(r.body()); }
            public void onFailure(Call<Location> c, Throwable t) { showError("Không thể tải địa điểm để sửa"); }
        });
        if (editRoomId != null) ApiClient.api.room(editRoomId).enqueue(new Callback<Room>() {
            public void onResponse(Call<Room> c, Response<Room> r) { if (r.isSuccessful() && r.body() != null) showRoomForm(r.body()); }
            public void onFailure(Call<Room> c, Throwable t) { showError("Không thể tải phòng để sửa"); }
        });
        return view;
    }

    private void selectLocations() {
        showingLocations = true;
        search.setHint("Tìm theo mã hoặc địa chỉ");
        TextView title = requireView().findViewById(R.id.resource_list_title);
        title.setText("DANH SÁCH ĐỊA ĐIỂM");
        render();
    }

    private void selectRooms() {
        showingLocations = false;
        search.setHint("Tìm theo tên phòng");
        TextView title = requireView().findViewById(R.id.resource_list_title);
        title.setText("DANH SÁCH PHÒNG");
        render();
    }

    private void loadLocations() {
        setLoading(true);
        ApiClient.api.locations().enqueue(new Callback<List<Location>>() {
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locations.clear(); locations.addAll(response.body()); render();
                } else showError("Không thể tải danh sách địa điểm");
                setLoading(false);
            }
            public void onFailure(Call<List<Location>> call, Throwable t) { setLoading(false); showError("Không thể kết nối đến máy chủ"); }
        });
    }

    private void loadRooms() {
        ApiClient.api.rooms().enqueue(new Callback<List<Room>>() {
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) { rooms.clear(); rooms.addAll(response.body()); render(); }
                else if (!showingLocations) showError("Không thể tải danh sách phòng");
            }
            public void onFailure(Call<List<Room>> call, Throwable t) { if (!showingLocations) showError("Không thể kết nối đến máy chủ"); }
        });
    }

    private void render() {
        if (adapter == null) return;
        String query = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        if (showingLocations) {
            for (Location location : locations) {
                if (locationMatches(location, query)) visible.add(location);
            }
        } else {
            for (Room room : rooms) {
                if (roomMatches(room, query)) visible.add(room);
            }
        }
        adapter.notifyDataSetChanged();
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(showingLocations ? "Chưa có địa điểm phù hợp" : "Chưa có phòng phù hợp");
    }

    static boolean locationMatches(Location l, String query) {
        if (query.isEmpty()) return true;
        return text(l.location_code).contains(query) || text(l.address_line).contains(query)
                || text(l.ward_name).contains(query) || text(l.district_name).contains(query)
                || text(l.province_name).contains(query);
    }

    static boolean roomMatches(Room r, String query) {
        return query.isEmpty() || (!text(r.room_name).isEmpty() && text(r.room_name).contains(query));
    }

    private void openDetails(Object item) {
        Bundle args = new Bundle();
        if (item instanceof Location) {
            args.putString("location_id", ((Location) item).location_id.toString());
            androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.locationDetailFragment, args);
        } else {
            args.putString("room_id", ((Room) item).room_id.toString());
            androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.roomDetailFragment, args);
        }
    }

    private void showLocationForm(Location current) {
        View form = getLayoutInflater().inflate(R.layout.form_location, null);
        EditText code = form.findViewById(R.id.location_code);
        EditText address = form.findViewById(R.id.location_address);
        EditText description = form.findViewById(R.id.location_description);
        Spinner province = form.findViewById(R.id.location_province);
        Spinner district = form.findViewById(R.id.location_district);
        Spinner ward = form.findViewById(R.id.location_ward);
        Spinner status = form.findViewById(R.id.location_status);
        code.setText(current == null ? "" : current.location_code); address.setText(current == null ? "" : current.address_line); description.setText(current == null ? "" : current.description);
        setSpinner(status, new String[]{"AVAILABLE", "UNAVAILABLE"}, current == null ? "AVAILABLE" : current.status);
        province.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(Collections.singletonList("Đang tải tỉnh/thành phố..."))));
        district.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Chọn tỉnh trước"}));
        ward.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Chọn quận/huyện trước"}));
        loadProvinces(province, district, ward, current);
        province.setOnItemSelectedListener(new SimpleSelectListener(() -> loadDistricts(province, district, ward)));
        district.setOnItemSelectedListener(new SimpleSelectListener(() -> loadWards(province, district, ward)));
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle(current == null ? "Thêm địa điểm" : "Sửa địa điểm")
                .setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Province p = selectedProvince(province); Province d = selectedProvince(district); Province w = selectedProvince(ward);
            if (blank(code) || blank(address) || p == null) { showError("Vui lòng nhập mã, địa chỉ và tỉnh/thành phố"); return; }
            LocationRequest request = new LocationRequest(code.getText().toString().trim(), address.getText().toString().trim(), d == null ? null : d.name, w == null ? null : w.name, p.name, status.getSelectedItem().toString(), text(description));
            dialog.dismiss();
            if (current == null) ApiClient.api.createLocation(request).enqueue(locationCallback("Đã thêm địa điểm"));
            else ApiClient.api.updateLocation(current.location_id.toString(), request).enqueue(locationCallback("Đã cập nhật địa điểm"));
        }));
        dialog.show();
    }

    private Callback<Location> locationCallback(String success) {
        return new Callback<Location>() {
            public void onResponse(Call<Location> c, Response<Location> r) { if (r.isSuccessful()) { showError(success); loadLocations(); } else showError(error(r, "Không thể lưu địa điểm")); }
            public void onFailure(Call<Location> c, Throwable t) { showError("Không thể kết nối đến máy chủ"); }
        };
    }

    private void showRoomForm(Room current) {
        if (locations.isEmpty()) { showError("Cần có địa điểm trước khi thêm phòng"); return; }
        selectedImages.clear();
        View form = getLayoutInflater().inflate(R.layout.form_room, null);
        Spinner location = form.findViewById(R.id.room_location); EditText code = form.findViewById(R.id.room_code); EditText name = form.findViewById(R.id.room_name);
        EditText floor = form.findViewById(R.id.room_floor); EditText area = form.findViewById(R.id.room_area); EditText occupants = form.findViewById(R.id.room_occupants); EditText rent = form.findViewById(R.id.room_rent); EditText description = form.findViewById(R.id.room_description); RadioGroup status = form.findViewById(R.id.room_status);
        code.setText(current == null ? "" : current.room_code); name.setText(current == null ? "" : current.room_name); floor.setText(current == null ? "" : value(current.floor)); area.setText(current == null ? "" : value(current.area_m2)); occupants.setText(current == null ? "1" : value(current.max_occupants)); rent.setText(current == null ? "0" : value(current.rent_price)); description.setText(current == null ? "" : current.description);
        ((RadioButton) form.findViewById("UNAVAILABLE".equals(current == null ? "AVAILABLE" : current.status) ? R.id.room_status_unavailable : R.id.room_status_available)).setChecked(true);
        List<UUID> locationIds = new ArrayList<>(); List<String> labels = new ArrayList<>(); for (Location l : locations) { locationIds.add(l.location_id); labels.add(value(l.location_code) + " - " + address(l)); }
        location.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)); if (current != null) location.setSelection(locationIds.indexOf(current.location_id));
        TextView imageStatus = form.findViewById(R.id.room_images_status);
        imageStatus.setTag(form.findViewById(R.id.room_image_preview));
        if (current != null && current.image_urls != null && !current.image_urls.isEmpty()) imageStatus.setText("Đang giữ " + current.image_urls.size() + " hình ảnh cũ; chọn ảnh mới để tải bổ sung");
        form.findViewById(R.id.room_choose_images).setOnClickListener(v -> chooseImages(imageStatus));
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle(current == null ? "Thêm phòng" : "Sửa phòng")
                .setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create(); openRoomDialog = dialog;
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                if (blank(code) || Integer.parseInt(occupants.getText().toString()) < 1 || new BigDecimal(rent.getText().toString()).compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
                BigDecimal areaValue = blank(area) ? null : new BigDecimal(area.getText().toString());
                if (areaValue != null && areaValue.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
                int locationPosition = location.getSelectedItemPosition();
                if (locationPosition < 0 || locationPosition >= locationIds.size()) {
                    showError("Vui lòng chọn địa điểm");
                    return;
                }
                if (status.getCheckedRadioButtonId() == -1) {
                    showError("Vui lòng chọn trạng thái phòng");
                    return;
                }
                String statusValue = status.getCheckedRadioButtonId() == R.id.room_status_unavailable ? "UNAVAILABLE" : "AVAILABLE";
                RoomRequest request = new RoomRequest(locationIds.get(locationPosition), code.getText().toString().trim(), optional(name), number(floor), areaValue, Integer.valueOf(occupants.getText().toString()), new BigDecimal(rent.getText().toString()), statusValue, optional(description));
                RequestBody body = RequestBody.create(new Gson().toJson(request), MediaType.parse("application/json"));
                List<MultipartBody.Part> imageParts = imageParts();
                Callback<Room> callback = roomCallback(current == null ? "Đã thêm phòng" : "Đã cập nhật phòng");
                dialog.dismiss();
                if (current == null) ApiClient.api.createRoom(body, imageParts).enqueue(callback);
                else ApiClient.api.updateRoom(current.room_id.toString(), body, imageParts).enqueue(callback);
            } catch (Exception e) { showError("Vui lòng kiểm tra mã, số người, diện tích và giá thuê"); }
        }));
        dialog.show();
    }

    private Callback<Room> roomCallback(String success) {
        return new Callback<Room>() {
            public void onResponse(Call<Room> c, Response<Room> r) {
                if (r.isSuccessful()) { showError(success); loadRooms(); }
                else showError(error(r, "Không thể lưu phòng (HTTP " + r.code() + ")"));
            }
            public void onFailure(Call<Room> c, Throwable t) { showError("Không thể kết nối đến máy chủ: " + t.getMessage()); }
        };
    }

    private void confirmLocationDelete(Location l) { new AlertDialog.Builder(requireContext()).setTitle("Xóa địa điểm?").setMessage("Thao tác này không thể hoàn tác.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> ApiClient.api.deleteLocation(l.location_id.toString()).enqueue(deleteCallback("Đã xóa địa điểm", true))).show(); }
    private void confirmRoomDelete(Room r) { new AlertDialog.Builder(requireContext()).setTitle("Xóa phòng?").setMessage("Thao tác này không thể hoàn tác.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> ApiClient.api.deleteRoom(r.room_id.toString()).enqueue(deleteCallback("Đã xóa phòng", false))).show(); }
    private Callback<Void> deleteCallback(String success, boolean location) { return new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { showError(success); if (location) loadLocations(); else loadRooms(); } else showError(error(r, r.code() == 409 ? "Không thể xóa vì tài nguyên đang được sử dụng" : "Không thể xóa tài nguyên")); } public void onFailure(Call<Void> c, Throwable t) { showError("Không thể kết nối đến máy chủ"); } }; }

    private void setLoading(boolean value) { loading.setVisibility(value ? View.VISIBLE : View.GONE); }
    private String locationLabel(UUID id) { for (Location l : locations) if (l.location_id != null && l.location_id.equals(id)) return value(l.location_code); return id == null ? "Chưa cập nhật" : id.toString(); }
    private String address(Location l) { return value(l.address_line) + ", " + value(l.ward_name) + ", " + value(l.district_name) + ", " + value(l.province_name); }
    static String roomName(Room r) { return text(r.room_name).isEmpty() ? "Chưa đặt tên phòng" : r.room_name; }
    private String status(String value) { return "AVAILABLE".equalsIgnoreCase(value) ? "Đang sử dụng" : "Không khả dụng"; }
    private static String text(Object value) { return value == null ? "" : value.toString().trim().toLowerCase(Locale.ROOT); }
    private static String value(Object value) { return value == null || value.toString().trim().isEmpty() ? "Chưa cập nhật" : value.toString(); }
    private static boolean blank(EditText e) { return e.getText().toString().trim().isEmpty(); }
    private static String optional(EditText e) { String value = e.getText().toString().trim(); return value.isEmpty() ? null : value; }
    private static String text(EditText e) { return e.getText().toString().trim().isEmpty() ? null : e.getText().toString().trim(); }
    private static Integer number(EditText e) { return e.getText().toString().trim().isEmpty() ? null : Integer.valueOf(e.getText().toString().trim()); }
    private String error(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String message = new org.json.JSONObject(response.errorBody().string()).optString("message");
                if (!message.isBlank()) return message;
            }
        } catch (Exception ignored) { }
        return fallback;
    }
    private void showError(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }

    private void setSpinner(Spinner spinner, String[] values, String selected) { spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, values)); for (int i = 0; i < values.length; i++) if (values[i].equalsIgnoreCase(selected)) spinner.setSelection(i); }
    private Province selectedProvince(Spinner spinner) { Object value = spinner.getSelectedItem(); return value instanceof Province ? (Province) value : null; }
    private void loadProvinces(Spinner p, Spinner d, Spinner w, Location current) { ProvinceApiClient.api.provinces().enqueue(new Callback<List<Province>>() { public void onResponse(Call<List<Province>> c, Response<List<Province>> r) { if (r.isSuccessful() && r.body() != null) { p.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, r.body())); if (current != null) selectName(p, current.province_name); } else showError("Không thể tải danh sách tỉnh/thành phố"); } public void onFailure(Call<List<Province>> c, Throwable t) { showError("Không thể kết nối API địa chỉ"); } }); }
    private void loadDistricts(Spinner p, Spinner d, Spinner w) { Province selected = selectedProvince(p); if (selected == null) return; ProvinceApiClient.api.legacyProvince(selected.code, 2).enqueue(new Callback<Province>() { public void onResponse(Call<Province> c, Response<Province> r) { if (r.isSuccessful() && r.body() != null) { d.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, r.body().districts == null ? new ArrayList<>() : r.body().districts)); w.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Chọn quận/huyện trước"})); } else showError("Không thể tải quận/huyện"); } public void onFailure(Call<Province> c, Throwable t) { showError("Không thể tải quận/huyện"); } }); }
    private void loadWards(Spinner p, Spinner d, Spinner w) { Province selected = selectedProvince(d); if (selected == null) return; ProvinceApiClient.api.legacyDistrict(selected.code, 2).enqueue(new Callback<Province>() { public void onResponse(Call<Province> c, Response<Province> r) { if (r.isSuccessful() && r.body() != null) w.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, r.body().wards == null ? new ArrayList<>() : r.body().wards)); else showError("Không thể tải phường/xã"); } public void onFailure(Call<Province> c, Throwable t) { showError("Không thể tải phường/xã"); } }); }
    private void selectName(Spinner spinner, String name) { if (name == null) return; for (int i = 0; i < spinner.getCount(); i++) { Object item = spinner.getItemAtPosition(i); if (item instanceof Province && name.equals(((Province) item).name)) spinner.setSelection(i); } }
    private void chooseImages(TextView status) { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("image/*"); intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); intent.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(intent, IMAGE_PICKER); status.setTag(status); }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) { super.onActivityResult(requestCode, resultCode, data); if (requestCode != IMAGE_PICKER || resultCode != -1 || data == null) return; selectedImages.clear(); if (data.getClipData() != null) for (int i = 0; i < Math.min(6, data.getClipData().getItemCount()); i++) selectedImages.add(data.getClipData().getItemAt(i).getUri()); else if (data.getData() != null) selectedImages.add(data.getData()); if (data.getClipData() != null && data.getClipData().getItemCount() > 6) showError("Chỉ được chọn tối đa 6 hình ảnh"); if (openRoomDialog != null) { TextView status = openRoomDialog.findViewById(R.id.room_images_status); if (status != null) { status.setText("Đã chọn " + selectedImages.size() + "/6 hình ảnh"); status.setOnClickListener(v -> { selectedImages.clear(); status.setText("Đã bỏ chọn hình ảnh"); }); android.widget.LinearLayout preview = openRoomDialog.findViewById(R.id.room_image_preview); preview.removeAllViews(); for (int i = 0; i < selectedImages.size(); i++) { final int index = i; TextView chip = new TextView(requireContext()); chip.setText("Ảnh " + (i + 1) + "  x"); chip.setContentDescription("Bỏ chọn ảnh " + (i + 1)); chip.setPadding(12, 8, 12, 8); chip.setOnClickListener(v -> { selectedImages.remove(index); v.setVisibility(View.GONE); status.setText("Đã chọn " + selectedImages.size() + "/6 hình ảnh"); }); preview.addView(chip); } } } }
    private List<MultipartBody.Part> imageParts() { List<MultipartBody.Part> result = new ArrayList<>(); for (Uri uri : selectedImages) try { String type = requireContext().getContentResolver().getType(uri); if (type == null || !type.startsWith("image/")) { showError("Chỉ hỗ trợ tệp hình ảnh"); continue; } byte[] bytes = readBytes(uri); if (bytes.length == 0) { showError("Không thể đọc hình ảnh đã chọn"); continue; } result.add(MultipartBody.Part.createFormData("images", "room-image", RequestBody.create(bytes, MediaType.parse(type)))); } catch (Exception ignored) { showError("Không thể đọc hình ảnh đã chọn"); } return result; }
    private byte[] readBytes(Uri uri) throws Exception { java.io.InputStream input = requireContext().getContentResolver().openInputStream(uri); java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count; while (input != null && (count = input.read(buffer)) != -1) output.write(buffer, 0, count); if (input != null) input.close(); return output.toByteArray(); }
    private static class SimpleSelectListener implements android.widget.AdapterView.OnItemSelectedListener { private final Runnable action; SimpleSelectListener(Runnable action) { this.action = action; } public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { action.run(); } public void onNothingSelected(android.widget.AdapterView<?> p) { } }
}
