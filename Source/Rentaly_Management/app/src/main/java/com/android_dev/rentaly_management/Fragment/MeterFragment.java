package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.android_dev.rentaly_management.DTO.*;
import com.android_dev.rentaly_management.R;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeterFragment extends Fragment {
    private final List<Meter> meters = new ArrayList<>();
    private ArrayAdapter<Meter> adapter;
    private String roomId;
    private ProgressBar loading;
    private TextView empty;
    private Uri evidenceUri;
    private ImageView evidencePreview;
    private static final int EVIDENCE_PICKER = 804;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_meter, container, false);
        roomId = requireArguments().getString("room_id");
        loading = view.findViewById(R.id.meter_loading); empty = view.findViewById(R.id.meter_empty);
        ((TextView) view.findViewById(R.id.meter_room)).setText("Phòng: " + roomId);
        view.findViewById(R.id.meter_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        adapter = new ArrayAdapter<Meter>(requireContext(), R.layout.item_meter, R.id.meter_item_name, meters) {
            @NonNull @Override public View getView(int position, View convert, @NonNull ViewGroup parent) {
                View row = super.getView(position, convert, parent); Meter meter = getItem(position);
                ((TextView) row.findViewById(R.id.meter_item_name)).setText(meter.meter_type);
                ((TextView) row.findViewById(R.id.meter_item_status)).setText(status(meter.status)); return row;
            }
        };
        ListView list = view.findViewById(R.id.meter_list); list.setAdapter(adapter);
        list.setOnItemClickListener((p, row, position, id) -> showMeterActions(meters.get(position)));
        view.findViewById(R.id.meter_add).setOnClickListener(v -> showMeterForm(null)); loadMeters(); return view;
    }
    private void loadMeters() { loading.setVisibility(View.VISIBLE); ApiClient.api.meters(roomId).enqueue(new Callback<List<Meter>>() {
        public void onResponse(Call<List<Meter>> c, Response<List<Meter>> r) { loading.setVisibility(View.GONE); if (r.isSuccessful() && r.body() != null) { meters.clear(); meters.addAll(r.body()); adapter.notifyDataSetChanged(); empty.setVisibility(meters.isEmpty() ? View.VISIBLE : View.GONE); empty.setText("Chưa có đồng hồ. Nhấn + để thêm đồng hồ cho phòng này."); } else error("Không thể tải danh sách đồng hồ"); }
        public void onFailure(Call<List<Meter>> c, Throwable t) { loading.setVisibility(View.GONE); error("Không thể kết nối đến máy chủ"); }
    }); }
    private void showMeterForm(Meter current) {
        View form = getLayoutInflater().inflate(R.layout.dialog_create_meter, null);
        EditText type = form.findViewById(R.id.meter_form_type); RadioButton available = form.findViewById(R.id.meter_form_available); RadioButton unavailable = form.findViewById(R.id.meter_form_unavailable);
        type.setText(current == null ? "" : current.meter_type); ("UNAVAILABLE".equalsIgnoreCase(current == null ? "AVAILABLE" : current.status) ? unavailable : available).setChecked(true);
        new AlertDialog.Builder(requireContext()).setTitle(current == null ? "Thêm đồng hồ" : "Sửa đồng hồ").setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", (d, w) -> {
            if (type.getText().toString().trim().isEmpty()) { error("Vui lòng nhập loại đồng hồ"); return; }
            MeterRequest request = new MeterRequest(java.util.UUID.fromString(roomId), type.getText().toString().trim(), available.isChecked() ? "AVAILABLE" : "UNAVAILABLE");
            Call<Meter> call = current == null ? ApiClient.api.createMeter(request) : ApiClient.api.updateMeter(current.meter_id.toString(), request);
            call.enqueue(new Callback<Meter>() { public void onResponse(Call<Meter> c, Response<Meter> r) { if (r.isSuccessful()) { error("Đã lưu đồng hồ"); loadMeters(); } else error("Không thể lưu đồng hồ"); } public void onFailure(Call<Meter> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } });
        }).show();
    }
    private void showMeterActions(Meter meter) { new AlertDialog.Builder(requireContext()).setTitle(meter.meter_type).setItems(new String[]{"Xem lịch sử chỉ số", "Sửa đồng hồ", "Xóa đồng hồ"}, (d, which) -> { if (which == 0) showReadings(meter); else if (which == 1) showMeterForm(meter); else new AlertDialog.Builder(requireContext()).setTitle("Xóa đồng hồ?").setMessage("Các chỉ số của đồng hồ cũng sẽ bị xóa.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (x, y) -> ApiClient.api.deleteMeter(meter.meter_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { error("Đã xóa đồng hồ"); loadMeters(); } else error("Không thể xóa đồng hồ"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } })).show(); }).show(); }
    private void showReadings(Meter meter) { ApiClient.api.meterReadings(meter.meter_id.toString()).enqueue(new Callback<List<MeterReading>>() { public void onResponse(Call<List<MeterReading>> c, Response<List<MeterReading>> r) { if (!r.isSuccessful() || r.body() == null) { error("Không thể tải chỉ số"); return; } List<String> labels = new ArrayList<>(); for (MeterReading value : r.body()) labels.add(value.reading_at + " | Cũ: " + value.previous_value + " | Mới: " + value.current_value + " | Dùng: " + value.quantity); labels.add("+ Thêm chỉ số"); new AlertDialog.Builder(requireContext()).setTitle("Lịch sử chỉ số: " + meter.meter_type).setItems(labels.toArray(new String[0]), (d, which) -> { if (which == labels.size() - 1) showReadingForm(meter, null); else showReadingActions(meter, r.body().get(which)); }).setNegativeButton("Đóng", null).show(); } public void onFailure(Call<List<MeterReading>> c, Throwable t) { error("Không thể tải chỉ số"); } }); }
    private void showReadingActions(Meter meter, MeterReading reading) { new AlertDialog.Builder(requireContext()).setTitle("Chỉ số đọc").setItems(new String[]{"Sửa chỉ số", "Xóa chỉ số"}, (d, which) -> { if (which == 0) showReadingForm(meter, reading); else new AlertDialog.Builder(requireContext()).setTitle("Xóa chỉ số?").setMessage("Chỉ số đã dùng để lập hóa đơn sẽ không thể xóa.").setNegativeButton("Hủy", null).setPositiveButton("Xóa", (x, y) -> ApiClient.api.deleteMeterReading(reading.reading_id.toString()).enqueue(new Callback<Void>() { public void onResponse(Call<Void> c, Response<Void> r) { if (r.isSuccessful()) { error("Đã xóa chỉ số"); showReadings(meter); } else error(r.code() == 409 ? "Chỉ số đã được dùng để tính hóa đơn" : "Không thể xóa chỉ số"); } public void onFailure(Call<Void> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } })).show(); }).show(); }
    private void showReadingForm(Meter meter, MeterReading old) {
        evidenceUri = null; View form = getLayoutInflater().inflate(R.layout.dialog_create_meter_reading, null);
        EditText at = form.findViewById(R.id.reading_form_at), previous = form.findViewById(R.id.reading_form_previous), current = form.findViewById(R.id.reading_form_current), quantity = form.findViewById(R.id.reading_form_quantity), note = form.findViewById(R.id.reading_form_note);
        Button evidence = form.findViewById(R.id.reading_form_evidence); evidencePreview = form.findViewById(R.id.reading_form_preview);
        at.setText(old == null ? LocalDateTime.now().withNano(0).toString() : old.reading_at.toString()); previous.setText(value(old == null ? null : old.previous_value)); current.setText(value(old == null ? null : old.current_value)); quantity.setText(value(old == null ? null : old.quantity)); note.setText(old == null ? "" : value(old.note)); evidence.setOnClickListener(v -> chooseEvidence());
        new AlertDialog.Builder(requireContext()).setTitle(old == null ? "Thêm chỉ số" : "Sửa chỉ số").setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", (d, w) -> saveReading(meter, old, at, previous, current, quantity, note)).show();
    }
    private void saveReading(Meter meter, MeterReading old, EditText at, EditText previous, EditText current, EditText quantity, EditText note) { try { MeterReadingRequest request = new MeterReadingRequest(meter.meter_id, LocalDateTime.parse(at.getText().toString().trim()), new BigDecimal(current.getText().toString()), new BigDecimal(previous.getText().toString()), new BigDecimal(quantity.getText().toString()), old == null ? null : old.evidence_url, optional(note)); RequestBody body = RequestBody.create(ApiClient.gson.toJson(request), MediaType.parse("application/json")); MultipartBody.Part image = evidencePart(); Call<MeterReading> call = old == null ? ApiClient.api.createMeterReading(body, image) : ApiClient.api.updateMeterReading(old.reading_id.toString(), body, image); call.enqueue(new Callback<MeterReading>() { public void onResponse(Call<MeterReading> c, Response<MeterReading> r) { if (r.isSuccessful()) error("Đã lưu chỉ số"); else error("Không thể lưu chỉ số"); } public void onFailure(Call<MeterReading> c, Throwable t) { error("Không thể kết nối đến máy chủ"); } }); } catch (Exception e) { error("Vui lòng kiểm tra thông tin chỉ số"); } }
    private void chooseEvidence() { Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("image/*"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i, EVIDENCE_PICKER); }
    @Override public void onActivityResult(int request, int result, Intent data) { super.onActivityResult(request, result, data); if (request == EVIDENCE_PICKER && result == Activity.RESULT_OK && data != null && data.getData() != null) { evidenceUri = data.getData(); if (evidencePreview != null) { evidencePreview.setImageURI(evidenceUri); evidencePreview.setVisibility(View.VISIBLE); } } }
    private MultipartBody.Part evidencePart() throws Exception { if (evidenceUri == null) return null; String type = requireContext().getContentResolver().getType(evidenceUri); if (type == null || !type.startsWith("image/")) throw new IllegalArgumentException(); InputStream in = requireContext().getContentResolver().openInputStream(evidenceUri); ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count; while (in != null && (count = in.read(buffer)) != -1) out.write(buffer, 0, count); if (in != null) in.close(); return MultipartBody.Part.createFormData("evidence", "meter-evidence", RequestBody.create(out.toByteArray(), MediaType.parse(type))); }
    private String status(String value) { return "AVAILABLE".equalsIgnoreCase(value) ? "Đang hoạt động" : "Không hoạt động"; }
    private String value(Object value) { return value == null ? "0" : value.toString(); }
    private String optional(EditText field) { String value = field.getText().toString().trim(); return value.isEmpty() ? null : value; }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
}
