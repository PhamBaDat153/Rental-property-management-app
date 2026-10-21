package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.MeterAdapter;
import com.android_dev.rentaly_management.Adapter.MeterReadingAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Meter;
import com.android_dev.rentaly_management.DTO.MeterReading;
import com.android_dev.rentaly_management.DTO.MeterReadingRequest;
import com.android_dev.rentaly_management.DTO.MeterRequest;
import com.android_dev.rentaly_management.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages meters and meter-reading history for a room.
 *
 * This fragment coordinates API requests and UI events.
 * Individual list items are rendered by MeterAdapter and
 * MeterReadingAdapter.
 */
public class MeterFragment extends Fragment {

    private static final String[] METER_LABELS = {"Điện", "Nước"};
    private static final String[] METER_TYPES = {"ELECTRICITY", "WATER"};

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int EVIDENCE_PICKER_REQUEST = 804;

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String UNKNOWN_VALUE = "0";

    private static final String MIME_JSON = "application/json";

    private static final String MESSAGE_LOAD_METERS_FAILED =
            "Không thể tải danh sách đồng hồ";

    private static final String MESSAGE_CONNECTION_FAILED =
            "Không thể kết nối đến máy chủ";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Meter> meters = new ArrayList<>();

    private MeterAdapter meterAdapter;

    private String roomId;

    private Uri evidenceUri;

    private ImageView evidencePreview;

    private AlertDialog readingsDialog;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private ProgressBar loadingIndicator;

    private TextView emptyState;

    // -------------------------------------------------------------------------
    // Fragment lifecycle
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(
                R.layout.fragment_meter,
                container,
                false
        );

        roomId = requireArguments().getString("room_id");

        bindViews();
        setupRoomInformation();
        setupClickListeners();
        setupMeterList();

        loadMeters();

