package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationDetailFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_LOCATION_ID = "location_id";

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String UNKNOWN_LOCATION = "Chưa xác định";

    private static final String LOADING_PROVINCES =
            "Đang tải tỉnh/thành phố...";

    private static final String LOADING_DISTRICTS =
            "Đang tải quận/huyện...";

    private static final String LOADING_WARDS =
            "Đang tải phường/xã...";

    private static final String SELECT_DISTRICT_FIRST =
            "Chọn quận/huyện trước";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private Location location;

    private boolean restoring;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private Spinner provinceSpinner;
    private Spinner districtSpinner;
    private Spinner wardSpinner;

    private EditText locationCodeInput;
    private EditText addressInput;
    private EditText descriptionInput;

    private RadioGroup statusGroup;

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
                R.layout.fragment_location_detail,
                container,
                false
        );

        bindViews();
        setupClickListeners();
        setupAddressSpinners();

        String locationId = requireArguments().getString(
                ARG_LOCATION_ID
        );

        loadLocation(locationId);

        return rootView;
    }

    private void bindViews() {
        provinceSpinner = rootView.findViewById(
                R.id.location_detail_province
        );

        districtSpinner = rootView.findViewById(
                R.id.location_detail_district
        );

        wardSpinner = rootView.findViewById(
                R.id.location_detail_ward
        );

        locationCodeInput = rootView.findViewById(
                R.id.location_detail_code
        );

        addressInput = rootView.findViewById(
                R.id.location_detail_address
        );

        descriptionInput = rootView.findViewById(
                R.id.location_detail_description
        );

        statusGroup = rootView.findViewById(
                R.id.location_detail_status
        );
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.location_detail_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );

        rootView.findViewById(R.id.location_detail_save)
                .setOnClickListener(
                        view -> saveLocation()
                );

        rootView.findViewById(R.id.location_detail_delete)
                .setOnClickListener(
                        view -> {
                            if (location != null) {
                                confirmDelete();
                            }
                        }
                );
    }

    private void setupAddressSpinners() {
        provinceSpinner.setOnItemSelectedListener(
                new SelectListener(
                        () -> {
                            if (!restoring) {
                                loadDistricts(null);
                            }
                        }
                )
        );

        districtSpinner.setOnItemSelectedListener(
                new SelectListener(
                        () -> {
                            if (!restoring) {
                                loadWards(null);
                            }
                        }
                )
        );
    }

    // -------------------------------------------------------------------------
    // Load location
    // -------------------------------------------------------------------------

    private void loadLocation(@Nullable String locationId) {
        if (locationId == null || locationId.trim().isEmpty()) {
            showMessage("Không tìm thấy mã địa điểm");
            return;
        }

        ApiClient.api.location(locationId).enqueue(
                new Callback<Location>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Location> call,
                            @NonNull Response<Location> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải địa điểm");
                            return;
                        }

                        location = response.body();

                        populateLocationDetails();
                        loadProvinces();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Location> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    private void populateLocationDetails() {
        if (location == null) {
            return;
        }

        locationCodeInput.setText(
                getValue(location.location_code)
        );

        addressInput.setText(
                getValue(location.address_line)
        );

        descriptionInput.setText(
                getValue(location.description)
        );

        int statusViewId = STATUS_UNAVAILABLE.equalsIgnoreCase(
                location.status
        )
                ? R.id.location_status_unavailable
                : R.id.location_status_available;

        RadioButton statusButton = rootView.findViewById(
                statusViewId
        );

        statusButton.setChecked(true);
    }

    // -------------------------------------------------------------------------
    // Load provinces
    // -------------------------------------------------------------------------

    private void loadProvinces() {
        setLoading(provinceSpinner, LOADING_PROVINCES);

        ProvinceApiClient.api.provinces().enqueue(
                new Callback<List<Province>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Province>> call,
                            @NonNull Response<List<Province>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải tỉnh/thành phố"
                            );
                            return;
                        }

                        if (location == null) {
                            return;
                        }

                        updateSpinnerItems(
                                provinceSpinner,
                                response.body()
                        );

                        Province selectedProvince = selectByName(
                                provinceSpinner,
                                location.province_name
                        );

                        if (selectedProvince == null) {
                            showMessage(
                                    "Không tìm thấy tỉnh/thành phố đã lưu"
                            );
                            return;
                        }

                        loadDistricts(location.district_name);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Province>> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage(
                                "Không thể kết nối API địa chỉ"
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Load districts
    // -------------------------------------------------------------------------

    private void loadDistricts(@Nullable String selectedDistrictName) {
        Province selectedProvince = getSelectedProvince();

        if (selectedProvince == null) {
            return;
        }

        setLoading(districtSpinner, LOADING_DISTRICTS);
        setLoading(wardSpinner, SELECT_DISTRICT_FIRST);

        ProvinceApiClient.api.legacyProvince(
                selectedProvince.code,
                2
        ).enqueue(new Callback<Province>() {

            @Override
            public void onResponse(
                    @NonNull Call<Province> call,
                    @NonNull Response<Province> response
            ) {
                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().districts == null) {
                    showMessage("Không thể tải quận/huyện");
                    return;
                }

                updateSpinnerItems(
                        districtSpinner,
                        response.body().districts
                );

                Province selectedDistrict = selectByName(
                        districtSpinner,
                        selectedDistrictName
                );

                if (selectedDistrictName != null
                        && selectedDistrict == null) {
                    showMessage(
                            "Không tìm thấy quận/huyện đã lưu"
                    );
                    return;
                }

                if (selectedDistrict != null) {
                    String selectedWardName =
                            selectedDistrictName != null && location != null
                                    ? location.ward_name
                                    : null;

                    loadWards(selectedWardName);
                } else {
                    setLoading(
                            wardSpinner,
                            SELECT_DISTRICT_FIRST
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<Province> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể tải quận/huyện");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Load wards
    // -------------------------------------------------------------------------

    private void loadWards(@Nullable String selectedWardName) {
        Province selectedDistrict = getSelectedDistrict();

        if (selectedDistrict == null) {
            setLoading(wardSpinner, SELECT_DISTRICT_FIRST);
            return;
        }

        setLoading(wardSpinner, LOADING_WARDS);

        ProvinceApiClient.api.legacyDistrict(
                selectedDistrict.code,
                2
        ).enqueue(new Callback<Province>() {

            @Override
            public void onResponse(
                    @NonNull Call<Province> call,
                    @NonNull Response<Province> response
            ) {
                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().wards == null) {
                    showMessage("Không thể tải phường/xã");
                    return;
                }

                updateSpinnerItems(
                        wardSpinner,
                        response.body().wards
                );

                Province selectedWard = selectByName(
                        wardSpinner,
                        selectedWardName
                );

                if (selectedWardName != null && selectedWard == null) {
                    showMessage(
                            "Không tìm thấy phường/xã đã lưu"
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<Province> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể tải phường/xã");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Save location
    // -------------------------------------------------------------------------

    private void saveLocation() {
        if (location == null || location.location_id == null) {
            showMessage("Địa điểm chưa tải xong");
            return;
        }

        String code = locationCodeInput.getText()
                .toString()
                .trim();

        String address = addressInput.getText()
                .toString()
                .trim();

        String description = getOptionalText(descriptionInput);

        Province selectedProvince = getSelectedProvince();
        Province selectedDistrict = getSelectedDistrict();
        Province selectedWard = getSelectedWard();

        int selectedStatusId = statusGroup.getCheckedRadioButtonId();

        if (code.isEmpty()
                || address.isEmpty()
                || selectedProvince == null
                || selectedStatusId == -1) {
            showMessage("Vui lòng nhập đủ thông tin địa điểm");
            return;
        }

        String selectedStatus =
                selectedStatusId == R.id.location_status_unavailable
                        ? STATUS_UNAVAILABLE
                        : STATUS_AVAILABLE;

        LocationRequest request = new LocationRequest(
                code,
                address,
                selectedDistrict == null
                        ? null
                        : selectedDistrict.name,
                selectedWard == null
                        ? null
                        : selectedWard.name,
                selectedProvince.name,
                selectedStatus,
                description
        );

        submitLocationUpdate(request);
    }

    private void submitLocationUpdate(
            @NonNull LocationRequest request
    ) {
        ApiClient.api.updateLocation(
                location.location_id.toString(),
                request
        ).enqueue(new Callback<Location>() {

            @Override
            public void onResponse(
                    @NonNull Call<Location> call,
                    @NonNull Response<Location> response
            ) {
                if (!response.isSuccessful()) {
                    showMessage("Không thể cập nhật địa điểm");
                    return;
                }

                if (response.body() != null) {
                    location = response.body();
                }

                showMessage("Đã cập nhật địa điểm");
            }

            @Override
            public void onFailure(
                    @NonNull Call<Location> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Delete location
    // -------------------------------------------------------------------------

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa địa điểm?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteLocation()
                )
                .show();
    }

    private void deleteLocation() {
        if (location == null || location.location_id == null) {
            showMessage("Không xác định được địa điểm");
            return;
        }

        ApiClient.api.deleteLocation(
                location.location_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    NavHostFragment.findNavController(
                            LocationDetailFragment.this
                    ).navigateUp();

                    return;
                }

                showMessage(
                        response.code() == 409
                                ? "Không thể xóa vì địa điểm đang có phòng"
                                : "Không thể xóa địa điểm"
                );
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Spinner selection
    // -------------------------------------------------------------------------

    @Nullable
    private Province getSelectedProvince() {
        return getSelectedItem(provinceSpinner);
    }

    @Nullable
    private Province getSelectedDistrict() {
        return getSelectedItem(districtSpinner);
    }

    @Nullable
    private Province getSelectedWard() {
        return getSelectedItem(wardSpinner);
    }

    @Nullable
    private Province getSelectedItem(@NonNull Spinner spinner) {
        Object item = spinner.getSelectedItem();

        return item instanceof Province
                ? (Province) item
                : null;
    }

    /**
     * Selects an address item using an exact name match first,
     * followed by a normalized name match for legacy address data.
     */
    @Nullable
    private Province selectByName(
            @NonNull Spinner spinner,
            @Nullable String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String expectedName = normalizeAddressName(name);

        if (expectedName.isEmpty()) {
            return null;
        }

        Province fallback = null;
        int fallbackIndex = -1;

        for (int index = 0; index < spinner.getCount(); index++) {
            Object item = spinner.getItemAtPosition(index);

            if (!(item instanceof Province)) {
                continue;
            }

            Province candidate = (Province) item;

            String candidateName = candidate.name == null
                    ? ""
                    : candidate.name.trim();

            if (name.trim().equalsIgnoreCase(candidateName)) {
                spinner.setSelection(index);
                return candidate;
            }

            String normalizedCandidate =
                    normalizeAddressName(candidateName);

            if (normalizedCandidate.isEmpty()) {
                continue;
            }

            if (expectedName.equals(normalizedCandidate)
                    || normalizedCandidate.contains(expectedName)
                    || expectedName.contains(normalizedCandidate)) {
                fallback = candidate;
                fallbackIndex = index;
            }
        }

        if (fallback != null && fallbackIndex >= 0) {
            spinner.setSelection(fallbackIndex);
            return fallback;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Address normalization
    // -------------------------------------------------------------------------

    private String normalizeAddressName(@Nullable String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        normalized = normalized
                .replace("thành phố", "")
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

    // -------------------------------------------------------------------------
    // Spinner adapters and loading states
    // -------------------------------------------------------------------------

    private void updateSpinnerItems(
            @NonNull Spinner spinner,
            @NonNull List<Province> items
    ) {
        restoring = true;

        try {
            ArrayAdapter<Province> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    items
            );

            spinner.setAdapter(adapter);

        } finally {
            restoring = false;
        }
    }

    private void setLoading(
            @NonNull Spinner spinner,
            @NonNull String message
    ) {
        restoring = true;

        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{message}
            );

            spinner.setAdapter(adapter);

        } finally {
            restoring = false;
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    @NonNull
    private String getValue(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    @Nullable
    private String getOptionalText(@NonNull EditText input) {
        String value = input.getText()
                .toString()
                .trim();

        return value.isEmpty() ? null : value;
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
    // Spinner selection listener
    // -------------------------------------------------------------------------

    private static class SelectListener
            implements AdapterView.OnItemSelectedListener {

        private final Runnable action;

        SelectListener(@NonNull Runnable action) {
            this.action = action;
        }

        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id
        ) {
            action.run();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // No action required when no item is selected.
        }
    }

    // -------------------------------------------------------------------------
    // Fragment cleanup
    // -------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        rootView = null;

        provinceSpinner = null;
        districtSpinner = null;
        wardSpinner = null;

        locationCodeInput = null;
        addressInput = null;
        descriptionInput = null;

        statusGroup = null;
    }
}