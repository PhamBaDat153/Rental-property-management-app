package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class ContractDisplay {

    private static final String UNKNOWN_ROOM = "Chưa xác định phòng";

    private ContractDisplay() {
        // Prevent instantiation.
    }

    // -------------------------------------------------------------------------
    // Room display
    // -------------------------------------------------------------------------

    /**
     * Lấy mã phòng tương ứng với hợp đồng.
     *
     * @return Mã phòng hoặc thông báo khi chưa xác định được phòng.
     */
    static String roomCode(
            RentalContract contract,
            List<Room> rooms
    ) {
        if (contract == null || contract.room_id == null) {
            return UNKNOWN_ROOM;
        }

        for (Room room : safeRooms(rooms)) {
            if (room == null || room.room_id == null) {
                continue;
            }

            if (!contract.room_id.equals(room.room_id)) {
                continue;
            }

            return getRoomCode(room);
        }

        return UNKNOWN_ROOM;
    }

    private static String getRoomCode(Room room) {
        String code = room.room_code;

        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN_ROOM;
        }

        return code;
    }

    // -------------------------------------------------------------------------
    // Room search
    // -------------------------------------------------------------------------

    /**
     * Kiểm tra mã phòng có khớp với từ khóa tìm kiếm hay không.
     *
     * Từ khóa rỗng sẽ khớp với mọi hợp đồng.
     */
    static boolean matchesRoomCode(
            RentalContract contract,
            List<Room> rooms,
            String query
    ) {
        String normalizedQuery = normalize(query);

        if (normalizedQuery.isEmpty()) {
            return true;
        }

        for (Room room : safeRooms(rooms)) {
            if (room == null || room.room_id == null
                    || contract == null || !room.room_id.equals(contract.room_id)) {
                continue;
            }
            return contains(room.room_code, normalizedQuery)
                    || contains(room.room_name, normalizedQuery);
        }

        return false;
    }

    private static boolean contains(String value, String query) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private static List<Room> safeRooms(List<Room> rooms) {
        return rooms == null
                ? Collections.emptyList()
                : rooms;
    }
}