        return rootView;
    }

    private void bindViews() {
        loadingIndicator = rootView.findViewById(
                R.id.meter_loading
        );

        emptyState = rootView.findViewById(
                R.id.meter_empty
        );
    }

    private void setupRoomInformation() {
        TextView roomText = rootView.findViewById(
                R.id.meter_room
        );

        roomText.setText("Phòng: " + roomId);
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.meter_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );

        rootView.findViewById(R.id.meter_add)
                .setOnClickListener(
                        view -> showMeterForm(null)
                );
    }

    // -------------------------------------------------------------------------
    // Meter list
    // -------------------------------------------------------------------------

    private void setupMeterList() {
        ListView meterList = rootView.findViewById(
                R.id.meter_list
        );

        meterAdapter = new MeterAdapter(
                requireContext(),
                meters,
                this::getMeterStatusLabel
        );

        meterList.setAdapter(meterAdapter);

        meterList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    if (position >= 0 && position < meters.size()) {
                        showMeterActions(meters.get(position));
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Load meters
    // -------------------------------------------------------------------------

    /**
     * Loads meters associated with the current room.
     */
    private void loadMeters() {
        setLoading(true);

        ApiClient.api.meters(roomId).enqueue(
                new Callback<List<Meter>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Meter>> call,
                            @NonNull Response<List<Meter>> response
                    ) {
                        if (rootView == null || !isAdded()) {
                            return;
                        }

                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(MESSAGE_LOAD_METERS_FAILED);
                            return;
                        }

                        meters.clear();
                        meters.addAll(response.body());

                        renderMeters();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Meter>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (rootView == null || !isAdded()) {
                            return;
                        }

                        setLoading(false);
                        showMessage(MESSAGE_CONNECTION_FAILED);
                    }
                }
        );
    }

    private void renderMeters() {
        if (meterAdapter == null || rootView == null) {
            return;
        }

        meterAdapter.notifyDataSetChanged();

        boolean isEmpty = meters.isEmpty();

        emptyState.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        if (isEmpty) {
            emptyState.setText(
                    "Chưa có đồng hồ. Nhấn + để thêm đồng hồ cho phòng này."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Meter form
    // -------------------------------------------------------------------------

    private void showMeterForm(@Nullable Meter meterToEdit) {
        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_meter,
                null
        );

        Spinner typeInput = formView.findViewById(
                R.id.meter_form_type
        );
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                METER_LABELS
        );
        typeInput.setAdapter(typeAdapter);

        RadioButton availableButton = formView.findViewById(
                R.id.meter_form_available
        );

        RadioButton unavailableButton = formView.findViewById(
                R.id.meter_form_unavailable
        );

        populateMeterForm(
                typeInput,
                availableButton,
                unavailableButton,
                meterToEdit
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        meterToEdit == null
                                ? "Thêm đồng hồ"
                                : "Sửa đồng hồ"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveMeter(
                                        typeInput,
                                        availableButton,
                                        meterToEdit,
                                        dialog
                                )
                        )
        );

        dialog.show();
    }

    private void populateMeterForm(
            @NonNull Spinner typeInput,
            @NonNull RadioButton availableButton,
            @NonNull RadioButton unavailableButton,
            @Nullable Meter meter
    ) {
        if (meter != null && meter.meter_type != null) {
            for (int index = 0; index < typeInput.getCount(); index++) {
                String option = String.valueOf(typeInput.getItemAtPosition(index));
                boolean matches = ("ELECTRICITY".equalsIgnoreCase(meter.meter_type) && "Điện".equals(option))
                        || ("WATER".equalsIgnoreCase(meter.meter_type) && "Nước".equals(option));
                if (matches) {
                    typeInput.setSelection(index);
                    break;
                }
            }
        }

        String status = meter == null
                ? STATUS_AVAILABLE
                : meter.status;

        boolean isUnavailable = STATUS_UNAVAILABLE.equalsIgnoreCase(
                status
        );

        unavailableButton.setChecked(isUnavailable);
        availableButton.setChecked(!isUnavailable);
    }

    // -------------------------------------------------------------------------
    // Save meter
    // -------------------------------------------------------------------------

    private void saveMeter(
            @NonNull Spinner typeInput,
            @NonNull RadioButton availableButton,
            @Nullable Meter meterToEdit,
            @NonNull AlertDialog dialog
    ) {
        Object selectedType = typeInput.getSelectedItem();
        String meterLabel = selectedType == null ? "" : selectedType.toString();
        String meterType = meterLabel.equals("Nước") ? METER_TYPES[1] : METER_TYPES[0];

        if (meterLabel.isEmpty()) {
            showMessage("Vui lòng chọn loại đồng hồ");
            return;
        }

        UUID roomUuid;

        try {
            roomUuid = UUID.fromString(roomId);
        } catch (Exception exception) {
            showMessage("Mã phòng không hợp lệ");
            return;
        }

        if (meterToEdit != null && meterToEdit.meter_id == null) {
            showMessage("Không xác định được đồng hồ");
            return;
        }

        String status = availableButton.isChecked()
                ? STATUS_AVAILABLE
                : STATUS_UNAVAILABLE;

        MeterRequest request = new MeterRequest(
                roomUuid,
                meterType,
                status
        );

        submitMeter(request, meterToEdit, dialog);
    }

    private void submitMeter(
            @NonNull MeterRequest request,
            @Nullable Meter meterToEdit,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText("Đang lưu...");

        Call<Meter> call;

        if (meterToEdit == null) {
            call = ApiClient.api.createMeter(request);
        } else {
            call = ApiClient.api.updateMeter(
                    meterToEdit.meter_id.toString(),
                    request
            );
        }

        call.enqueue(new Callback<Meter>() {

            @Override
            public void onResponse(
                    @NonNull Call<Meter> call,
                    @NonNull Response<Meter> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setText("Lưu");

                    showMessage("Không thể lưu đồng hồ");
                    return;
                }

                dialog.dismiss();

                showMessage("Đã lưu đồng hồ");
                loadMeters();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Meter> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Meter actions
    // -------------------------------------------------------------------------

    private void showMeterActions(@NonNull Meter meter) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getValue(meter.meter_type))
                .setItems(
                        new String[]{
                                "Xem lịch sử chỉ số",
                                "Sửa đồng hồ",
                                "Xóa đồng hồ"
                        },
                        (dialog, selectedAction) -> {
                            switch (selectedAction) {
                                case 0:
                                    showReadings(meter);
                                    break;

                                case 1:
                                    showMeterForm(meter);
                                    break;

                                case 2:
                                    confirmMeterDelete(meter);
                                    break;

                                default:
                                    break;
                            }
                        }
                )
                .show();
    }

    // -------------------------------------------------------------------------
    // Delete meter
    // -------------------------------------------------------------------------

    private void confirmMeterDelete(@NonNull Meter meter) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa đồng hồ?")
                .setMessage(
                        "Các chỉ số của đồng hồ cũng sẽ bị xóa."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteMeter(meter)
                )
                .show();
    }

    private void deleteMeter(@NonNull Meter meter) {
        if (meter.meter_id == null) {
            showMessage("Không xác định được đồng hồ");
            return;
        }

        ApiClient.api.deleteMeter(
                meter.meter_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    showMessage("Không thể xóa đồng hồ");
                    return;
                }

                showMessage("Đã xóa đồng hồ");
                loadMeters();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Meter reading history
    // -------------------------------------------------------------------------

    /**
     * Loads the reading history for a selected meter.
     */
    private void showReadings(@NonNull Meter meter) {
        if (meter.meter_id == null) {
            showMessage("Không xác định được đồng hồ");
            return;
        }

        ApiClient.api.meterReadings(
                meter.meter_id.toString()
        ).enqueue(new Callback<List<MeterReading>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<MeterReading>> call,
                    @NonNull Response<List<MeterReading>> response
            ) {
                if (!isAdded() || rootView == null) {
                    return;
                }

                if (!response.isSuccessful()
                        || response.body() == null) {
                    showMessage("Không thể tải chỉ số");
                    return;
                }

                displayReadings(meter, response.body());
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<MeterReading>> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể tải chỉ số");
            }
        });
    }

    private void displayReadings(
            @NonNull Meter meter,
            @NonNull List<MeterReading> readings
    ) {
        LinearLayout container = new LinearLayout(requireContext());

        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 4, 24, 8);

        ListView readingList = new ListView(requireContext());

        Drawable divider = requireContext()
                .getDrawable(R.color.background);

        readingList.setDivider(divider);
        readingList.setDividerHeight(8);

        MeterReadingAdapter readingAdapter = new MeterReadingAdapter(
                requireContext(),
                readings,
                reading -> showReadingActions(meter, reading)
        );

        readingList.setAdapter(readingAdapter);

        LinearLayout.LayoutParams listParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                );

        container.addView(readingList, listParams);

        Button addButton = new Button(requireContext());

        addButton.setText("Thêm");

        addButton.setOnClickListener(
                 view -> loadLatestReadingForForm(meter)
        );

        container.addView(
                addButton,
                createLayoutParams(0, 8, 0, 0)
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Lịch sử chỉ số: " + meter.meter_type)
                .setView(container)
                .setNegativeButton("Đóng", null)
                .create();

        readingsDialog = dialog;

        dialog.setOnDismissListener(
                ignored -> {
                    if (readingsDialog == dialog) {
                        readingsDialog = null;
                    }
                }
        );

        dialog.show();
    }

    private void loadLatestReadingForForm(@NonNull Meter meter) {
        if (meter.meter_id == null) {
            showMessage("Không xác định được đồng hồ");
            return;
        }

        ApiClient.api.meterReadings(meter.meter_id.toString()).enqueue(
                new Callback<List<MeterReading>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<List<MeterReading>> call,
                            @NonNull Response<List<MeterReading>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage("Không thể tải chỉ số trước đó");
                            return;
                        }
                        showReadingForm(meter, null, response.body().isEmpty()
                                ? BigDecimal.ZERO
                                : response.body().get(0).current_value);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<MeterReading>> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể tải chỉ số trước đó");
                    }
                }
        );
    }

    /**
     * Refreshes the current reading history without stacking dialogs.
     */
    private void refreshReadings(@NonNull Meter meter) {
        if (readingsDialog != null && readingsDialog.isShowing()) {
            readingsDialog.dismiss();
        }

        readingsDialog = null;

        showReadings(meter);
    }

    // -------------------------------------------------------------------------
    // Reading actions
    // -------------------------------------------------------------------------

    private void showReadingActions(
            @NonNull Meter meter,
            @NonNull MeterReading reading
    ) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉ số đọc")
                .setItems(
                        new String[]{
                                "Sửa chỉ số",
                                "Xóa chỉ số"
                        },
                        (dialog, selectedAction) -> {
                            if (selectedAction == 0) {
                                showReadingForm(meter, reading);
                            } else if (selectedAction == 1) {
                                confirmReadingDelete(meter, reading);
                            }
                        }
                )
                .show();
    }

    // -------------------------------------------------------------------------
    // Delete reading
    // -------------------------------------------------------------------------

    private void confirmReadingDelete(
            @NonNull Meter meter,
            @NonNull MeterReading reading
    ) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa chỉ số?")
                .setMessage(
                        "Chỉ số đã dùng để lập hóa đơn sẽ không thể xóa."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteReading(meter, reading)
                )
                .show();
    }

    private void deleteReading(
            @NonNull Meter meter,
            @NonNull MeterReading reading
    ) {
        if (reading.reading_id == null) {
            showMessage("Không xác định được chỉ số");
            return;
        }

        ApiClient.api.deleteMeterReading(
                reading.reading_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    showMessage(
                            response.code() == 409
                                    ? "Chỉ số đã được dùng để tính hóa đơn"
                                    : "Không thể xóa chỉ số"
                    );
                    return;
                }

                showMessage("Đã xóa chỉ số");

                refreshReadings(meter);
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Reading form
    // -------------------------------------------------------------------------

    private void showReadingForm(
            @NonNull Meter meter,
            @Nullable MeterReading readingToEdit
    ) {
        showReadingForm(meter, readingToEdit, null);
    }

    private void showReadingForm(
            @NonNull Meter meter,
            @Nullable MeterReading readingToEdit,
            @Nullable BigDecimal previousValue
    ) {
        evidenceUri = null;
        evidencePreview = null;

        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_meter_reading,
                null
        );

        ReadingForm form = new ReadingForm(formView);

        populateReadingForm(form, readingToEdit, previousValue);
        setupEvidencePicker(form);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        readingToEdit == null
                                ? "Thêm chỉ số"
                                : "Sửa chỉ số"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveReading(
                                        meter,
                                        readingToEdit,
                                        form,
                                        dialog
                                )
                        )
        );

        dialog.setOnDismissListener(
                ignored -> {
                    if (evidencePreview == form.evidencePreview) {
                        evidencePreview = null;
                    }

                    evidenceUri = null;
                }
        );

        dialog.show();
    }

    /**
     * Groups the input fields used by the reading form.
     */
    private static final class ReadingForm {

        final EditText readingAtInput;
        final EditText previousValueInput;
        final EditText currentValueInput;
        final EditText quantityInput;
        final EditText noteInput;

        final Button evidenceButton;
        final ImageView evidencePreview;

        ReadingForm(View root) {
            readingAtInput = root.findViewById(
                    R.id.reading_form_at
            );

            previousValueInput = root.findViewById(
                    R.id.reading_form_previous
            );

            currentValueInput = root.findViewById(
                    R.id.reading_form_current
            );

            quantityInput = root.findViewById(
                    R.id.reading_form_quantity
            );

            noteInput = root.findViewById(
                    R.id.reading_form_note
            );

            evidenceButton = root.findViewById(
                    R.id.reading_form_evidence
            );

            evidencePreview = root.findViewById(
                    R.id.reading_form_preview
            );
        }
    }

    private void populateReadingForm(
            @NonNull ReadingForm form,
            @Nullable MeterReading reading,
            @Nullable BigDecimal automaticPreviousValue
    ) {
        form.readingAtInput.setText(
                reading == null || reading.reading_at == null
                        ? LocalDateTime.now().withNano(0).toString()
                        : reading.reading_at.toString()
        );

        form.previousValueInput.setText(
                reading == null
                        ? getValue(automaticPreviousValue == null
                                ? BigDecimal.ZERO : automaticPreviousValue)
                        : getValue(reading.previous_value)
        );

        form.currentValueInput.setText(
                reading == null
                        ? "0"
                        : getValue(reading.current_value)
        );

        form.quantityInput.setText(reading == null ? "0" : getValue(reading.quantity));
        form.currentValueInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    BigDecimal previous = new BigDecimal(form.previousValueInput.getText().toString().trim());
                    BigDecimal current = new BigDecimal(s.toString().trim());
                    if (previous.stripTrailingZeros().scale() > 0 || current.stripTrailingZeros().scale() > 0) {
                        form.quantityInput.setText("");
                        return;
                    }
                    form.quantityInput.setText(current.compareTo(previous) >= 0
                            ? current.subtract(previous).toPlainString() : "");
                } catch (Exception ignored) {
                    form.quantityInput.setText("");
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });

        form.noteInput.setText(
                reading == null
                        ? ""
                        : getValue(reading.note)
        );

        form.evidencePreview.setVisibility(View.GONE);

        evidencePreview = form.evidencePreview;
    }

    private void setupEvidencePicker(@NonNull ReadingForm form) {
        form.evidenceButton.setOnClickListener(
                view -> chooseEvidence()
        );
    }

    // -------------------------------------------------------------------------
    // Save reading
    // -------------------------------------------------------------------------

    private void saveReading(
            @NonNull Meter meter,
            @Nullable MeterReading readingToEdit,
            @NonNull ReadingForm form,
            @NonNull AlertDialog dialog
    ) {
        try {
            if (meter.meter_id == null) {
                throw new IllegalArgumentException(
                        "Không xác định được đồng hồ"
                );
            }

            LocalDateTime readingAt = LocalDateTime.parse(
                    form.readingAtInput.getText()
                            .toString()
                            .trim()
            );

            BigDecimal currentValue = parseRequiredInteger(
                    form.currentValueInput.getText().toString()
            );

            BigDecimal previousValue = parseRequiredInteger(form.previousValueInput.getText().toString());
            BigDecimal quantity = currentValue.subtract(previousValue);

            if (!validReadingValues(
                    currentValue,
                    previousValue,
                    quantity
            )) {
                throw new IllegalArgumentException(
                        "Chỉ số hiện tại phải lớn hơn hoặc bằng chỉ số trước"
                );
            }

            String existingEvidenceUrl = readingToEdit == null
                    ? null
                    : readingToEdit.evidence_url;

            MeterReadingRequest request = new MeterReadingRequest(
                    meter.meter_id,
                    readingAt,
                    currentValue,
                    previousValue,
                    quantity,
                    existingEvidenceUrl,
                    getOptionalText(form.noteInput)
            );

            RequestBody body = RequestBody.create(
                    ApiClient.gson.toJson(request),
                    MediaType.parse(MIME_JSON)
            );

            MultipartBody.Part evidencePart = createEvidencePart();

            submitReading(
                    meter,
                    readingToEdit,
                    body,
                    evidencePart,
                    dialog
            );

        } catch (Exception exception) {
            String message = exception.getMessage();

            showMessage(
                    message == null || message.trim().isEmpty()
                            ? "Vui lòng kiểm tra thông tin chỉ số"
                            : message
            );
        }
    }

    private void submitReading(
            @NonNull Meter meter,
            @Nullable MeterReading readingToEdit,
            @NonNull RequestBody body,
            @Nullable MultipartBody.Part evidence,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText("Đang lưu...");

        Call<MeterReading> call;

        if (readingToEdit == null) {
            call = ApiClient.api.createMeterReading(
                    body,
                    evidence
            );
        } else {
            if (readingToEdit.reading_id == null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage("Không xác định được chỉ số");
                return;
            }

            call = ApiClient.api.updateMeterReading(
                    readingToEdit.reading_id.toString(),
                    body,
                    evidence
            );
        }

        call.enqueue(new Callback<MeterReading>() {

            @Override
            public void onResponse(
                    @NonNull Call<MeterReading> call,
                    @NonNull Response<MeterReading> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setText("Lưu");

                    showMessage("Không thể lưu chỉ số");
                    return;
                }

                dialog.dismiss();

                showMessage("Đã lưu chỉ số");

                refreshReadings(meter);
            }

            @Override
            public void onFailure(
                    @NonNull Call<MeterReading> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage(MESSAGE_CONNECTION_FAILED);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Evidence picker
    // -------------------------------------------------------------------------

    private void chooseEvidence() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(
                intent,
                EVIDENCE_PICKER_REQUEST
        );
    }

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != EVIDENCE_PICKER_REQUEST
                || resultCode != Activity.RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        evidenceUri = data.getData();

        int grantedFlags = data.getFlags()
                & Intent.FLAG_GRANT_READ_URI_PERMISSION;

        if (grantedFlags != 0) {
            try {
                requireContext()
                        .getContentResolver()
                        .takePersistableUriPermission(
                                evidenceUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (SecurityException ignored) {
                // Some document providers do not support persistent URI permissions.
            }
        }

        if (evidencePreview != null) {
            evidencePreview.setImageURI(evidenceUri);
            evidencePreview.setVisibility(View.VISIBLE);
        }
    }

    // -------------------------------------------------------------------------
    // Evidence upload
    // -------------------------------------------------------------------------

    @Nullable
    private MultipartBody.Part createEvidencePart() throws Exception {
        if (evidenceUri == null) {
            return null;
        }

        String mimeType = requireContext()
                .getContentResolver()
                .getType(evidenceUri);

        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Tệp minh chứng phải là hình ảnh"
            );
        }

        byte[] imageBytes = readEvidenceBytes(evidenceUri);

        if (imageBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Tệp minh chứng không có dữ liệu"
            );
        }

        RequestBody requestBody = RequestBody.create(
                imageBytes,
                MediaType.parse(mimeType)
        );

        return MultipartBody.Part.createFormData(
                "evidence",
                "meter-evidence",
                requestBody
        );
    }

    private byte[] readEvidenceBytes(@NonNull Uri uri) throws Exception {
        try (
                InputStream input = requireContext()
                        .getContentResolver()
                        .openInputStream(uri);

                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            if (input == null) {
                throw new IllegalArgumentException(
                        "Không thể đọc tệp minh chứng"
                );
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        }
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    private LinearLayout.LayoutParams createLayoutParams(
            int left,
            int top,
            int right,
            int bottom
    ) {
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        layoutParams.setMargins(
                left,
                top,
                right,
                bottom
        );

        return layoutParams;
    }

    // -------------------------------------------------------------------------
    // Reading validation and formatting
    // -------------------------------------------------------------------------

    static boolean validReadingValues(
            BigDecimal current,
            BigDecimal previous,
            BigDecimal quantity
    ) {
        return current != null
                && previous != null
                && quantity != null
                && current.signum() >= 0
                && previous.signum() >= 0
                && quantity.signum() >= 0
                && current.compareTo(previous) >= 0
                && current.subtract(previous).compareTo(quantity) == 0;
    }

    static String readingLabel(MeterReading reading) {
        return reading.reading_at
                + " | Cũ: " + reading.previous_value
                + " | Mới: " + reading.current_value
                + " | Dùng: " + reading.quantity;
    }

    private String getMeterStatusLabel(@Nullable String status) {
        return STATUS_AVAILABLE.equalsIgnoreCase(status)
                ? "Đang hoạt động"
                : "Không hoạt động";
    }

    private BigDecimal parseRequiredDecimal(@NonNull String text) {
        String value = text.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ các chỉ số"
            );
        }

        return new BigDecimal(value);
    }

    private BigDecimal parseRequiredInteger(@NonNull String text) {
        BigDecimal value = parseRequiredDecimal(text);
        if (value.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Chỉ số phải là số nguyên");
        }
        return value;
    }

    private String getValue(@Nullable Object value) {
        return value == null ? UNKNOWN_VALUE : value.toString();
    }

    @Nullable
    private String getOptionalText(@NonNull EditText input) {
        String value = input.getText()
                .toString()
                .trim();

        return value.isEmpty() ? null : value;
    }

    // -------------------------------------------------------------------------
    // Loading and messages
    // -------------------------------------------------------------------------

    private void setLoading(boolean isLoading) {
        if (loadingIndicator == null) {
            return;
        }

        loadingIndicator.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    private void showMessage(@NonNull String message) {
        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // -------------------------------------------------------------------------
    // Fragment cleanup
    // -------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        if (readingsDialog != null && readingsDialog.isShowing()) {
            readingsDialog.dismiss();
        }

        readingsDialog = null;
        evidencePreview = null;
        evidenceUri = null;

        rootView = null;
        loadingIndicator = null;
        emptyState = null;
        meterAdapter = null;

        super.onDestroyView();
    }
}
