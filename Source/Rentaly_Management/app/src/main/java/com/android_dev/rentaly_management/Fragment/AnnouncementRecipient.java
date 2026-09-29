package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.UserTenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class AnnouncementRecipient {
    final UUID userId;
    final String name;
    final Set<UUID> roomIds = new LinkedHashSet<>();
    final List<String> roomLabels = new ArrayList<>();

    AnnouncementRecipient(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    String roomText() {
        return roomLabels.isEmpty() ? "Chưa xác định phòng" : "Phòng: " + String.join(", ", roomLabels);
    }

    boolean matches(String query, UUID roomId) {
        return (query == null || query.trim().isEmpty() || name.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT)))
                && (roomId == null || roomIds.contains(roomId));
    }

    static List<AnnouncementRecipient> build(List<UserTenant> users, List<Room> rooms,
                                             List<RentalContract> contracts, List<ContractTenant> assignments) {
        Map<UUID, String> roomLabels = new HashMap<>();
        for (Room room : rooms == null ? Collections.<Room>emptyList() : rooms) {
            if (room.room_id != null) roomLabels.put(room.room_id, label(room));
        }
        Map<UUID, UUID> contractRooms = new HashMap<>();
        for (RentalContract contract : contracts == null ? Collections.<RentalContract>emptyList() : contracts) {
            if (contract.contract_id != null) contractRooms.put(contract.contract_id, contract.room_id);
        }
        Map<UUID, AnnouncementRecipient> result = new LinkedHashMap<>();
        for (UserTenant user : users == null ? Collections.<UserTenant>emptyList() : users) {
            if (user.getUser_id() != null) result.put(user.getUser_id(), new AnnouncementRecipient(user.getUser_id(), user.displayName()));
        }
        Map<UUID, UserTenant> usersByTenant = new HashMap<>();
        for (UserTenant user : users == null ? Collections.<UserTenant>emptyList() : users) {
            if (user.getTenant_id() != null) usersByTenant.put(user.getTenant_id(), user);
        }
        for (ContractTenant assignment : assignments == null ? Collections.<ContractTenant>emptyList() : assignments) {
            UserTenant user = usersByTenant.get(assignment.tenant_id);
            UUID roomId = contractRooms.get(assignment.contract_id);
            AnnouncementRecipient recipient = user == null ? null : result.get(user.getUser_id());
            if (recipient != null && roomId != null && roomLabels.containsKey(roomId)) {
                recipient.roomIds.add(roomId);
                if (!recipient.roomLabels.contains(roomLabels.get(roomId))) recipient.roomLabels.add(roomLabels.get(roomId));
            }
        }
        for (AnnouncementRecipient recipient : result.values()) recipient.roomLabels.sort(Comparator.naturalOrder());
        return new ArrayList<>(result.values());
    }

    static List<AnnouncementRecipient> filter(List<AnnouncementRecipient> all, String query, UUID roomId) {
        List<AnnouncementRecipient> result = new ArrayList<>();
        for (AnnouncementRecipient recipient : all) if (recipient.matches(query, roomId)) result.add(recipient);
        return result;
    }

    private static String label(Room room) {
        String code = room.room_code == null ? "" : room.room_code.trim();
        String name = room.room_name == null ? "" : room.room_name.trim();
        if (name.isEmpty()) return code.isEmpty() ? "Chưa xác định phòng" : code;
        return code.isEmpty() ? name : name + " (" + code + ")";
    }
}
