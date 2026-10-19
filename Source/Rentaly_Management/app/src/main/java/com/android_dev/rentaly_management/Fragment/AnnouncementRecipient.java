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

    private static final String UNKNOWN_ROOM = "Chưa xác định phòng";
    private static final String ROOM_PREFIX = "Phòng: ";

    final UUID userId;
    final String name;

    final Set<UUID> roomIds = new LinkedHashSet<>();
    final List<String> roomLabels = new ArrayList<>();

    AnnouncementRecipient(UUID userId, String name) {
        this.userId = userId;
        this.name = name == null ? "" : name;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    String roomText() {
        if (roomLabels.isEmpty()) {
            return UNKNOWN_ROOM;
        }

        return ROOM_PREFIX + String.join(", ", roomLabels);
    }

    // -------------------------------------------------------------------------
    // Search and filtering
    // -------------------------------------------------------------------------

    boolean matches(String query, UUID roomId) {
        boolean matchesName = matchesName(query);
        boolean matchesRoom = roomId == null || roomIds.contains(roomId);

        return matchesName && matchesRoom;
    }

    private boolean matchesName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        String normalizedName = name.toLowerCase(Locale.ROOT);

        return normalizedName.contains(normalizedQuery);
    }

    static List<AnnouncementRecipient> filter(
            List<AnnouncementRecipient> recipients,
            String query,
            UUID roomId
    ) {
        List<AnnouncementRecipient> filteredRecipients = new ArrayList<>();

        for (AnnouncementRecipient recipient : safeList(recipients)) {
            if (recipient != null && recipient.matches(query, roomId)) {
                filteredRecipients.add(recipient);
            }
        }

        return filteredRecipients;
    }

    // -------------------------------------------------------------------------
    // Build recipient data
    // -------------------------------------------------------------------------

    static List<AnnouncementRecipient> build(
            List<UserTenant> users,
            List<Room> rooms,
            List<RentalContract> contracts,
            List<ContractTenant> assignments
    ) {
        Map<UUID, String> roomLabels = buildRoomLabels(rooms);
        Map<UUID, UUID> contractRooms = buildContractRooms(contracts);

        Map<UUID, AnnouncementRecipient> recipientsByUserId =
                buildRecipientsByUserId(users);

        Map<UUID, UserTenant> usersByTenantId =
                buildUsersByTenantId(users);

        assignRoomsToRecipients(
                assignments,
                contractRooms,
                roomLabels,
                usersByTenantId,
                recipientsByUserId
        );

        sortRoomLabels(recipientsByUserId);

        return new ArrayList<>(recipientsByUserId.values());
    }

    // -------------------------------------------------------------------------
    // Room mapping
    // -------------------------------------------------------------------------

    private static Map<UUID, String> buildRoomLabels(List<Room> rooms) {
        Map<UUID, String> roomLabels = new HashMap<>();

        for (Room room : safeList(rooms)) {
            if (room == null || room.room_id == null) {
                continue;
            }

            roomLabels.put(room.room_id, createRoomLabel(room));
        }

        return roomLabels;
    }

    private static Map<UUID, UUID> buildContractRooms(
            List<RentalContract> contracts
    ) {
        Map<UUID, UUID> contractRooms = new HashMap<>();

        for (RentalContract contract : safeList(contracts)) {
            if (contract == null || contract.contract_id == null) {
                continue;
            }

            contractRooms.put(
                    contract.contract_id,
                    contract.room_id
            );
        }

        return contractRooms;
    }

    private static String createRoomLabel(Room room) {
        String roomCode = room.room_code == null
                ? ""
                : room.room_code.trim();

        String roomName = room.room_name == null
                ? ""
                : room.room_name.trim();

        if (roomName.isEmpty()) {
            return roomCode.isEmpty() ? UNKNOWN_ROOM : roomCode;
        }

        if (roomCode.isEmpty()) {
            return roomName;
        }

        return roomName + " (" + roomCode + ")";
    }

    // -------------------------------------------------------------------------
    // Recipient mapping
    // -------------------------------------------------------------------------

    private static Map<UUID, AnnouncementRecipient> buildRecipientsByUserId(
            List<UserTenant> users
    ) {
        Map<UUID, AnnouncementRecipient> recipients = new LinkedHashMap<>();

        for (UserTenant user : safeList(users)) {
            if (user == null || user.getUser_id() == null) {
                continue;
            }

            UUID userId = user.getUser_id();

            recipients.put(
                    userId,
                    new AnnouncementRecipient(
                            userId,
                            user.displayName()
                    )
            );
        }

        return recipients;
    }

    private static Map<UUID, UserTenant> buildUsersByTenantId(
            List<UserTenant> users
    ) {
        Map<UUID, UserTenant> usersByTenantId = new HashMap<>();

        for (UserTenant user : safeList(users)) {
            if (user == null || user.getTenant_id() == null) {
                continue;
            }

            usersByTenantId.put(user.getTenant_id(), user);
        }

        return usersByTenantId;
    }

    // -------------------------------------------------------------------------
    // Assign rooms to recipients
    // -------------------------------------------------------------------------

    private static void assignRoomsToRecipients(
            List<ContractTenant> assignments,
            Map<UUID, UUID> contractRooms,
            Map<UUID, String> roomLabels,
            Map<UUID, UserTenant> usersByTenantId,
            Map<UUID, AnnouncementRecipient> recipientsByUserId
    ) {
        for (ContractTenant assignment : safeList(assignments)) {
            if (assignment == null) {
                continue;
            }

            UserTenant user = usersByTenantId.get(assignment.tenant_id);

            if (user == null || user.getUser_id() == null) {
                continue;
            }

            AnnouncementRecipient recipient =
                    recipientsByUserId.get(user.getUser_id());

            UUID roomId = contractRooms.get(assignment.contract_id);

            if (recipient == null
                    || roomId == null
                    || !roomLabels.containsKey(roomId)) {
                continue;
            }

            addRoomToRecipient(
                    recipient,
                    roomId,
                    roomLabels.get(roomId)
            );
        }
    }

    private static void addRoomToRecipient(
            AnnouncementRecipient recipient,
            UUID roomId,
            String roomLabel
    ) {
        recipient.roomIds.add(roomId);

        if (!recipient.roomLabels.contains(roomLabel)) {
            recipient.roomLabels.add(roomLabel);
        }
    }

    private static void sortRoomLabels(
            Map<UUID, AnnouncementRecipient> recipients
    ) {
        for (AnnouncementRecipient recipient : recipients.values()) {
            recipient.roomLabels.sort(Comparator.naturalOrder());
        }
    }

    // -------------------------------------------------------------------------
    // Shared utilities
    // -------------------------------------------------------------------------

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}