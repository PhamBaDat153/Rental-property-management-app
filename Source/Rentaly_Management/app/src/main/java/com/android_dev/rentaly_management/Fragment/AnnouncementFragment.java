package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Announcement;
import com.android_dev.rentaly_management.DTO.AnnouncementRequest;
import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementFragment extends Fragment {
    private final List<Announcement> items = new ArrayList<>();
    private final List<UserTenant> users = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<ContractTenant> assignments = new ArrayList<>();
    private ArrayAdapter<Announcement> adapter;
    private ProgressBar loading;
    private TextView empty;
    private AlertDialog formDialog;
    private List<AnnouncementRecipient> recipients = new ArrayList<>();
    private List<AnnouncementRecipient> visibleRecipients = new ArrayList<>();
    private final Set<UUID> selectedIds = new HashSet<>();
    private EditText recipientSearch;
    private Spinner roomFilter;
    private TextView recipientCount;
    private CheckBox selectAll;
    private ArrayAdapter<String> recipientAdapter;
    private List<UUID> roomIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        loading = view.findViewById(R.id.announcement_loading);
        empty = view.findViewById(R.id.announcement_empty);
        view.findViewById(R.id.announcement_back).setOnClickListener(x -> NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.announcement_add).setOnClickListener(x -> loadUsers(null));
        adapter = new ArrayAdapter<Announcement>(requireContext(), R.layout.item_announcement, R.id.announcement_item_content, items) {
            @NonNull @Override public View getView(int position, View convert, @NonNull ViewGroup parent) {
                View row = super.getView(position, convert, parent);
                Announcement announcement = getItem(position);
                ((TextView) row.findViewById(R.id.announcement_item_content)).setText(announcement.content);
                ((TextView) row.findViewById(R.id.announcement_item_meta)).setText(
                        ("SENTED".equalsIgnoreCase(announcement.status) ? "Đã gửi" : "Bản nháp")
                                + " | Người nhận: " + (announcement.recipient_ids == null ? 0 : announcement.recipient_ids.size()));
                return row;
            }
        };
        ((ListView) view.findViewById(R.id.announcement_list)).setAdapter(adapter);
        load();
        return view;
    }

    private void load() {
        loading.setVisibility(View.VISIBLE);
        ApiClient.api.announcements().enqueue(new Callback<List<Announcement>>() {
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                loading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    items.clear(); items.addAll(response.body()); adapter.notifyDataSetChanged();
                    empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else error("Không thể tải thông báo");
            }
            public void onFailure(Call<List<Announcement>> call, Throwable error) { loading.setVisibility(View.GONE); error("Không thể kết nối đến máy chủ"); }
        });
    }

    private void loadUsers(Announcement old) {
        loading.setVisibility(View.VISIBLE);
        final boolean[] done = {false, false, false, false};
        final boolean[] failed = {false};
        ApiClient.api.managedUsers(null).enqueue(new Callback<List<UserTenant>>() {
            public void onResponse(Call<List<UserTenant>> c, Response<List<UserTenant>> r) { if (r.isSuccessful() && r.body() != null) { users.clear(); users.addAll(r.body()); } else failed[0] = true; done[0] = true; finishRecipientLoad(done, failed, old); }
            public void onFailure(Call<List<UserTenant>> c, Throwable t) { failed[0] = true; done[0] = true; finishRecipientLoad(done, failed, old); }
        });
        ApiClient.api.rooms().enqueue(new Callback<List<Room>>() {
            public void onResponse(Call<List<Room>> c, Response<List<Room>> r) { if (r.isSuccessful() && r.body() != null) { rooms.clear(); rooms.addAll(r.body()); } else failed[0] = true; done[1] = true; finishRecipientLoad(done, failed, old); }
            public void onFailure(Call<List<Room>> c, Throwable t) { failed[0] = true; done[1] = true; finishRecipientLoad(done, failed, old); }
        });
        ApiClient.api.contracts().enqueue(new Callback<List<RentalContract>>() {
            public void onResponse(Call<List<RentalContract>> c, Response<List<RentalContract>> r) { if (r.isSuccessful() && r.body() != null) { contracts.clear(); contracts.addAll(r.body()); } else failed[0] = true; done[2] = true; finishRecipientLoad(done, failed, old); }
            public void onFailure(Call<List<RentalContract>> c, Throwable t) { failed[0] = true; done[2] = true; finishRecipientLoad(done, failed, old); }
        });
        ApiClient.api.contractTenants().enqueue(new Callback<List<ContractTenant>>() {
            public void onResponse(Call<List<ContractTenant>> c, Response<List<ContractTenant>> r) { if (r.isSuccessful() && r.body() != null) { assignments.clear(); assignments.addAll(r.body()); } else failed[0] = true; done[3] = true; finishRecipientLoad(done, failed, old); }
            public void onFailure(Call<List<ContractTenant>> c, Throwable t) { failed[0] = true; done[3] = true; finishRecipientLoad(done, failed, old); }
        });
    }

    private void finishRecipientLoad(boolean[] done, boolean[] failed, Announcement old) {
        if (!done[0] || !done[1] || !done[2] || !done[3]) return;
        loading.setVisibility(View.GONE);
        if (failed[0]) { error("Không thể tải đủ dữ liệu người nhận và phòng. Vui lòng thử lại."); return; }
        form(old);
    }

    private void form(Announcement old) {
        View view = getLayoutInflater().inflate(R.layout.dialog_create_announcement, null);
        EditText content = view.findViewById(R.id.announcement_form_content);
        EditText type = view.findViewById(R.id.announcement_form_type);
        recipientSearch = view.findViewById(R.id.announcement_form_search);
        roomFilter = view.findViewById(R.id.announcement_form_room);
        recipientCount = view.findViewById(R.id.announcement_form_recipient_count);
        selectAll = view.findViewById(R.id.announcement_form_select_all);
        ListView recipientList = view.findViewById(R.id.announcement_form_recipients);
        CheckBox send = view.findViewById(R.id.announcement_form_send);
        recipients = AnnouncementRecipient.build(users, rooms, contracts, assignments);
        selectedIds.clear();
        if (old != null && old.recipient_ids != null) selectedIds.addAll(old.recipient_ids);
        content.setText(old == null ? "" : old.content);
        type.setText(old == null ? "" : old.announcement_type);
        send.setChecked(old != null && "SENTED".equalsIgnoreCase(old.status));
        setupRoomFilter();
        recipientAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
        recipientList.setAdapter(recipientAdapter);
        recipientList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        recipientList.setOnItemClickListener((parent, row, position, id) -> {
            UUID userId = visibleRecipients.get(position).userId;
            if (recipientList.isItemChecked(position)) selectedIds.add(userId); else selectedIds.remove(userId);
            updateSelectAllState();
        });
        recipientSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int count, int after) { }
            public void onTextChanged(CharSequence s, int st, int before, int count) { renderRecipients(recipientList); }
            public void afterTextChanged(Editable e) { }
        });
        roomFilter.setOnItemSelectedListener(new SimpleSelectListener(() -> renderRecipients(recipientList)));
        selectAll.setOnClickListener(v -> {
            for (AnnouncementRecipient recipient : visibleRecipients) if (selectAll.isChecked()) selectedIds.add(recipient.userId); else selectedIds.remove(recipient.userId);
            renderRecipients(recipientList);
        });
        renderRecipients(recipientList);
        formDialog = new AlertDialog.Builder(requireContext()).setTitle(old == null ? "Tạo thông báo" : "Sửa thông báo")
                .setView(view).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        formDialog.setOnShowListener(x -> formDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> saveAnnouncement(old, content, type, send)));
        formDialog.show();
    }

    private void setupRoomFilter() {
        List<String> labels = new ArrayList<>(); roomIds = new ArrayList<>(); labels.add("Tất cả phòng"); roomIds.add(null);
        for (Room room : rooms) { labels.add(roomLabel(room)); roomIds.add(room.room_id); }
        roomFilter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
    }

    private void renderRecipients(ListView list) {
        UUID roomId = roomFilter == null || roomFilter.getSelectedItemPosition() < 0 ? null : roomIds.get(roomFilter.getSelectedItemPosition());
        visibleRecipients = AnnouncementRecipient.filter(recipients, recipientSearch == null ? "" : recipientSearch.getText().toString(), roomId);
        List<String> labels = new ArrayList<>();
        for (AnnouncementRecipient recipient : visibleRecipients) labels.add(recipient.name + "\n" + recipient.roomText());
        recipientAdapter.clear(); recipientAdapter.addAll(labels); recipientAdapter.notifyDataSetChanged();
        for (int i = 0; i < visibleRecipients.size(); i++) list.setItemChecked(i, selectedIds.contains(visibleRecipients.get(i).userId));
        if (recipientCount != null) recipientCount.setText("Đang hiển thị " + visibleRecipients.size() + " người nhận; đã chọn " + selectedIds.size());
        updateSelectAllState();
    }

    private void updateSelectAllState() {
        boolean allSelected = !visibleRecipients.isEmpty();
        for (AnnouncementRecipient recipient : visibleRecipients) if (!selectedIds.contains(recipient.userId)) allSelected = false;
        if (selectAll != null) { selectAll.setOnCheckedChangeListener(null); selectAll.setChecked(allSelected); selectAll.setOnCheckedChangeListener((button, checked) -> { }); }
    }

    private void saveAnnouncement(Announcement old, EditText content, EditText type, CheckBox send) {
        if (content.getText().toString().trim().isEmpty() || selectedIds.isEmpty()) { error("Cần nhập nội dung và chọn người nhận"); return; }
        AnnouncementRequest request = new AnnouncementRequest(content.getText().toString().trim(), type.getText().toString().trim(), send.isChecked(), new ArrayList<>(selectedIds));
        Call<Announcement> call = old == null ? ApiClient.api.createAnnouncement(request) : ApiClient.api.updateAnnouncement(old.announcement_id.toString(), request);
        call.enqueue(new Callback<Announcement>() {
            public void onResponse(Call<Announcement> c, Response<Announcement> r) { if (r.isSuccessful()) { formDialog.dismiss(); error("Đã lưu thông báo"); load(); } else error("Không thể lưu thông báo"); }
            public void onFailure(Call<Announcement> c, Throwable t) { error("Không thể kết nối đến máy chủ"); }
        });
    }

    private String roomLabel(Room room) { return room.room_name == null || room.room_name.trim().isEmpty() ? room.room_code : room.room_name + " (" + room.room_code + ")"; }
    private void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
    private static class SimpleSelectListener implements android.widget.AdapterView.OnItemSelectedListener { private final Runnable action; SimpleSelectListener(Runnable action) { this.action = action; } public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { action.run(); } public void onNothingSelected(android.widget.AdapterView<?> p) { } }
}
