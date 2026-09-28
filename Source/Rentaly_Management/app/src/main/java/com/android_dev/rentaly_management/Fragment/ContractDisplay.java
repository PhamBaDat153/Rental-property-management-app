package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;

import java.util.List;
import java.util.Locale;

final class ContractDisplay {
    private ContractDisplay() { }

    static String roomCode(RentalContract contract, List<Room> rooms) {
        if (contract == null || contract.room_id == null) return "Chưa xác định phòng";
        for (Room room : rooms) {
            if (contract.room_id.equals(room.room_id)) {
                return room.room_code == null || room.room_code.trim().isEmpty()
                        ? "Chưa xác định phòng" : room.room_code;
            }
        }
        return "Chưa xác định phòng";
    }

    static boolean matchesRoomCode(RentalContract contract, List<Room> rooms, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() || roomCode(contract, rooms).toLowerCase(Locale.ROOT).contains(normalized)
                && !roomCode(contract, rooms).equals("Chưa xác định phòng");
    }
}
