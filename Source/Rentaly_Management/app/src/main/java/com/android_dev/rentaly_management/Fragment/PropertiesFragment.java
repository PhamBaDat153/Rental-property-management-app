package com.android_dev.rentaly_management.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.PropertyAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.Apis.ProvinceApiClient;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.Province;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.RoomRequest;
import com.android_dev.rentaly_management.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertiesFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int IMAGE_PICKER_REQUEST = 401;
    private static final int MAX_IMAGES = 6;

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String MIME_JSON = "application/json";

    private static final String ARG_SHOW_ROOMS = "show_rooms";
    private static final String ARG_CREATE_ROOM = "create_room";
    private static final String ARG_EDIT_LOCATION_ID = "edit_location_id";
    private static final String ARG_EDIT_ROOM_ID = "edit_room_id";

    private static final String UNKNOWN_VALUE = "Chưa cập nhật";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Uri> selectedImages = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<Object> visibleItems = new ArrayList<>();

    private PropertyAdapter propertyAdapter;

    private boolean showingLocations = true;
    private boolean createRoomPending;

    private String pendingEditLocationId;
    private String pendingEditRoomId;

    // -------------------------------------------------------------------------
    // Views and dialogs
    // -------------------------------------------------------------------------

    private View rootView;

    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView emptyState;

    private AlertDialog openRoomDialog;

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
                R.layout.fragment_properties,
                container,
                false
        );

        bindViews();
        readNavigationArguments();
        setupPropertyList();
        setupCategoryButtons();
        setupSearch();
        setupAddButton();

        if (getArguments() != null
                && getArguments().getBoolean(ARG_SHOW_ROOMS, false)) {
            selectRooms();
        } else {
            selectLocations();
        }

        loadLocations();
        loadRooms();
        loadPendingEditForms();

        return rootView;
    }

    private void bindViews() {
        searchInput = rootView.findViewById(
                R.id.resource_search
        );

        loadingIndicator = rootView.findViewById(
                R.id.resource_loading
        );

        emptyState = rootView.findViewById(
                R.id.resource_empty
        );
    }

    private void readNavigationArguments() {
        Bundle arguments = getArguments();

        if (arguments == null) {
            return;
        }

        pendingEditLocationId = arguments.getString(
                ARG_EDIT_LOCATION_ID
        );

        pendingEditRoomId = arguments.getString(
                ARG_EDIT_ROOM_ID
        );

        createRoomPending = arguments.getBoolean(
                ARG_CREATE_ROOM,
                false
        );
    }

    // -------------------------------------------------------------------------
    // Property list setup
    // -------------------------------------------------------------------------

    private void setupPropertyList() {
        ListView propertyList = rootView.findViewById(
                R.id.resource_list
        );

        propertyAdapter = new PropertyAdapter(
                requireContext(),
                visibleItems,
                new PropertyAdapter.Formatter() {

                    @Override
                    public String location(Object id) {
                        return getLocationLabel((UUID) id);
                    }

                    @Override
                    public String status(String status) {
                        return getStatusLabel(status);
                    }

                    @Override
                    public String roomName(Room room) {
                        return PropertiesFragment.roomName(room);
                    }

                    @Override
                    public String address(Location location) {
                        return getAddress(location);
                    }
                }
        );

        propertyList.setAdapter(propertyAdapter);

        propertyList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    if (position >= 0
                            && position < visibleItems.size()) {
                        openDetails(visibleItems.get(position));
                    }
                }
        );
    }

    private void setupCategoryButtons() {
        rootView.findViewById(R.id.resource_locations)
                .setOnClickListener(
                        view -> selectLocations()
                );

        rootView.findViewById(R.id.resource_rooms)
                .setOnClickListener(
                        view -> selectRooms()
                );
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
                // No action required before the text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                renderItems();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required after the text changes.
            }
        });
    }

    private void setupAddButton() {
        rootView.findViewById(R.id.resource_add)
                .setOnClickListener(
                        view -> {
                            if (showingLocations) {
                                showLocationForm(null);
                            } else {
                                showRoomForm(null);
                            }
                        }
                );
    }

    // -------------------------------------------------------------------------
    // Category selection
    // -------------------------------------------------------------------------

    private void selectLocations() {
        showingLocations = true;

        searchInput.setHint("Tìm theo mã hoặc địa chỉ");

        TextView title = rootView.findViewById(
                R.id.resource_list_title
        );

        title.setText("DANH SÁCH ĐỊA ĐIỂM");

        rootView.findViewById(R.id.resource_locations)
                .setSelected(true);

        rootView.findViewById(R.id.resource_rooms)
                .setSelected(false);

        renderItems();
    }

    private void selectRooms() {
        showingLocations = false;

        searchInput.setHint("Tìm theo tên phòng");

        TextView title = rootView.findViewById(
                R.id.resource_list_title
        );

        title.setText("DANH SÁCH PHÒNG");

        rootView.findViewById(R.id.resource_locations)
                .setSelected(false);

        rootView.findViewById(R.id.resource_rooms)
                .setSelected(true);

        renderItems();
    }

    // -------------------------------------------------------------------------
    // Load locations
    // -------------------------------------------------------------------------

    private void loadLocations() {
        setLoading(true);

        ApiClient.api.locations().enqueue(
                new Callback<List<Location>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Location>> call,
                            @NonNull Response<List<Location>> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải danh sách địa điểm"
                            );
                            return;
                        }

                        locations.clear();
                        locations.addAll(response.body());

                        renderItems();
                        handlePendingLocationActions();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Location>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        setLoading(false);

                        showMessage(
                                "Không thể kết nối đến máy chủ"
                        );
                    }
                }
        );
    }

    private void handlePendingLocationActions() {
        if (pendingEditLocationId != null) {
            String locationId = pendingEditLocationId;
            pendingEditLocationId = null;

            loadLocationForEditing(locationId);
            return;
        }

        if (createRoomPending) {
            if (locations.isEmpty()) {
                showMessage(
                        "Cần có địa điểm trước khi thêm phòng"
                );

                createRoomPending = false;
                return;
            }

            createRoomPending = false;

            rootView.post(() -> showRoomForm(null));
        }
    }

    private void loadLocationForEditing(
            @NonNull String locationId
    ) {
        ApiClient.api.location(locationId).enqueue(
                new Callback<Location>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Location> call,
                            @NonNull Response<Location> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải địa điểm để sửa"
                            );
                            return;
                        }

                        showLocationForm(response.body());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Location> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage(
                                "Không thể tải địa điểm để sửa"
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Load rooms
    // -------------------------------------------------------------------------

    private void loadRooms() {
        ApiClient.api.rooms().enqueue(
                new Callback<List<Room>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Room>> call,
                            @NonNull Response<List<Room>> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            if (!showingLocations) {
                                showMessage(
                                        "Không thể tải danh sách phòng"
                                );
                            }
                            return;
                        }

                        rooms.clear();
                        rooms.addAll(response.body());

                        renderItems();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Room>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (isAdded() && !showingLocations) {
                            showMessage(
                                    "Không thể kết nối đến máy chủ"
                            );
                        }
                    }
                }
        );
    }

    private void loadRoomForEditing(@NonNull String roomId) {
        ApiClient.api.room(roomId).enqueue(
                new Callback<Room>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Room> call,
                            @NonNull Response<Room> response
                    ) {
                        if (!isAdded() || rootView == null) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải phòng để sửa");
                            return;
                        }

                        showRoomForm(response.body());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Room> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage("Không thể tải phòng để sửa");
                    }
                }
        );
    }

    private void loadPendingEditForms() {
        if (pendingEditRoomId == null) {
            return;
        }

        String roomId = pendingEditRoomId;
        pendingEditRoomId = null;

        loadRoomForEditing(roomId);
    }

    // -------------------------------------------------------------------------
    // Render filtered items
    // -------------------------------------------------------------------------

    private void renderItems() {
        if (propertyAdapter == null || searchInput == null) {
            return;
        }

        String query = normalizeText(
                searchInput.getText().toString()
        );

        visibleItems.clear();

        if (showingLocations) {
            addMatchingLocations(query);
        } else {
            addMatchingRooms(query);
        }

        propertyAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void addMatchingLocations(@NonNull String query) {
        for (Location location : locations) {
            if (locationMatches(location, query)) {
                visibleItems.add(location);
            }
        }
    }

    private void addMatchingRooms(@NonNull String query) {
        for (Room room : rooms) {
            if (roomMatches(room, query)) {
                visibleItems.add(room);
            }
        }
    }

    private void updateEmptyState() {
        if (emptyState == null) {
            return;
        }

        boolean isEmpty = visibleItems.isEmpty();

        emptyState.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        emptyState.setText(
                showingLocations
                        ? "Chưa có địa điểm phù hợp"
                        : "Chưa có phòng phù hợp"
        );
    }

    // -------------------------------------------------------------------------
    // Search matching
    // -------------------------------------------------------------------------

    static boolean locationMatches(Location location, String query) {
        if (location == null) {
            return false;
        }

        if (query == null || query.isEmpty()) {
            return true;
        }

        return text(location.location_code).contains(query)
                || text(location.address_line).contains(query)
                || text(location.ward_name).contains(query)
                || text(location.district_name).contains(query)
                || text(location.province_name).contains(query);
    }

    static boolean roomMatches(Room room, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        return room != null
                && (text(room.room_name).contains(query)
                || text(room.room_code).contains(query)
                || text(room.status).contains(query));
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void openDetails(@NonNull Object item) {
        Bundle arguments = new Bundle();

        if (item instanceof Location) {
            Location location = (Location) item;

            if (location.location_id == null) {
                showMessage("Không xác định được địa điểm");
                return;
            }

            arguments.putString(
                    "location_id",
                    location.location_id.toString()
            );

            NavHostFragment.findNavController(this).navigate(
                    R.id.locationDetailFragment,
                    arguments
            );

            return;
        }

        if (item instanceof Room) {
            Room room = (Room) item;

            if (room.room_id == null) {
                showMessage("Không xác định được phòng");
                return;
            }

            arguments.putString(
                    "room_id",
                    room.room_id.toString()
            );

            NavHostFragment.findNavController(this).navigate(
                    R.id.roomDetailFragment,
                    arguments
            );
        }
    }

    // -------------------------------------------------------------------------
    // Location form
    // -------------------------------------------------------------------------

    private void showLocationForm(@Nullable Location current) {
        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_location,
                null
        );

        LocationForm form = new LocationForm(formView);

        populateLocationForm(form, current);
        setupLocationSpinnerListeners(form);
        initializeLocationSpinners(form, current);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        current == null
                                ? "Thêm địa điểm"
                                : "Sửa địa điểm"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveLocation(form, current, dialog)
                        )
        );

        dialog.show();
    }

    /**
     * Holds the views used by the location form.
     */
    private static final class LocationForm {

        final EditText codeInput;
        final EditText addressInput;
        final EditText descriptionInput;

        final Spinner provinceSpinner;
        final Spinner districtSpinner;
        final Spinner wardSpinner;
        final Spinner statusSpinner;

        LocationForm(View root) {
            codeInput = root.findViewById(R.id.location_code);
            addressInput = root.findViewById(R.id.location_address);
            descriptionInput = root.findViewById(R.id.location_description);

            provinceSpinner = root.findViewById(R.id.location_province);
            districtSpinner = root.findViewById(R.id.location_district);
            wardSpinner = root.findViewById(R.id.location_ward);
            statusSpinner = root.findViewById(R.id.location_status);
        }
    }

    private void populateLocationForm(
            @NonNull LocationForm form,
            @Nullable Location current
    ) {
        form.codeInput.setText(
                current == null ? "" : getValue(current.location_code)
        );

        form.addressInput.setText(
                current == null ? "" : getValue(current.address_line)
        );

        form.descriptionInput.setText(
                current == null ? "" : getValue(current.description)
        );

        setSpinner(
                form.statusSpinner,
                new String[]{
                        STATUS_AVAILABLE,
                        STATUS_UNAVAILABLE
                },
                current == null ? STATUS_AVAILABLE : current.status
        );

        setLoadingSpinner(
                form.provinceSpinner,
                "Đang tải tỉnh/thành phố..."
        );

        setLoadingSpinner(
                form.districtSpinner,
                "Chọn tỉnh trước"
        );

        setLoadingSpinner(
                form.wardSpinner,
                "Chọn quận/huyện trước"
        );
    }

    private void setupLocationSpinnerListeners(
            @NonNull LocationForm form
    ) {
        form.provinceSpinner.setOnItemSelectedListener(
                new SimpleSelectListener(
                        () -> loadDistricts(
                                form.provinceSpinner,
                                form.districtSpinner,
                                form.wardSpinner
                        )
                )
        );

        form.districtSpinner.setOnItemSelectedListener(
                new SimpleSelectListener(
                        () -> loadWards(
                                form.districtSpinner,
                                form.wardSpinner
                        )
                )
        );
    }

    private void initializeLocationSpinners(
            @NonNull LocationForm form,
            @Nullable Location current
    ) {
        ProvinceApiClient.api.provinces().enqueue(
                new Callback<List<Province>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Province>> call,
                            @NonNull Response<List<Province>> response
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải danh sách tỉnh/thành phố"
                            );
                            return;
                        }

                        setProvinceItems(
                                form.provinceSpinner,
                                response.body()
                        );

                        if (current != null) {
                            selectName(
                                    form.provinceSpinner,
                                    current.province_name
                            );
                        }
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
    // Location form validation and save
    // -------------------------------------------------------------------------

    private void saveLocation(
            @NonNull LocationForm form,
            @Nullable Location current,
            @NonNull AlertDialog dialog
    ) {
        String code = form.codeInput.getText()
                .toString()
                .trim();

        String address = form.addressInput.getText()
                .toString()
                .trim();

        Province province = selectedProvince(form.provinceSpinner);
        Province district = selectedProvince(form.districtSpinner);
        Province ward = selectedProvince(form.wardSpinner);

        if (code.isEmpty() || address.isEmpty() || province == null) {
            showMessage(
                    "Vui lòng nhập mã, địa chỉ và tỉnh/thành phố"
            );
            return;
        }

        Object selectedStatus = form.statusSpinner.getSelectedItem();

        if (selectedStatus == null) {
            showMessage("Vui lòng chọn trạng thái địa điểm");
            return;
        }

        LocationRequest request = new LocationRequest(
                code,
                address,
                district == null ? null : district.name,
                ward == null ? null : ward.name,
                province.name,
                selectedStatus.toString(),
                getOptionalText(form.descriptionInput)
        );

        Call<Location> call;

        if (current == null) {
            call = ApiClient.api.createLocation(request);
        } else {
            if (current.location_id == null) {
                showMessage("Không xác định được địa điểm");
                return;
            }

            call = ApiClient.api.updateLocation(
                    current.location_id.toString(),
                    request
            );
        }

        submitLocation(
                call,
                current == null
                        ? "Đã thêm địa điểm"
                        : "Đã cập nhật địa điểm",
                dialog
        );
    }

    private void submitLocation(
            @NonNull Call<Location> call,
            @NonNull String successMessage,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        call.enqueue(new Callback<Location>() {

            @Override
            public void onResponse(
                    @NonNull Call<Location> call,
                    @NonNull Response<Location> response
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

                    showMessage(
                            getErrorMessage(
                                    response,
                                    "Không thể lưu địa điểm"
                            )
                    );

                    return;
                }

                dialog.dismiss();

                showMessage(successMessage);
                loadLocations();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Location> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Room form
    // -------------------------------------------------------------------------

    private void showRoomForm(@Nullable Room current) {
        if (locations.isEmpty()) {
            showMessage(
                    "Cần có địa điểm trước khi thêm phòng"
            );
            return;
        }

        selectedImages.clear();

        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_room,
                null
        );

        RoomForm form = new RoomForm(formView);

        populateRoomForm(form, current);
        setupRoomImagePicker(form);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        current == null
                                ? "Thêm phòng"
                                : "Sửa phòng"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        openRoomDialog = dialog;

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(
                                view -> saveRoom(form, current, dialog)
                        )
        );

        dialog.setOnDismissListener(
                ignored -> {
                    if (openRoomDialog == dialog) {
                        openRoomDialog = null;
                    }

                    selectedImages.clear();
                }
        );

        dialog.show();
    }

    /**
     * Holds the views and location IDs used by the room form.
     */
    private static final class RoomForm {

        // Root view of the room form.
        final View root;

        final Spinner locationSpinner;

        final EditText codeInput;
        final EditText nameInput;
        final EditText floorInput;
        final EditText areaInput;
        final EditText occupantsInput;
        final EditText rentInput;
        final EditText descriptionInput;

        final RadioGroup statusGroup;

        final TextView imageStatus;
        final LinearLayout imagePreview;

        final List<UUID> locationIds = new ArrayList<>();

        RoomForm(View root) {
            this.root = root;

            locationSpinner = root.findViewById(R.id.room_location);

            codeInput = root.findViewById(R.id.room_code);
            nameInput = root.findViewById(R.id.room_name);
            floorInput = root.findViewById(R.id.room_floor);
            areaInput = root.findViewById(R.id.room_area);
            occupantsInput = root.findViewById(R.id.room_occupants);
            rentInput = root.findViewById(R.id.room_rent);
            descriptionInput = root.findViewById(R.id.room_description);

            statusGroup = root.findViewById(R.id.room_status);

            imageStatus = root.findViewById(R.id.room_images_status);
            imagePreview = root.findViewById(R.id.room_image_preview);
        }
    }

    private void populateRoomForm(
            @NonNull RoomForm form,
            @Nullable Room current
    ) {
        form.codeInput.setText(
                current == null ? "" : getValue(current.room_code)
        );

        form.nameInput.setText(
                current == null ? "" : getValue(current.room_name)
        );

        form.floorInput.setText(
                current == null ? "" : getValue(current.floor)
        );

        form.areaInput.setText(
                current == null ? "" : getValue(current.area_m2)
        );

        form.occupantsInput.setText(
                current == null ? "1" : getValue(current.max_occupants)
        );

        form.rentInput.setText(
                current == null ? "0" : getValue(current.rent_price)
        );

        form.descriptionInput.setText(
                current == null ? "" : getValue(current.description)
        );

        String status = current == null
                ? STATUS_AVAILABLE
                : current.status;

        int statusId = STATUS_UNAVAILABLE.equalsIgnoreCase(status)
                ? R.id.room_status_unavailable
                : R.id.room_status_available;

        RadioButton statusButton = form.root.findViewById(statusId);
        statusButton.setChecked(true);

        setupRoomLocationSpinner(form, current);
        setupExistingRoomImages(form, current);
    }

    private void setupRoomLocationSpinner(
            @NonNull RoomForm form,
            @Nullable Room current
    ) {
        List<String> locationLabels = new ArrayList<>();

        for (Location location : locations) {
            form.locationIds.add(location.location_id);

            locationLabels.add(
                    getValue(location.location_code)
                            + " - "
                            + getAddress(location)
            );
        }

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                locationLabels
        );

        form.locationSpinner.setAdapter(locationAdapter);

        if (current != null) {
            int selectedIndex = form.locationIds.indexOf(
                    current.location_id
            );

            if (selectedIndex >= 0) {
                form.locationSpinner.setSelection(selectedIndex);
            }
        }
    }

    private void setupExistingRoomImages(
            @NonNull RoomForm form,
            @Nullable Room current
    ) {
        if (current != null
                && current.image_urls != null
                && !current.image_urls.isEmpty()) {
            form.imageStatus.setText(
                    "Đang giữ " + current.image_urls.size()
                            + " hình ảnh cũ; chọn ảnh mới để tải bổ sung"
            );
        } else {
            form.imageStatus.setText("Chưa chọn hình ảnh");
        }

        form.imagePreview.removeAllViews();
    }

    private void setupRoomImagePicker(@NonNull RoomForm form) {
        form.root.findViewById(R.id.room_choose_images)
                .setOnClickListener(
                        view -> chooseImages()
                );

        form.imageStatus.setOnClickListener(
                view -> {
                    selectedImages.clear();
                    renderSelectedImages(form);
                    form.imageStatus.setText("Đã bỏ chọn hình ảnh");
                }
        );
    }

    // -------------------------------------------------------------------------
    // Room validation and save
    // -------------------------------------------------------------------------

    private void saveRoom(
            @NonNull RoomForm form,
            @Nullable Room current,
            @NonNull AlertDialog dialog
    ) {
        try {
            String code = form.codeInput.getText()
                    .toString()
                    .trim();

            String occupantsText = form.occupantsInput.getText()
                    .toString()
                    .trim();

            String rentText = form.rentInput.getText()
                    .toString()
                    .trim();

            if (code.isEmpty()) {
                showMessage("Vui lòng nhập mã phòng");
                return;
            }

            if (occupantsText.isEmpty() || rentText.isEmpty()) {
                showMessage(
                        "Vui lòng nhập số người và giá thuê"
                );
                return;
            }

            int occupants = Integer.parseInt(occupantsText);
            BigDecimal rent = new BigDecimal(rentText);

            if (occupants < 1 || rent.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException();
            }

            BigDecimal area = parseOptionalDecimal(
                    form.areaInput.getText().toString()
            );

            if (area != null && area.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException();
            }

            int locationPosition =
                    form.locationSpinner.getSelectedItemPosition();

            if (locationPosition < 0
                    || locationPosition >= form.locationIds.size()) {
                showMessage("Vui lòng chọn địa điểm");
                return;
            }

            UUID selectedLocationId = form.locationIds.get(
                    locationPosition
            );

            if (selectedLocationId == null) {
                showMessage("Địa điểm được chọn không hợp lệ");
                return;
            }

            int selectedStatusId =
                    form.statusGroup.getCheckedRadioButtonId();

            if (selectedStatusId == -1) {
                showMessage("Vui lòng chọn trạng thái phòng");
                return;
            }

            String status = selectedStatusId
                    == R.id.room_status_unavailable
                    ? STATUS_UNAVAILABLE
                    : STATUS_AVAILABLE;

            RoomRequest request = new RoomRequest(
                    selectedLocationId,
                    code,
                    getOptionalText(form.nameInput),
                    parseOptionalInteger(
                            form.floorInput.getText().toString()
                    ),
                    area,
                    occupants,
                    rent,
                    status,
                    getOptionalText(form.descriptionInput)
            );

            RequestBody requestBody = RequestBody.create(
                    ApiClient.gson.toJson(request),
                    MediaType.parse(MIME_JSON)
            );

            List<MultipartBody.Part> imageParts = createImageParts();

            submitRoom(
                    requestBody,
                    imageParts,
                    current,
                    dialog
            );

        } catch (Exception exception) {
            showMessage(
                    "Vui lòng kiểm tra mã, số người, diện tích và giá thuê"
            );
        }
    }

    private void submitRoom(
            @NonNull RequestBody requestBody,
            @NonNull List<MultipartBody.Part> imageParts,
            @Nullable Room current,
            @NonNull AlertDialog dialog
    ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText("Đang lưu...");

        Call<Room> call;

        if (current == null) {
            call = ApiClient.api.createRoom(
                    requestBody,
                    imageParts
            );
        } else {
            if (current.room_id == null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage("Không xác định được phòng");
                return;
            }

            call = ApiClient.api.updateRoom(
                    current.room_id.toString(),
                    requestBody,
                    imageParts
            );
        }

        call.enqueue(new Callback<Room>() {

            @Override
            public void onResponse(
                    @NonNull Call<Room> call,
                    @NonNull Response<Room> response
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

                    showMessage(
                            getErrorMessage(
                                    response,
                                    "Không thể lưu phòng"
                            )
                    );

                    return;
                }

                dialog.dismiss();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setText("Lưu");
                showMessage(
                        current == null
                                ? "Đã thêm phòng"
                                : "Đã cập nhật phòng"
                );

                loadRooms();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Room> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded()) {
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                showMessage(
                        "Không thể kết nối đến máy chủ: "
                                + throwable.getMessage()
                );
            }
        });
    }

    // -------------------------------------------------------------------------
    // Image picker
    // -------------------------------------------------------------------------

    private void chooseImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(
                intent,
                IMAGE_PICKER_REQUEST
        );
    }

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != IMAGE_PICKER_REQUEST
                || resultCode != Activity.RESULT_OK
                || data == null) {
            return;
        }

        selectedImages.clear();

        if (data.getClipData() != null) {
            int selectedCount = Math.min(
                    MAX_IMAGES,
                    data.getClipData().getItemCount()
            );

            for (int index = 0; index < selectedCount; index++) {
                Uri uri = data.getClipData()
                        .getItemAt(index)
                        .getUri();

                persistReadPermission(data, uri);
                selectedImages.add(uri);
            }

            if (data.getClipData().getItemCount() > MAX_IMAGES) {
                showMessage(
                        "Chỉ được chọn tối đa " + MAX_IMAGES
                                + " hình ảnh"
                );
            }

        } else if (data.getData() != null) {
            Uri uri = data.getData();

            persistReadPermission(data, uri);
            selectedImages.add(uri);
        }

        renderSelectedImagesFromDialog();
    }

    private void persistReadPermission(
            @NonNull Intent data,
            @NonNull Uri uri
    ) {
        int flags = data.getFlags()
                & Intent.FLAG_GRANT_READ_URI_PERMISSION;

        if (flags == 0) {
            return;
        }

        try {
            requireContext()
                    .getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
        } catch (SecurityException ignored) {
            // Some document providers do not support persistent URI permissions.
        }
    }

    private void renderSelectedImagesFromDialog() {
        if (openRoomDialog == null) {
            return;
        }

        TextView imageStatus = openRoomDialog.findViewById(
                R.id.room_images_status
        );

        LinearLayout preview = openRoomDialog.findViewById(
                R.id.room_image_preview
        );

        if (imageStatus == null || preview == null) {
            return;
        }

        imageStatus.setText(
                "Đã chọn " + selectedImages.size() + "/" + MAX_IMAGES
                        + " hình ảnh"
        );

        preview.removeAllViews();

        for (Uri uri : new ArrayList<>(selectedImages)) {
            TextView imageChip = new TextView(requireContext());

            imageChip.setText(
                    "Ảnh " + (selectedImages.indexOf(uri) + 1) + "  ×"
            );

            imageChip.setContentDescription("Bỏ chọn ảnh");
            imageChip.setPadding(12, 8, 12, 8);

            imageChip.setOnClickListener(
                    view -> {
                        selectedImages.remove(uri);
                        renderSelectedImagesFromDialog();
                    }
            );

            preview.addView(imageChip);
        }
    }

    private void renderSelectedImages(@NonNull RoomForm form) {
        form.imagePreview.removeAllViews();

        form.imageStatus.setText(
                "Đã chọn " + selectedImages.size() + "/" + MAX_IMAGES
                        + " hình ảnh"
        );

        for (Uri uri : new ArrayList<>(selectedImages)) {
            TextView imageChip = new TextView(requireContext());

            imageChip.setText(
                    "Ảnh " + (selectedImages.indexOf(uri) + 1) + "  ×"
            );

            imageChip.setPadding(12, 8, 12, 8);

            imageChip.setOnClickListener(
                    view -> {
                        selectedImages.remove(uri);
                        renderSelectedImages(form);
                    }
            );

            form.imagePreview.addView(imageChip);
        }
    }

    // -------------------------------------------------------------------------
    // Image upload
    // -------------------------------------------------------------------------

    private List<MultipartBody.Part> createImageParts() {
        List<MultipartBody.Part> imageParts = new ArrayList<>();

        for (Uri uri : selectedImages) {
            try {
                String mimeType = requireContext()
                        .getContentResolver()
                        .getType(uri);

                if (mimeType == null || !mimeType.startsWith("image/")) {
                    showMessage("Chỉ hỗ trợ tệp hình ảnh");
                    continue;
                }

                byte[] imageBytes = readBytes(uri);

                if (imageBytes.length == 0) {
                    showMessage("Không thể đọc hình ảnh đã chọn");
                    continue;
                }

                RequestBody imageBody = RequestBody.create(
                        imageBytes,
                        MediaType.parse(mimeType)
                );

                imageParts.add(
                        MultipartBody.Part.createFormData(
                                "images",
                                "room-image",
                                imageBody
                        )
                );

            } catch (Exception exception) {
                showMessage("Không thể đọc hình ảnh đã chọn");
            }
        }

        return imageParts;
    }

    private byte[] readBytes(@NonNull Uri uri) throws Exception {
        try (
                InputStream input = requireContext()
                        .getContentResolver()
                        .openInputStream(uri);

                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            if (input == null) {
                throw new IllegalArgumentException(
                        "Unable to open image"
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
    // Delete location and room
    // -------------------------------------------------------------------------

    private void confirmLocationDelete(@NonNull Location location) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa địa điểm?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {
                            if (location.location_id != null) {
                                ApiClient.api.deleteLocation(
                                        location.location_id.toString()
                                ).enqueue(
                                        deleteCallback(
                                                "Đã xóa địa điểm",
                                                true
                                        )
                                );
                            }
                        }
                )
                .show();
    }

    private void confirmRoomDelete(@NonNull Room room) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa phòng?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {
                            if (room.room_id != null) {
                                ApiClient.api.deleteRoom(
                                        room.room_id.toString()
                                ).enqueue(
                                        deleteCallback(
                                                "Đã xóa phòng",
                                                false
                                        )
                                );
                            }
                        }
                )
                .show();
    }

    private Callback<Void> deleteCallback(
            @NonNull String successMessage,
            boolean deleteLocation
    ) {
        return new Callback<Void>() {

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
                            getErrorMessage(
                                    response,
                                    response.code() == 409
                                            ? "Không thể xóa vì tài nguyên đang được sử dụng"
                                            : "Không thể xóa tài nguyên"
                            )
                    );

                    return;
                }

                showMessage(successMessage);

                if (deleteLocation) {
                    loadLocations();
                } else {
                    loadRooms();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        };
    }

    // -------------------------------------------------------------------------
    // Province and address spinner helpers
    // -------------------------------------------------------------------------

    private void setSpinner(
            @NonNull Spinner spinner,
            @NonNull String[] values,
            @Nullable String selectedValue
    ) {
        spinner.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        values
                )
        );

        if (selectedValue == null) {
            return;
        }

        for (int index = 0; index < values.length; index++) {
            if (values[index].equalsIgnoreCase(selectedValue)) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    @Nullable
    private Province selectedProvince(@NonNull Spinner spinner) {
        Object selectedItem = spinner.getSelectedItem();

        return selectedItem instanceof Province
                ? (Province) selectedItem
                : null;
    }

    private void setProvinceItems(
            @NonNull Spinner spinner,
            @NonNull List<Province> provinces
    ) {
        spinner.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        provinces
                )
        );
    }

    private void setLoadingSpinner(
            @NonNull Spinner spinner,
            @NonNull String message
    ) {
        spinner.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        Collections.singletonList(message)
                )
        );
    }

    private void loadDistricts(
            @NonNull Spinner provinceSpinner,
            @NonNull Spinner districtSpinner,
            @NonNull Spinner wardSpinner
    ) {
        Province selectedProvince = selectedProvince(provinceSpinner);

        if (selectedProvince == null) {
            setLoadingSpinner(districtSpinner, "Chọn tỉnh trước");
            setLoadingSpinner(wardSpinner, "Chọn quận/huyện trước");
            return;
        }

        setLoadingSpinner(
                districtSpinner,
                "Đang tải quận/huyện..."
        );

        setLoadingSpinner(
                wardSpinner,
                "Chọn quận/huyện trước"
        );

        ProvinceApiClient.api.legacyProvince(
                selectedProvince.code,
                2
        ).enqueue(new Callback<Province>() {

            @Override
            public void onResponse(
                    @NonNull Call<Province> call,
                    @NonNull Response<Province> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()
                        || response.body() == null) {
                    showMessage("Không thể tải quận/huyện");
                    return;
                }

                List<Province> districts = response.body().districts;

                setProvinceItems(
                        districtSpinner,
                        districts == null
                                ? new ArrayList<>()
                                : districts
                );

                setLoadingSpinner(
                        wardSpinner,
                        "Chọn quận/huyện trước"
                );
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

    private void loadWards(
            @NonNull Spinner districtSpinner,
            @NonNull Spinner wardSpinner
    ) {
        Province selectedDistrict = selectedProvince(districtSpinner);

        if (selectedDistrict == null) {
            setLoadingSpinner(
                    wardSpinner,
                    "Chọn quận/huyện trước"
            );
            return;
        }

        setLoadingSpinner(
                wardSpinner,
                "Đang tải phường/xã..."
        );

        ProvinceApiClient.api.legacyDistrict(
                selectedDistrict.code,
                2
        ).enqueue(new Callback<Province>() {

            @Override
            public void onResponse(
                    @NonNull Call<Province> call,
                    @NonNull Response<Province> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()
                        || response.body() == null) {
                    showMessage("Không thể tải phường/xã");
                    return;
                }

                List<Province> wards = response.body().wards;

                setProvinceItems(
                        wardSpinner,
                        wards == null
                                ? new ArrayList<>()
                                : wards
                );
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

    private void selectName(
            @NonNull Spinner spinner,
            @Nullable String name
    ) {
        if (name == null) {
            return;
        }

        for (int index = 0; index < spinner.getCount(); index++) {
            Object item = spinner.getItemAtPosition(index);

            if (item instanceof Province) {
                Province province = (Province) item;

                if (name.equals(province.name)) {
                    spinner.setSelection(index);
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Formatting and validation helpers
    // -------------------------------------------------------------------------

    private void setLoading(boolean isLoading) {
        if (loadingIndicator == null) {
            return;
        }

        loadingIndicator.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    private String getLocationLabel(@Nullable UUID locationId) {
        for (Location location : locations) {
            if (location.location_id != null
                    && location.location_id.equals(locationId)) {
                return getValue(location.location_code);
            }
        }

        return locationId == null
                ? UNKNOWN_VALUE
                : locationId.toString();
    }

    private String getAddress(@Nullable Location location) {
        if (location == null) {
            return UNKNOWN_VALUE;
        }

        return getValue(location.address_line)
                + ", " + getValue(location.ward_name)
                + ", " + getValue(location.district_name)
                + ", " + getValue(location.province_name);
    }

    static String roomName(Room room) {
        return room == null || text(room.room_name).isEmpty()
                ? "Chưa đặt tên phòng"
                : room.room_name;
    }

    private String getStatusLabel(@Nullable String status) {
        return STATUS_AVAILABLE.equalsIgnoreCase(status)
                ? "Đang sử dụng"
                : "Không khả dụng";
    }

    private static String normalizeText(@Nullable Object value) {
        return value == null
                ? ""
                : value.toString().trim().toLowerCase(Locale.ROOT);
    }

    private static String text(@Nullable Object value) {
        return normalizeText(value);
    }

    private static String getValue(@Nullable Object value) {
        return value == null || value.toString().trim().isEmpty()
                ? UNKNOWN_VALUE
                : value.toString();
    }

    @Nullable
    private static String getOptionalText(@NonNull EditText input) {
        String value = input.getText()
                .toString()
                .trim();

        return value.isEmpty() ? null : value;
    }

    @Nullable
    private static Integer parseOptionalInteger(
            @Nullable String text
    ) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return Integer.valueOf(text.trim());
    }

    @Nullable
    private static BigDecimal parseOptionalDecimal(
            @Nullable String text
    ) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return new BigDecimal(text.trim());
    }

    private String getErrorMessage(
            @NonNull Response<?> response,
            @NonNull String fallback
    ) {
        try {
            if (response.errorBody() != null) {
                String message = new org.json.JSONObject(
                        response.errorBody().string()
                ).optString("message");

                if (!message.trim().isEmpty()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
            // Use the fallback message when the response body cannot be parsed.
        }

        return fallback;
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

    private static class SimpleSelectListener
            implements AdapterView.OnItemSelectedListener {

        private final Runnable action;

        SimpleSelectListener(@NonNull Runnable action) {
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
        if (openRoomDialog != null && openRoomDialog.isShowing()) {
            openRoomDialog.dismiss();
        }

        openRoomDialog = null;
        rootView = null;

        searchInput = null;
        loadingIndicator = null;
        emptyState = null;
        propertyAdapter = null;

        selectedImages.clear();

        super.onDestroyView();
    }
}