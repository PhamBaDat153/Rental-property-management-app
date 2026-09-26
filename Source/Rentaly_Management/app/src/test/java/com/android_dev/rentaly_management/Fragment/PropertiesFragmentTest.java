package com.android_dev.rentaly_management.Fragment;

import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.Room;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class PropertiesFragmentTest {
    @Test public void locationFilterMatchesCodeAndAddressIgnoringCase() {
        Location location = new Location();
        location.location_code = "LOC-01";
        location.address_line = "Nguyen Trai";
        location.province_name = "Ho Chi Minh";
        assertTrue(PropertiesFragment.locationMatches(location, "loc-01"));
        assertTrue(PropertiesFragment.locationMatches(location, "nguyen"));
        assertTrue(PropertiesFragment.locationMatches(location, "chi minh"));
    }

    @Test public void roomFilterUsesOnlyRoomNameAndKeepsUnnamedSeparate() {
        Room room = new Room();
        room.room_name = "Phòng ban công";
        room.room_code = "A101";
        assertTrue(PropertiesFragment.roomMatches(room, "ban công"));
        assertFalse(PropertiesFragment.roomMatches(room, "a101"));
        room.room_name = null;
        assertFalse(PropertiesFragment.roomMatches(room, "a101"));
        assertEquals("Chưa đặt tên phòng", PropertiesFragment.roomName(room));
    }
}
