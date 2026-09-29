package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.UserTenant;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnouncementRecipientTest {
    @Test public void aggregatesRoomsAndFiltersByNameAndRoom() {
        UUID userId = UUID.randomUUID(), tenantId = UUID.randomUUID(), c1 = UUID.randomUUID(), c2 = UUID.randomUUID();
        UUID r1 = UUID.randomUUID(), r2 = UUID.randomUUID();
        UserTenant user = user(userId, tenantId, "Nguyen An");
        Room room1 = room(r1, "P101"), room2 = room(r2, "P202");
        RentalContract contract1 = contract(c1, r1), contract2 = contract(c2, r2);
        ContractTenant a1 = assignment(c1, tenantId), a2 = assignment(c2, tenantId);

        List<AnnouncementRecipient> all = AnnouncementRecipient.build(Arrays.asList(user), Arrays.asList(room1, room2), Arrays.asList(contract1, contract2), Arrays.asList(a1, a2));

        assertEquals(1, all.size());
        assertEquals(2, all.get(0).roomLabels.size());
        assertEquals(1, AnnouncementRecipient.filter(all, "an", r2).size());
        assertEquals(0, AnnouncementRecipient.filter(all, "binh", r2).size());
    }

    @Test public void keepsUsersWithUnresolvedRooms() {
        UUID userId = UUID.randomUUID(), tenantId = UUID.randomUUID();
        List<AnnouncementRecipient> all = AnnouncementRecipient.build(Arrays.asList(user(userId, tenantId, "An")), null, null, null);
        assertEquals(1, all.size());
        assertTrue(all.get(0).roomText().contains("Chưa xác định phòng"));
    }

    private static UserTenant user(UUID userId, UUID tenantId, String name) {
        UserTenant user = new UserTenant();
        try { java.lang.reflect.Field f = UserTenant.class.getDeclaredField("user_id"); f.setAccessible(true); f.set(user, userId); f = UserTenant.class.getDeclaredField("tenant_id"); f.setAccessible(true); f.set(user, tenantId); f = UserTenant.class.getDeclaredField("full_name"); f.setAccessible(true); f.set(user, name); } catch (Exception e) { throw new AssertionError(e); }
        return user;
    }
    private static Room room(UUID id, String code) { Room room = new Room(); room.room_id = id; room.room_code = code; return room; }
    private static RentalContract contract(UUID id, UUID roomId) { RentalContract c = new RentalContract(); c.contract_id = id; c.room_id = roomId; return c; }
    private static ContractTenant assignment(UUID contractId, UUID tenantId) { ContractTenant a = new ContractTenant(); a.contract_id = contractId; a.tenant_id = tenantId; return a; }
}
