package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.android_dev.rentaly_management.DTO.RentalContractRequest;
import com.android_dev.rentaly_management.Apis.ApiClient;

import static org.junit.Assert.*;

public class ContractDisplayTest {
    @Test public void filtersByRelatedRoomCodeCaseInsensitively() {
        UUID roomId = UUID.randomUUID();
        RentalContract contract = new RentalContract(); contract.room_id = roomId;
        Room room = new Room(); room.room_id = roomId; room.room_code = "P-101";
        assertTrue(ContractDisplay.matchesRoomCode(contract, Collections.singletonList(room), "p-10"));
        assertFalse(ContractDisplay.matchesRoomCode(contract, Collections.singletonList(room), "P-202"));
    }

    @Test public void unresolvedRoomDoesNotMatchNonEmptyQuery() {
        RentalContract contract = new RentalContract(); contract.room_id = UUID.randomUUID();
        assertEquals("Chưa xác định phòng", ContractDisplay.roomCode(contract, Collections.emptyList()));
        assertFalse(ContractDisplay.matchesRoomCode(contract, Collections.emptyList(), "P-101"));
        assertTrue(ContractDisplay.matchesRoomCode(contract, Collections.emptyList(), ""));
    }

    @Test public void rejectsInvalidContractValues() {
        assertFalse(ContractValidation.valid(LocalDate.now().plusDays(1), LocalDate.now(),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, 0));
        assertFalse(ContractValidation.valid(LocalDate.now().minusDays(1), null,
                BigDecimal.ZERO, BigDecimal.ZERO, 1, 0));
        assertFalse(ContractValidation.valid(LocalDate.now(), null,
                BigDecimal.valueOf(-1), BigDecimal.ZERO, 1, 0));
        assertTrue(ContractValidation.valid(LocalDate.now(), null,
                BigDecimal.ZERO, BigDecimal.ZERO, 31, 0));
    }

    @Test public void rejectsInvalidLifecycleDates() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        assertFalse(ContractValidation.validLifecycleDates(
                start, end, start.plusDays(1), null, true));
        assertFalse(ContractValidation.validLifecycleDates(
                start, end, null, end.plusDays(1), false));
        assertTrue(ContractValidation.validLifecycleDates(
                start, end, start.minusDays(1), null, true));
    }

    @Test public void serializesRequestWithoutDocumentField() {
        RentalContractRequest request = new RentalContractRequest(UUID.randomUUID(), LocalDate.of(2026, 2, 1),
                null, null, BigDecimal.TEN, BigDecimal.ZERO, null, 0, "INACTIVE", null, null, null);
        String json = ApiClient.gson.toJson(request);
        assertTrue(json.contains("room_id"));
        assertFalse(json.contains("document_url"));
    }
}
