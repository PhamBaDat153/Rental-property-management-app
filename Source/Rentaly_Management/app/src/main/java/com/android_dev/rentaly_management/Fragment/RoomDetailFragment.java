package com.android_dev.rentaly_management.Fragment;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.RoomRequest;
import com.android_dev.rentaly_management.R;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailFragment extends Fragment {
    private Room room;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);
        view.findViewById(R.id.room_detail_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.room_detail_save).setOnClickListener(v -> save(view));
        view.findViewById(R.id.room_detail_delete).setOnClickListener(v -> { if (room != null) confirmDelete(); });
        view.findViewById(R.id.room_detail_previous).setOnClickListener(v -> ((ViewFlipper) view.findViewById(R.id.room_detail_slider)).showPrevious());
        view.findViewById(R.id.room_detail_next).setOnClickListener(v -> ((ViewFlipper) view.findViewById(R.id.room_detail_slider)).showNext());
        view.findViewById(R.id.room_detail_assign_service).setOnClickListener(v -> { if (room != null) loadServicesForAssignment(view); });
        view.findViewById(R.id.room_detail_meters).setOnClickListener(v -> { if (room != null) { Bundle args = new Bundle(); args.putString("room_id", room.room_id.toString()); NavHostFragment.findNavController(this).navigate(R.id.meterFragment, args); } });
        load(requireArguments().getString("room_id"), view); return view;
    }
    private void load(String id, View view) { ApiClient.api.room(id).enqueue(new Callback<Room>() { public void onResponse(Call<Room> c, Response<Room> r) { if (r.isSuccessful() && r.body() != null) { room = r.body(); bind(view); loadLocation(view); loadRoomServices(view); } else error("Không thể tải phòng"); } public void onFailure(Call<Room> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); }
    private void loadLocation(View view) {
        if (room.location_id == null) return;
        ApiClient.api.location(room.location_id.toString()).enqueue(new Callback<Location>() {
            public void onResponse(Call<Location> c, Response<Location> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Location location = r.body();
                    ((android.widget.TextView) view.findViewById(R.id.room_detail_location)).setText("Địa điểm: " + locationAddress(location));
                }
            }
            public void onFailure(Call<Location> c, Throwable t) { }
        });
    }
    private void bind(View v) {
        ((android.widget.TextView) v.findViewById(R.id.room_detail_location)).setText("Địa điểm: Đang tải địa chỉ...");
        ((EditText) v.findViewById(R.id.room_detail_code)).setText(room.room_code); ((EditText) v.findViewById(R.id.room_detail_name)).setText(room.room_name); ((EditText) v.findViewById(R.id.room_detail_floor)).setText(value(room.floor)); ((EditText) v.findViewById(R.id.room_detail_area)).setText(value(room.area_m2)); ((EditText) v.findViewById(R.id.room_detail_occupants)).setText(value(room.max_occupants)); ((EditText) v.findViewById(R.id.room_detail_rent)).setText(value(room.rent_price)); ((EditText) v.findViewById(R.id.room_detail_description)).setText(room.description);
        ((RadioButton) v.findViewById("UNAVAILABLE".equals(room.status) ? R.id.room_status_unavailable : R.id.room_status_available)).setChecked(true);
        ViewFlipper slider = v.findViewById(R.id.room_detail_slider); slider.removeAllViews(); if (room.image_urls == null || room.image_urls.isEmpty()) { android.widget.TextView empty = new android.widget.TextView(requireContext()); empty.setText("Chưa có hình ảnh phòng"); slider.addView(empty); return; } for (String url : room.image_urls) { ImageView image = new ImageView(requireContext()); image.setContentDescription("Hình ảnh phòng"); image.setScaleType(ImageView.ScaleType.CENTER_CROP); slider.addView(image); new Thread(() -> { try { android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream()); if (bitmap != null) requireActivity().runOnUiThread(() -> image.setImageBitmap(bitmap)); } catch (Exception ignored) { } }).start(); }
    }
    private void save(View v) { try { EditText code = v.findViewById(R.id.room_detail_code), name = v.findViewById(R.id.room_detail_name), floor = v.findViewById(R.id.room_detail_floor), area = v.findViewById(R.id.room_detail_area), occupants = v.findViewById(R.id.room_detail_occupants), rent = v.findViewById(R.id.room_detail_rent), description = v.findViewById(R.id.room_detail_description); RadioGroup status = v.findViewById(R.id.room_detail_status); if (code.getText().toString().trim().isEmpty() || Integer.parseInt(occupants.getText().toString()) < 1 || new BigDecimal(rent.getText().toString()).signum() < 0 || status.getCheckedRadioButtonId() == -1) throw new IllegalArgumentException(); BigDecimal areaValue = area.getText().toString().trim().isEmpty() ? null : new BigDecimal(area.getText().toString()); if (areaValue != null && areaValue.signum() < 0) throw new IllegalArgumentException(); String statusValue = status.getCheckedRadioButtonId() == R.id.room_status_unavailable ? "UNAVAILABLE" : "AVAILABLE"; RoomRequest request = new RoomRequest(room.location_id, code.getText().toString().trim(), optional(name), number(floor), areaValue, Integer.parseInt(occupants.getText().toString()), new BigDecimal(rent.getText().toString()), statusValue, optional(description)); RequestBody body = RequestBody.create(new Gson().toJson(request), MediaType.parse("application/json")); ApiClient.api.updateRoom(room.room_id.toString(), body, new ArrayList<>()).enqueue(new Callback<Room>() { public void onResponse(Call<Room> c, Response<Room> r) { if (r.isSuccessful() && r.body() != null) { room = r.body(); bind(v); error("Đã cập nhật phòng"); } else error("Không thể cập nhật phòng"); } public void onFailure(Call<Room> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); } catch (Exception e) { error("Vui lòng kiểm tra thông tin phòng"); } }
    private void confirmDelete() { new AlertDialog.Builder(requireContext()).setTitle("Xóa phòng?").setMessage("Thao tác này không thể hoàn tác.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> ApiClient.api.deleteRoom(room.room_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) NavHostFragment.findNavController(RoomDetailFragment.this).navigateUp(); else error(r.code() == 409 ? "Không thể xóa vì phòng đang có hợp đồng" : "Không thể xóa phòng"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } })).show(); }
    private void loadRoomServices(View view) { ApiClient.api.roomServices(room.room_id.toString()).enqueue(new Callback<List<com.android_dev.rentaly_management.DTO.RoomService>>() { public void onResponse(Call<List<com.android_dev.rentaly_management.DTO.RoomService>> c, Response<List<com.android_dev.rentaly_management.DTO.RoomService>> r) { if (r.isSuccessful() && r.body() != null) renderServices(view, r.body()); else error("Không thể tải dịch vụ phòng"); } public void onFailure(Call<List<com.android_dev.rentaly_management.DTO.RoomService>> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); }
    private void renderServices(View view, List<com.android_dev.rentaly_management.DTO.RoomService> services) { android.widget.LinearLayout list = view.findViewById(R.id.room_detail_services_list); list.removeAllViews(); view.findViewById(R.id.room_detail_services_empty).setVisibility(services.isEmpty() ? View.VISIBLE : View.GONE); for (com.android_dev.rentaly_management.DTO.RoomService service : services) { android.widget.LinearLayout row = new android.widget.LinearLayout(requireContext()); row.setOrientation(android.widget.LinearLayout.HORIZONTAL); android.widget.TextView label = new android.widget.TextView(requireContext()); label.setText(service.name + " | " + service.unit + " | " + service.calculation_method + " | " + service.default_unit_price + " | " + (Boolean.TRUE.equals(service.is_active) ? "Đang bật" : "Đang tắt")); label.setTextColor(getResources().getColor(R.color.secondary)); label.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)); row.addView(label); android.widget.Button toggle = new android.widget.Button(requireContext()); toggle.setText(Boolean.TRUE.equals(service.is_active) ? "Tắt" : "Bật"); toggle.setOnClickListener(v -> updateRoomService(view, service, !Boolean.TRUE.equals(service.is_active))); row.addView(toggle); android.widget.Button remove = new android.widget.Button(requireContext()); remove.setText("Xóa"); remove.setOnClickListener(v -> new AlertDialog.Builder(requireContext()).setTitle("Bỏ dịch vụ?").setMessage("Thao tác này sẽ xóa liên kết dịch vụ khỏi phòng.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> deleteRoomService(view, service)).show()); row.addView(remove); list.addView(row); } }
    private void loadServicesForAssignment(View view) { ApiClient.api.services().enqueue(new Callback<List<com.android_dev.rentaly_management.DTO.Service>>() { public void onResponse(Call<List<com.android_dev.rentaly_management.DTO.Service>> c, Response<List<com.android_dev.rentaly_management.DTO.Service>> r) { if (!r.isSuccessful() || r.body() == null) { error("Không thể tải danh mục dịch vụ"); return; } ApiClient.api.roomServices(room.room_id.toString()).enqueue(new Callback<List<com.android_dev.rentaly_management.DTO.RoomService>>() { public void onResponse(Call<List<com.android_dev.rentaly_management.DTO.RoomService>> c2, Response<List<com.android_dev.rentaly_management.DTO.RoomService>> r2) { if (!r2.isSuccessful() || r2.body() == null) { error("Không thể tải dịch vụ phòng"); return; } List<com.android_dev.rentaly_management.DTO.Service> available = new ArrayList<>(); for (com.android_dev.rentaly_management.DTO.Service value : r.body()) { boolean assigned = false; for (com.android_dev.rentaly_management.DTO.RoomService current : r2.body()) if (value.service_id.equals(current.service_id)) assigned = true; if (!assigned) available.add(value); } if (available.isEmpty()) { error("Không còn dịch vụ để gán"); return; } String[] labels = new String[available.size()]; for (int i = 0; i < labels.length; i++) labels[i] = available.get(i).name + " (" + available.get(i).unit + ")"; new AlertDialog.Builder(requireContext()).setTitle("Chọn dịch vụ").setItems(labels, (d, which) -> ApiClient.api.assignRoomService(room.room_id.toString(), available.get(which).service_id.toString()).enqueue(simpleRoomServiceCallback(view, "Đã gán dịch vụ"))).setNegativeButton("Hủy", null).show(); } public void onFailure(Call<List<com.android_dev.rentaly_management.DTO.RoomService>> c2, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); } public void onFailure(Call<List<com.android_dev.rentaly_management.DTO.Service>> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); }
    private void updateRoomService(View view, com.android_dev.rentaly_management.DTO.RoomService service, boolean active) { Map<String, Boolean> request = new HashMap<>(); request.put("is_active", active); ApiClient.api.updateRoomService(room.room_id.toString(), service.service_id.toString(), request).enqueue(simpleRoomServiceCallback(view, active ? "Đã bật dịch vụ" : "Đã tắt dịch vụ")); }
    private void deleteRoomService(View view, com.android_dev.rentaly_management.DTO.RoomService service) { ApiClient.api.deleteRoomService(room.room_id.toString(), service.service_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { error("Đã bỏ dịch vụ"); loadRoomServices(view); } else error("Không thể bỏ dịch vụ"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); }
    private Callback<com.android_dev.rentaly_management.DTO.RoomService> simpleRoomServiceCallback(View view, String success) { return new Callback<com.android_dev.rentaly_management.DTO.RoomService>() { public void onResponse(Call<com.android_dev.rentaly_management.DTO.RoomService> c, Response<com.android_dev.rentaly_management.DTO.RoomService> r) { if (r.isSuccessful()) { error(success); loadRoomServices(view); } else error(r.code() == 409 ? "Dịch vụ đã được gán cho phòng" : "Không thể cập nhật dịch vụ"); } public void onFailure(Call<com.android_dev.rentaly_management.DTO.RoomService> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }; }
    private Integer number(EditText e) { return e.getText().toString().trim().isEmpty() ? null : Integer.valueOf(e.getText().toString().trim()); }
    private String optional(EditText e) { String value = e.getText().toString().trim(); return value.isEmpty() ? null : value; }
    private String value(Object value) { return value == null ? "" : value.toString(); }
    private String locationAddress(Location location) {
        return value(location.location_code) + " - " + value(location.address_line)
                + ", " + value(location.ward_name)
                + ", " + value(location.district_name)
                + ", " + value(location.province_name);
    }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
