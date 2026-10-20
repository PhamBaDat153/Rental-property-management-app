package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Adapter.AnnouncementAdapter;
import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Announcement;
import com.android_dev.rentaly_management.DTO.AnnouncementRequest;
import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementFragment extends Fragment {

    // Constants
    private static final String ARG_CREATE_ANNOUNCEMENT = "create_announcement";
    private static final int RECIPIENT_DATA_REQUEST_COUNT = 4;
    private static final String STATUS_SENT = "SENTED";

    // Data
    private final List<Announcement> announcements = new ArrayList<>();
    private final List<UserTenant> users = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<ContractTenant> contractTenants = new ArrayList<>();

    // Recipient selection state
    private final Set<UUID> selectedRecipientIds = new HashSet<>();

    private List<AnnouncementRecipient> recipients = new ArrayList<>();
    private List<AnnouncementRecipient> visibleRecipients = new ArrayList<>();
    private List<UUID> roomIds = new ArrayList<>();

    // Views and adapters
    private AnnouncementAdapter announcementAdapter;
    private ArrayAdapter<String> recipientAdapter;

    private ProgressBar loadingIndicator;
    private TextView emptyState;
    private TextView recipientCount;

    private EditText recipientSearch;
    private Spinner roomFilter;
    private CheckBox selectAll;

    private AlertDialog formDialog;

    // -------------------------------------------------------------------------
    // Fragment lifecycle
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_announcement,
                container,
                false
        );

        bindFragmentViews(view);
        setupAnnouncementList(view);
        setupNavigation(view);

        loadAnnouncements();

        if (getArguments() != null
                && getArguments().getBoolean(ARG_CREATE_ANNOUNCEMENT, false)) {
            Announcement announcementToEdit = announcementFromArguments(getArguments());
            view.post(() -> loadRecipientData(announcementToEdit));
        }

        return view;
    }

    private void bindFragmentViews(@NonNull View view) {
        loadingIndicator = view.findViewById(R.id.announcement_loading);
        emptyState = view.findViewById(R.id.announcement_empty);
    }

    private void setupAnnouncementList(@NonNull View view) {
        announcementAdapter = new AnnouncementAdapter(
                requireContext(),
                announcements
        );

        ListView announcementList = view.findViewById(
                R.id.announcement_list
        );

        announcementList.setAdapter(announcementAdapter);
        announcementList.setOnItemClickListener((parent, row, position, id) -> {
            if (position < 0 || position >= announcements.size()) {
                return;
            }

            Announcement announcement = announcements.get(position);
            if (announcement.announcement_id == null) {
                showMessage("Không xác định được thông báo");
                return;
            }

            Bundle args = new Bundle();
            args.putString("announcement_id", announcement.announcement_id.toString());
            args.putString("announcement_content", announcement.content);
            args.putString("announcement_type", announcement.announcement_type);
            args.putString("announcement_status", announcement.status);
            args.putString("announcement_created_at", announcement.created_at == null ? null : announcement.created_at.toString());
            args.putString("announcement_sent_at", announcement.sent_at == null ? null : announcement.sent_at.toString());
            args.putSerializable("announcement_recipient_ids", new ArrayList<>(announcement.recipient_ids == null ? new ArrayList<>() : announcement.recipient_ids));
            NavHostFragment.findNavController(this).navigate(R.id.announcementDetailFragment, args);
        });
    }

    private void setupNavigation(@NonNull View view) {
        view.findViewById(R.id.announcement_back).setOnClickListener(
                ignored -> NavHostFragment.findNavController(this).navigateUp()
        );

        view.findViewById(R.id.announcement_add).setOnClickListener(
                ignored -> loadRecipientData(null)
        );
    }

    // -------------------------------------------------------------------------
    // Load announcements
    // -------------------------------------------------------------------------

    private void loadAnnouncements() {
        setLoading(true);

        ApiClient.api.announcements().enqueue(
                new Callback<List<Announcement>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Announcement>> call,
                            @NonNull Response<List<Announcement>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Không thể tải thông báo");
                            return;
                        }

                        announcements.clear();
                        announcements.addAll(response.body());

                        announcementAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Announcement>> call,
                            @NonNull Throwable throwable
                    ) {
                        setLoading(false);
                        showMessage("Không thể kết nối đến máy chủ");
                    }
                }
        );
    }

    private void updateEmptyState() {
        emptyState.setVisibility(
                announcements.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    // -------------------------------------------------------------------------
    // Load data for recipient selection
    // -------------------------------------------------------------------------

    private void loadRecipientData(
            @Nullable Announcement announcementToEdit
    ) {
        setLoading(true);

        RecipientLoadTracker tracker = new RecipientLoadTracker(
                announcementToEdit
        );

        enqueueRecipientRequest(
                ApiClient.api.managedUsers(null),
                users,
                tracker
        );

        enqueueRecipientRequest(
                ApiClient.api.rooms(),
                rooms,
                tracker
        );

        enqueueRecipientRequest(
                ApiClient.api.contracts(),
                contracts,
                tracker
        );

        enqueueRecipientRequest(
                ApiClient.api.contractTenants(),
                contractTenants,
                tracker
        );
    }

    private <T> void enqueueRecipientRequest(
            @NonNull Call<List<T>> call,
            @NonNull List<T> destination,
            @NonNull RecipientLoadTracker tracker
    ) {
        call.enqueue(new Callback<List<T>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<T>> request,
                    @NonNull Response<List<T>> response
            ) {
                boolean succeeded = response.isSuccessful()
                        && response.body() != null;

                if (succeeded) {
                    destination.clear();
                    destination.addAll(response.body());
                }

                tracker.onRequestCompleted(!succeeded);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<T>> request,
                    @NonNull Throwable throwable
            ) {
                tracker.onRequestCompleted(true);
            }
        });
    }

    private void finishRecipientDataLoad(
            boolean failed,
            @Nullable Announcement announcementToEdit
    ) {
        setLoading(false);

        if (failed) {
            showMessage(
                    "Không thể tải đủ dữ liệu người nhận và phòng. "
                            + "Vui lòng thử lại."
            );
            return;
        }

        showAnnouncementForm(announcementToEdit);
    }

    /**
     * Tracks completion of the four recipient-related API requests.
     */
    private final class RecipientLoadTracker {

        private final Announcement announcementToEdit;

        private int completedRequests;
        private boolean failed;

        private RecipientLoadTracker(
                @Nullable Announcement announcementToEdit
        ) {
            this.announcementToEdit = announcementToEdit;
        }

        private void onRequestCompleted(boolean requestFailed) {
            failed |= requestFailed;
            completedRequests++;

            if (completedRequests == RECIPIENT_DATA_REQUEST_COUNT) {
                finishRecipientDataLoad(
                        failed,
                        announcementToEdit
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // Create/edit announcement form
    // -------------------------------------------------------------------------

    @Nullable
    private Announcement announcementFromArguments(@NonNull Bundle args) {
        String id = args.getString("announcement_id");
        if (id == null) return null;

        Announcement announcement = new Announcement();
        try {
            announcement.announcement_id = UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        ArrayList<UUID> recipientIds = (ArrayList<UUID>) args.getSerializable("announcement_recipient_ids");
        announcement.recipient_ids = recipientIds == null ? new ArrayList<>() : recipientIds;
        announcement.content = args.getString("announcement_content");
        announcement.announcement_type = args.getString("announcement_type");
        announcement.status = args.getString("announcement_status");
        return announcement;
    }

    private void showAnnouncementForm(
            @Nullable Announcement announcementToEdit
    ) {
        View formView = getLayoutInflater().inflate(
                R.layout.dialog_create_announcement,
                null
        );

        EditText contentInput = formView.findViewById(
                R.id.announcement_form_content
        );

        EditText typeInput = formView.findViewById(
                R.id.announcement_form_type
        );

        recipientSearch = formView.findViewById(
                R.id.announcement_form_search
        );

        roomFilter = formView.findViewById(
                R.id.announcement_form_room
        );

        recipientCount = formView.findViewById(
                R.id.announcement_form_recipient_count
        );

        selectAll = formView.findViewById(
                R.id.announcement_form_select_all
        );

        ListView recipientList = formView.findViewById(
                R.id.announcement_form_recipients
        );

        CheckBox sendCheckbox = formView.findViewById(
                R.id.announcement_form_send
        );

        prepareFormData(announcementToEdit);

        populateFormInputs(
                announcementToEdit,
                contentInput,
                typeInput,
                sendCheckbox
        );

        setupRoomFilter();
        setupRecipientList(recipientList);
        setupRecipientSearch(recipientList);
        setupSelectAll(recipientList);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(
                        announcementToEdit == null
                                ? "Tạo thông báo"
                                : "Sửa thông báo"
                )
                .setView(formView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        formDialog = dialog;

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view ->
                                saveAnnouncement(
                                        announcementToEdit,
                                        contentInput,
                                        typeInput,
                                        sendCheckbox
                                )
                        )
        );

        dialog.show();
    }

    private void prepareFormData(
            @Nullable Announcement announcementToEdit
    ) {
        recipients = AnnouncementRecipient.build(
                users,
                rooms,
                contracts,
                contractTenants
        );

        selectedRecipientIds.clear();

        if (announcementToEdit != null
                && announcementToEdit.recipient_ids != null) {
            selectedRecipientIds.addAll(announcementToEdit.recipient_ids);
        }
    }

    private void populateFormInputs(
            @Nullable Announcement announcement,
            @NonNull EditText contentInput,
            @NonNull EditText typeInput,
            @NonNull CheckBox sendCheckbox
    ) {
        contentInput.setText(
                announcement == null ? "" : announcement.content
        );

        typeInput.setText(
                announcement == null
                        ? ""
                        : announcement.announcement_type
        );

        sendCheckbox.setChecked(
                announcement != null
                        && STATUS_SENT.equalsIgnoreCase(announcement.status)
        );
    }

    // -------------------------------------------------------------------------
    // Recipient list setup
    // -------------------------------------------------------------------------

    private void setupRecipientList(
            @NonNull ListView recipientList
    ) {
        recipientAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_multiple_choice,
                new ArrayList<>()
        );

        recipientList.setAdapter(recipientAdapter);
        recipientList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        recipientList.setOnItemClickListener(
                (parent, row, position, id) -> {
                    UUID recipientId = visibleRecipients
                            .get(position)
                            .userId;

                    if (recipientList.isItemChecked(position)) {
                        selectedRecipientIds.add(recipientId);
                    } else {
                        selectedRecipientIds.remove(recipientId);
                    }

                    updateSelectAllState();
                    updateRecipientCount();
                }
        );

        renderRecipients(recipientList);
    }

    private void setupRecipientSearch(
            @NonNull ListView recipientList
    ) {
        recipientSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
                // No action needed before the text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                renderRecipients(recipientList);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed after the text changes.
            }
        });

        roomFilter.setOnItemSelectedListener(
                new SimpleSelectListener(
                        () -> renderRecipients(recipientList)
                )
        );
    }

    private void setupSelectAll(
            @NonNull ListView recipientList
    ) {
        selectAll.setOnClickListener(view -> {
            boolean shouldSelect = selectAll.isChecked();

            for (AnnouncementRecipient recipient : visibleRecipients) {
                if (shouldSelect) {
                    selectedRecipientIds.add(recipient.userId);
                } else {
                    selectedRecipientIds.remove(recipient.userId);
                }
            }

            renderRecipients(recipientList);
        });
    }

    // -------------------------------------------------------------------------
    // Room filter
    // -------------------------------------------------------------------------

    private void setupRoomFilter() {
        List<String> roomLabels = new ArrayList<>();
        roomIds = new ArrayList<>();

        roomLabels.add("Tất cả phòng");
        roomIds.add(null);

        for (Room room : rooms) {
            roomLabels.add(getRoomLabel(room));
            roomIds.add(room.room_id);
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roomLabels
        );

        roomFilter.setAdapter(roomAdapter);
    }

    @Nullable
    private UUID getSelectedRoomId() {
        if (roomFilter == null) {
            return null;
        }

        int selectedPosition = roomFilter.getSelectedItemPosition();

        if (selectedPosition < 0
                || selectedPosition >= roomIds.size()) {
            return null;
        }

        return roomIds.get(selectedPosition);
    }

    // -------------------------------------------------------------------------
    // Recipient filtering and display
    // -------------------------------------------------------------------------

    private void renderRecipients(
            @NonNull ListView recipientList
    ) {
        UUID selectedRoomId = getSelectedRoomId();

        String query = recipientSearch == null
                ? ""
                : recipientSearch.getText().toString();

        visibleRecipients = AnnouncementRecipient.filter(
                recipients,
                query,
                selectedRoomId
        );

        List<String> recipientLabels = new ArrayList<>();

        for (AnnouncementRecipient recipient : visibleRecipients) {
            recipientLabels.add(
                    recipient.name + "\n" + recipient.roomText()
            );
        }

        recipientAdapter.clear();
        recipientAdapter.addAll(recipientLabels);
        recipientAdapter.notifyDataSetChanged();

        // Restore selection after the filtered list is refreshed.
        recipientList.clearChoices();

        for (int position = 0;
             position < visibleRecipients.size();
             position++) {

            UUID recipientId = visibleRecipients
                    .get(position)
                    .userId;

            recipientList.setItemChecked(
                    position,
                    selectedRecipientIds.contains(recipientId)
            );
        }

        updateRecipientCount();
        updateSelectAllState();
    }

    private void updateRecipientCount() {
        if (recipientCount == null) {
            return;
        }

        String countText = "Đang hiển thị "
                + visibleRecipients.size()
                + " người nhận; đã chọn "
                + selectedRecipientIds.size();

        recipientCount.setText(countText);
    }

    private void updateSelectAllState() {
        if (selectAll == null) {
            return;
        }

        boolean allVisibleRecipientsSelected =
                !visibleRecipients.isEmpty();

        for (AnnouncementRecipient recipient : visibleRecipients) {
            if (!selectedRecipientIds.contains(recipient.userId)) {
                allVisibleRecipientsSelected = false;
                break;
            }
        }

        selectAll.setChecked(allVisibleRecipientsSelected);
    }

    // -------------------------------------------------------------------------
    // Save announcement
    // -------------------------------------------------------------------------

    private void saveAnnouncement(
            @Nullable Announcement announcementToEdit,
            @NonNull EditText contentInput,
            @NonNull EditText typeInput,
            @NonNull CheckBox sendCheckbox
    ) {
        String content = contentInput.getText()
                .toString()
                .trim();

        String type = typeInput.getText()
                .toString()
                .trim();

        if (content.isEmpty()) {
            contentInput.setError("Nhập nội dung thông báo");
            contentInput.requestFocus();
            return;
        }
        if (selectedRecipientIds.isEmpty()) {
            recipientCount.setError("Chọn ít nhất một người nhận");
            recipientCount.requestFocus();
            return;
        }

        AnnouncementRequest request = new AnnouncementRequest(
                content,
                type,
                sendCheckbox.isChecked(),
                new ArrayList<>(selectedRecipientIds)
        );

        Call<Announcement> saveCall;

        if (announcementToEdit == null) {
            saveCall = ApiClient.api.createAnnouncement(request);
        } else {
            saveCall = ApiClient.api.updateAnnouncement(
                    announcementToEdit.announcement_id.toString(),
                    request
            );
        }

        if (formDialog != null) {
            formDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            formDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            formDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Đang lưu...");
        }

        saveCall.enqueue(new Callback<Announcement>() {

            @Override
            public void onResponse(
                    @NonNull Call<Announcement> call,
                    @NonNull Response<Announcement> response
            ) {
                if (!response.isSuccessful()) {
                    resetFormButtons();
                    showMessage("Không thể lưu thông báo");
                    return;
                }

                if (formDialog != null) {
                    formDialog.dismiss();
                }

                showMessage("Đã lưu thông báo");
                loadAnnouncements();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Announcement> call,
                    @NonNull Throwable throwable
            ) {
                resetFormButtons();
                showMessage("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void resetFormButtons() {
        if (formDialog == null) return;
        formDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        formDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
        formDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Lưu");
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private String getRoomLabel(@NonNull Room room) {
        if (room.room_name == null
                || room.room_name.trim().isEmpty()) {
            return room.room_code;
        }

        return room.room_name + " (" + room.room_code + ")";
    }

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
    // Spinner selection listener
    // -------------------------------------------------------------------------

    private static class SimpleSelectListener
            implements AdapterView.OnItemSelectedListener {

        private final Runnable action;

        private SimpleSelectListener(@NonNull Runnable action) {
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
        public void onNothingSelected(
                AdapterView<?> parent
        ) {
            // No action needed when the spinner has no selection.
        }
    }
}