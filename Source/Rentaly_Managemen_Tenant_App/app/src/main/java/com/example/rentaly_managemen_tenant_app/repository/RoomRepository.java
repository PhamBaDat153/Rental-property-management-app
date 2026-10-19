package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.Location;
import com.example.rentaly_managemen_tenant_app.models.Room;
import com.example.rentaly_managemen_tenant_app.models.RoomService;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.List;

public class RoomRepository {
    public void getRoom(String roomId, ApiCallback<Room> callback) {
        ApiRequests.enqueue(ApiClient.api.getRoomById(roomId), callback);
    }

    public void getLocation(String locationId, ApiCallback<Location> callback) {
        ApiRequests.enqueue(ApiClient.api.getLocationById(locationId), callback);
    }

    public void getRoomServices(String roomId, ApiCallback<List<RoomService>> callback) {
        ApiRequests.enqueue(ApiClient.api.getRoomServices(roomId), callback);
    }
}
