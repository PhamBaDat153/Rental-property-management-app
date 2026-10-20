package com.example.rentaly_managemen_tenant_app.network;


import com.example.rentaly_managemen_tenant_app.models.LoginRequest;
import com.example.rentaly_managemen_tenant_app.models.LoginResponse;
import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.models.Room;
import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.models.ContractTenant;
import com.example.rentaly_managemen_tenant_app.models.Meter;
import com.example.rentaly_managemen_tenant_app.models.MeterReading;
import com.example.rentaly_managemen_tenant_app.models.Invoice;
import com.example.rentaly_managemen_tenant_app.models.Maintenance;
import com.example.rentaly_managemen_tenant_app.models.MaintenanceRequest;
import com.example.rentaly_managemen_tenant_app.models.Announcement;
import com.example.rentaly_managemen_tenant_app.models.Service;
import com.example.rentaly_managemen_tenant_app.models.RoomService;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTH ====================

    @POST("be/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("be/user/manage/{id}")
    Call<UserTenant> getUserById(@Path("id") String id);

    @PUT("be/user/manage/{id}/profile")
    Call<UserTenant> updateUserProfile(@Path("id") String id, @Body com.example.rentaly_managemen_tenant_app.models.UserProfileUpdateRequest request);


    // ==================== ROOM ====================

    @GET("be/rooms/{id}")
    Call<Room> getRoomById(@Path("id") String id);

    @GET("be/locations/{id}")
    Call<com.example.rentaly_managemen_tenant_app.models.Location> getLocationById(@Path("id") String id);

    @GET("be/rooms/{roomId}/services")
    Call<List<RoomService>> getRoomServices(
            @Path("roomId") String roomId
    );


    // ==================== CONTRACT ====================

    @GET("be/contracts/{id}")
    Call<RentalContract> getContractById(
            @Path("id") String id
    );

    @GET("be/contract-tenants")
    Call<List<ContractTenant>> getContractTenants();

    @GET("be/contract-tenants/{contractId}/{tenantId}")
    Call<ContractTenant> getContractTenant(
            @Path("contractId") String contractId,
            @Path("tenantId") String tenantId
    );


    // ==================== ELECTRICITY / WATER ====================

    @GET("be/meters")
    Call<List<Meter>> getMeters(
            @Query("roomId") String roomId
    );

    @GET("be/meters/{meterId}/readings")
    Call<List<MeterReading>> getMeterReadings(
            @Path("meterId") String meterId
    );


    // ==================== INVOICE ====================

    @GET("be/invoices")
    Call<List<Invoice>> getInvoices(
            @Query("contractId") String contractId,
            @Query("status") String status
    );

    @GET("be/invoices/{id}")
    Call<Invoice> getInvoiceById(
            @Path("id") String id
    );


    // ==================== MAINTENANCE ====================

    @GET("be/maintenance")
    Call<List<Maintenance>> getMaintenance(
            @Query("roomId") String roomId,
            @Query("status") String status,
            @Query("priority") String priority
    );

    @POST("be/maintenance")
    Call<Maintenance> createMaintenance(
            @Body MaintenanceRequest request
    );


    // ==================== ANNOUNCEMENT ====================

    @GET("be/announcements")
    Call<List<Announcement>> getAnnouncements();
}
