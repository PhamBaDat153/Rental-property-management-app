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
        load(requireArguments().getString("room_id"), view); return view;
    }
    private void load(String id, View view) { ApiClient.api.room(id).enqueue(new Callback<Room>() { public void onResponse(Call<Room> c, Response<Room> r) { if (r.isSuccessful() && r.body() != null) { room = r.body(); bind(view); loadLocation(view); } else error("Không thể tải phòng"); } public void onFailure(Call<Room> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); }
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
