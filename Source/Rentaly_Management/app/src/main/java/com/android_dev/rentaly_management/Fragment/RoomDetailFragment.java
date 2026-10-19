package com.android_dev.rentaly_management.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.android_dev.rentaly_management.DTO.RoomService;
import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.R;
import com.google.gson.Gson;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String ARG_ROOM_ID = "room_id";

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private static final String MIME_JSON = "application/json";

    private static final String UNKNOWN_LOCATION = "Chưa cập nhật";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private Room room;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private ViewFlipper imageSlider;

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
                R.layout.fragment_room_detail,
                container,
                false
        );

        bindViews();
        setupClickListeners();

        loadRoom(requireArguments().getString(ARG_ROOM_ID));

        return rootView;
    }

    private void bindViews() {
        imageSlider = rootView.findViewById(
                R.id.room_detail_slider
        );
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.room_detail_back)
                .setOnClickListener(
                        view -> NavHostFragment
                                .findNavController(this)
                                .navigateUp()
                );

        rootView.findViewById(R.id.room_detail_save)
                .setOnClickListener(
                        view -> saveRoom()
                );

        rootView.findViewById(R.id.room_detail_delete)
                .setOnClickListener(
                        view -> {
                            if (room != null) {
                                confirmRoomDelete();
                            }
                        }
                );

        rootView.findViewById(R.id.room_detail_previous)
                .setOnClickListener(
                        view -> imageSlider.showPrevious()
                );

        rootView.findViewById(R.id.room_detail_next)
                .setOnClickListener(
                        view -> imageSlider.showNext()
                );

        rootView.findViewById(R.id.room_detail_assign_service)
                .setOnClickListener(
                        view -> {
                            if (room != null) {
                                loadServicesForAssignment();
                            }
                        }
                );

        rootView.findViewById(R.id.room_detail_meters)
                .setOnClickListener(
                        view -> openMeterManagement()
                );
    }

    private void openMeterManagement() {
        if (room == null || room.room_id == null) {
            showMessage("Không xác định được phòng");
            return;
        }

        Bundle arguments = new Bundle();

        arguments.putString(
                ARG_ROOM_ID,
                room.room_id.toString()
        );

        NavHostFragment.findNavController(this).navigate(
                R.id.meterFragment,
                arguments
        );
    }

    // -------------------------------------------------------------------------
    // Load room details
    // -------------------------------------------------------------------------

    private void loadRoom(@Nullable String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            showMessage("Không tìm thấy mã phòng");
            return;
        }

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
                            showMessage("Không thể tải phòng");
                            return;
                        }

                        room = response.body();

                        bindRoomDetails();
                        loadLocation();
                        loadRoomServices();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Room> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage(
                                "Không thể kết nối đến máy chủ"
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Bind room details
    // -------------------------------------------------------------------------

    private void bindRoomDetails() {
        if (room == null || rootView == null) {
            return;
        }

        setEditText(
                R.id.room_detail_code,
                getValue(room.room_code)
        );

        setEditText(
                R.id.room_detail_name,
                getValue(room.room_name)
        );

        setEditText(
                R.id.room_detail_floor,
                getValue(room.floor)
        );

        setEditText(
                R.id.room_detail_area,
                getValue(room.area_m2)
        );

        setEditText(
                R.id.room_detail_occupants,
                getValue(room.max_occupants)
        );

        setEditText(
                R.id.room_detail_rent,
                getValue(room.rent_price)
        );

        setEditText(
                R.id.room_detail_description,
                getValue(room.description)
        );

        bindRoomStatus();
        bindRoomImages();
        bindLocationPlaceholder();
    }

    private void bindRoomStatus() {
        if (room == null || rootView == null) {
            return;
        }

        int statusViewId = STATUS_UNAVAILABLE.equalsIgnoreCase(
                room.status
        )
                ? R.id.room_status_unavailable
                : R.id.room_status_available;

        RadioButton statusButton = rootView.findViewById(
                statusViewId
        );

        statusButton.setChecked(true);
    }

    private void bindLocationPlaceholder() {
        TextView locationText = rootView.findViewById(
                R.id.room_detail_location
        );

        locationText.setText(
                room == null || room.location_id == null
                        ? "Địa điểm: " + UNKNOWN_LOCATION
                        : "Địa điểm: Đang tải địa chỉ..."
        );
    }

    // -------------------------------------------------------------------------
    // Room images
    // -------------------------------------------------------------------------

    private void bindRoomImages() {
        if (imageSlider == null) {
            return;
        }

        imageSlider.removeAllViews();

        if (room.image_urls == null || room.image_urls.isEmpty()) {
            showEmptyImageState();
            return;
        }

        for (String imageUrl : room.image_urls) {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                continue;
            }

            ImageView imageView = createRoomImageView();

            imageSlider.addView(imageView);

            loadImage(imageUrl, imageView);
        }

        if (imageSlider.getChildCount() == 0) {
            showEmptyImageState();
        }
    }

    private ImageView createRoomImageView() {
        ImageView imageView = new ImageView(requireContext());

        imageView.setContentDescription("Hình ảnh phòng");
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return imageView;
    }

    private void showEmptyImageState() {
        TextView emptyImage = new TextView(requireContext());

        emptyImage.setText("Chưa có hình ảnh phòng");

        imageSlider.addView(emptyImage);
    }

    /**
     * Loads a room image on a background thread.
     */
    private void loadImage(
            @NonNull String imageUrl,
            @NonNull ImageView imageView
    ) {
        new Thread(() -> {
            Bitmap bitmap;

            try (InputStream input = new URL(imageUrl).openStream()) {
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception exception) {
                return;
            }

            if (bitmap == null) {
                return;
            }

            android.app.Activity activity = getActivity();

            if (activity == null) {
                bitmap.recycle();
                return;
            }

            activity.runOnUiThread(() -> {
                if (rootView == null
                        || imageSlider == null
                        || imageView.getParent() != imageSlider
                        || !isAdded()) {
                    bitmap.recycle();
                    return;
                }

                imageView.setImageBitmap(bitmap);
            });
        }).start();
    }

    // -------------------------------------------------------------------------
    // Load location
    // -------------------------------------------------------------------------

    private void loadLocation() {
        if (room == null || room.location_id == null) {
            return;
        }

        ApiClient.api.location(
                room.location_id.toString()
        ).enqueue(new Callback<Location>() {

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
                    TextView locationText = rootView.findViewById(
                            R.id.room_detail_location
                    );

                    locationText.setText(
                            "Địa điểm: " + UNKNOWN_LOCATION
                    );

                    return;
                }

                Location location = response.body();

                TextView locationText = rootView.findViewById(
                        R.id.room_detail_location
                );

                locationText.setText(
                        "Địa điểm: " + formatLocationAddress(location)
                );
            }

            @Override
            public void onFailure(
                    @NonNull Call<Location> call,
                    @NonNull Throwable throwable
            ) {
                if (rootView == null || !isAdded()) {
                    return;
                }

                TextView locationText = rootView.findViewById(
                        R.id.room_detail_location
                );

                locationText.setText(
                        "Địa điểm: " + UNKNOWN_LOCATION
                );
            }
        });
    }

    private String formatLocationAddress(@NonNull Location location) {
        return getValue(location.location_code)
                + " - " + getValue(location.address_line)
                + ", " + getValue(location.ward_name)
                + ", " + getValue(location.district_name)
                + ", " + getValue(location.province_name);
    }

    // -------------------------------------------------------------------------
    // Save room
    // -------------------------------------------------------------------------

    private void saveRoom() {
        if (room == null || room.room_id == null) {
            showMessage("Phòng chưa tải xong");
            return;
        }

        try {
            EditText codeInput = findEditText(
                    R.id.room_detail_code
            );

            EditText nameInput = findEditText(
                    R.id.room_detail_name
            );

            EditText floorInput = findEditText(
                    R.id.room_detail_floor
            );

            EditText areaInput = findEditText(
                    R.id.room_detail_area
            );

            EditText occupantsInput = findEditText(
                    R.id.room_detail_occupants
            );

            EditText rentInput = findEditText(
                    R.id.room_detail_rent
            );

            EditText descriptionInput = findEditText(
                    R.id.room_detail_description
            );

            RadioGroup statusGroup = rootView.findViewById(
                    R.id.room_detail_status
            );

            String code = codeInput.getText().toString().trim();
            String occupantsText = occupantsInput.getText()
                    .toString()
                    .trim();

            String rentText = rentInput.getText()
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

            if (occupants < 1 || rent.signum() < 0) {
                throw new IllegalArgumentException();
            }

            BigDecimal area = parseOptionalDecimal(
                    areaInput.getText().toString()
            );

            if (area != null && area.signum() < 0) {
                throw new IllegalArgumentException();
            }

            int selectedStatusId =
                    statusGroup.getCheckedRadioButtonId();

            if (selectedStatusId == -1) {
                showMessage("Vui lòng chọn trạng thái phòng");
                return;
            }

            String status = selectedStatusId
                    == R.id.room_status_unavailable
                    ? STATUS_UNAVAILABLE
                    : STATUS_AVAILABLE;

            RoomRequest request = new RoomRequest(
                    room.location_id,
                    code,
                    getOptionalText(nameInput),
                    parseOptionalInteger(
                            floorInput.getText().toString()
                    ),
                    area,
                    occupants,
                    rent,
                    status,
                    getOptionalText(descriptionInput)
            );

            RequestBody body = RequestBody.create(
                    new Gson().toJson(request),
                    MediaType.parse(MIME_JSON)
            );

            submitRoomUpdate(body);

        } catch (Exception exception) {
            showMessage(
                    "Vui lòng kiểm tra thông tin phòng"
            );
        }
    }

    private void submitRoomUpdate(@NonNull RequestBody body) {
        rootView.findViewById(R.id.room_detail_save)
                .setEnabled(false);

        ApiClient.api.updateRoom(
                room.room_id.toString(),
                body,
                new ArrayList<MultipartBody.Part>()
        ).enqueue(new Callback<Room>() {

            @Override
            public void onResponse(
                    @NonNull Call<Room> call,
                    @NonNull Response<Room> response
            ) {
                if (!isAdded() || rootView == null) {
                    return;
                }

                rootView.findViewById(R.id.room_detail_save)
                        .setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    showMessage("Không thể cập nhật phòng");
                    return;
                }

                room = response.body();

                bindRoomDetails();
                loadLocation();

                showMessage("Đã cập nhật phòng");
            }

            @Override
            public void onFailure(
                    @NonNull Call<Room> call,
                    @NonNull Throwable throwable
            ) {
                if (!isAdded() || rootView == null) {
                    return;
                }

                rootView.findViewById(R.id.room_detail_save)
                        .setEnabled(true);

                showMessage(
                        "Không thể kết nối đến máy chủ"
                );
            }
        });
    }

    // -------------------------------------------------------------------------
    // Delete room
    // -------------------------------------------------------------------------

    private void confirmRoomDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa phòng?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteRoom()
                )
                .show();
    }

    private void deleteRoom() {
        if (room == null || room.room_id == null) {
            showMessage("Không xác định được phòng");
            return;
        }

        ApiClient.api.deleteRoom(
                room.room_id.toString()
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    NavHostFragment.findNavController(
                            RoomDetailFragment.this
                    ).navigateUp();

                    return;
                }

                showMessage(
                        response.code() == 409
                                ? "Không thể xóa vì phòng đang có hợp đồng"
                                : "Không thể xóa phòng"
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
    // Load room services
    // -------------------------------------------------------------------------

    private void loadRoomServices() {
        if (room == null || room.room_id == null) {
            return;
        }

        ApiClient.api.roomServices(
                room.room_id.toString()
        ).enqueue(new Callback<List<RoomService>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<RoomService>> call,
                    @NonNull Response<List<RoomService>> response
            ) {
                if (!isAdded() || rootView == null) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showMessage("Không thể tải dịch vụ phòng");
                    return;
                }

                renderRoomServices(response.body());
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<RoomService>> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void renderRoomServices(
            @NonNull List<RoomService> services
    ) {
        LinearLayout serviceList = rootView.findViewById(
                R.id.room_detail_services_list
        );

        View emptyState = rootView.findViewById(
                R.id.room_detail_services_empty
        );

        serviceList.removeAllViews();

        emptyState.setVisibility(
                services.isEmpty() ? View.VISIBLE : View.GONE
        );

        for (RoomService service : services) {
            if (service == null) {
                continue;
            }

            serviceList.addView(
                    createRoomServiceRow(service)
            );
        }
    }

    private LinearLayout createRoomServiceRow(
            @NonNull RoomService service
    ) {
        LinearLayout row = new LinearLayout(requireContext());

        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(requireContext());

        label.setText(formatRoomService(service));

        label.setTextColor(
                requireContext().getResources()
                        .getColor(R.color.secondary)
        );

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        label.setLayoutParams(labelParams);

        row.addView(label);

        boolean isActive = Boolean.TRUE.equals(service.is_active);

        Button toggleButton = new Button(requireContext());

        toggleButton.setText(isActive ? "Tắt" : "Bật");

        toggleButton.setOnClickListener(
                view -> updateRoomService(
                        service,
                        !Boolean.TRUE.equals(service.is_active)
                )
        );

        row.addView(toggleButton);

        Button removeButton = new Button(requireContext());

        removeButton.setText("Xóa");

        removeButton.setOnClickListener(
                view -> confirmRoomServiceDelete(service)
        );

        row.addView(removeButton);

        return row;
    }

    private String formatRoomService(@NonNull RoomService service) {
        return getValue(service.name)
                + " | " + getValue(service.unit)
                + " | " + getValue(service.calculation_method)
                + " | " + getValue(service.default_unit_price)
                + " | "
                + (Boolean.TRUE.equals(service.is_active)
                ? "Đang bật"
                : "Đang tắt");
    }

    // -------------------------------------------------------------------------
    // Assign service to room
    // -------------------------------------------------------------------------

    private void loadServicesForAssignment() {
        if (room == null || room.room_id == null) {
            showMessage("Không xác định được phòng");
            return;
        }

        ApiClient.api.services().enqueue(
                new Callback<List<Service>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Service>> call,
                            @NonNull Response<List<Service>> response
                    ) {
                        if (!isAdded()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage(
                                    "Không thể tải danh mục dịch vụ"
                            );
                            return;
                        }

                        loadExistingServicesForAssignment(
                                response.body()
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Service>> call,
                            @NonNull Throwable throwable
                    ) {
                        showMessage(
                                "Không thể kết nối đến máy chủ"
                        );
                    }
                }
        );
    }

    private void loadExistingServicesForAssignment(
            @NonNull List<Service> allServices
    ) {
        ApiClient.api.roomServices(
                room.room_id.toString()
        ).enqueue(new Callback<List<RoomService>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<RoomService>> call,
                    @NonNull Response<List<RoomService>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showMessage("Không thể tải dịch vụ phòng");
                    return;
                }

                List<Service> availableServices = filterAvailableServices(
                        allServices,
                        response.body()
                );

                showAvailableServices(availableServices);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<RoomService>> call,
                    @NonNull Throwable throwable
            ) {
                showMessage(
                        "Không thể kết nối đến máy chủ"
                );
            }
        });
    }

    private List<Service> filterAvailableServices(
            @NonNull List<Service> allServices,
            @NonNull List<RoomService> assignedServices
    ) {
        List<Service> availableServices = new ArrayList<>();

        for (Service service : allServices) {
            if (service == null || service.service_id == null) {
                continue;
            }

            boolean alreadyAssigned = false;

            for (RoomService assigned : assignedServices) {
                if (assigned != null
                        && assigned.service_id != null
                        && service.service_id.equals(assigned.service_id)) {
                    alreadyAssigned = true;
                    break;
                }
            }

            if (!alreadyAssigned) {
                availableServices.add(service);
            }
        }

        return availableServices;
    }

    private void showAvailableServices(
            @NonNull List<Service> availableServices
    ) {
        if (availableServices.isEmpty()) {
            showMessage("Không còn dịch vụ để gán");
            return;
        }

        String[] labels = new String[availableServices.size()];

        for (int index = 0; index < availableServices.size(); index++) {
            Service service = availableServices.get(index);

            labels[index] = service.name + " (" + service.unit + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn dịch vụ")
                .setItems(
                        labels,
                        (dialog, selectedIndex) -> {
                            Service service = availableServices.get(
                                    selectedIndex
                            );

                            assignRoomService(service);
                        }
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void assignRoomService(@NonNull Service service) {
        if (room == null
                || room.room_id == null
                || service.service_id == null) {
            showMessage("Không xác định được phòng hoặc dịch vụ");
            return;
        }

        ApiClient.api.assignRoomService(
                room.room_id.toString(),
                service.service_id.toString()
        ).enqueue(
                roomServiceMutationCallback("Đã gán dịch vụ")
        );
    }

    // -------------------------------------------------------------------------
    // Update room service
    // -------------------------------------------------------------------------

    private void updateRoomService(
            @NonNull RoomService service,
            boolean active
    ) {
        if (room == null
                || room.room_id == null
                || service.service_id == null) {
            showMessage("Không xác định được phòng hoặc dịch vụ");
            return;
        }

        Map<String, Boolean> request = new HashMap<>();

        request.put("is_active", active);

        ApiClient.api.updateRoomService(
                room.room_id.toString(),
                service.service_id.toString(),
                request
        ).enqueue(
                roomServiceMutationCallback(
                        active ? "Đã bật dịch vụ" : "Đã tắt dịch vụ"
                )
        );
    }

    // -------------------------------------------------------------------------
    // Delete room service
    // -------------------------------------------------------------------------

    private void confirmRoomServiceDelete(
            @NonNull RoomService service
    ) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Bỏ dịch vụ?")
                .setMessage(
                        "Thao tác này sẽ xóa liên kết dịch vụ khỏi phòng."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> deleteRoomService(service)
                )
                .show();
    }

    private void deleteRoomService(@NonNull RoomService service) {
        if (room == null
                || room.room_id == null
                || service.service_id == null) {
            showMessage("Không xác định được phòng hoặc dịch vụ");
            return;
        }

        ApiClient.api.deleteRoomService(
                room.room_id.toString(),
                service.service_id.toString()
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
                    showMessage("Không thể bỏ dịch vụ");
                    return;
                }

                showMessage("Đã bỏ dịch vụ");
                loadRoomServices();
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
    // Shared room service callback
    // -------------------------------------------------------------------------

    private Callback<RoomService> roomServiceMutationCallback(
            @NonNull String successMessage
    ) {
        return new Callback<RoomService>() {

            @Override
            public void onResponse(
                    @NonNull Call<RoomService> call,
                    @NonNull Response<RoomService> response
            ) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    showMessage(
                            response.code() == 409
                                    ? "Dịch vụ đã được gán cho phòng"
                                    : "Không thể cập nhật dịch vụ"
                    );
                    return;
                }

                showMessage(successMessage);
                loadRoomServices();
            }

            @Override
            public void onFailure(
                    @NonNull Call<RoomService> call,
                    @NonNull Throwable throwable
            ) {
                showMessage("Không thể kết nối đến máy chủ");
            }
        };
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    @NonNull
    private EditText findEditText(int viewId) {
        return rootView.findViewById(viewId);
    }

    @Nullable
    private Integer parseOptionalInteger(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return Integer.valueOf(value.trim());
    }

    @Nullable
    private BigDecimal parseOptionalDecimal(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return new BigDecimal(value.trim());
    }

    @Nullable
    private String getOptionalText(@NonNull EditText input) {
        String value = input.getText()
                .toString()
                .trim();

        return value.isEmpty() ? null : value;
    }

    @NonNull
    private String getValue(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    private void setEditText(int viewId, @NonNull String value) {
        EditText editText = rootView.findViewById(viewId);
        editText.setText(value);
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
        rootView = null;
        imageSlider = null;

        super.onDestroyView();
    }
}