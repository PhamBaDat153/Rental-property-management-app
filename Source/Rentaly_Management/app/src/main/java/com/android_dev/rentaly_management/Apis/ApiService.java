package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.Announcement;
import com.android_dev.rentaly_management.DTO.AnnouncementRequest;
import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.ContractTenantRequest;
import com.android_dev.rentaly_management.DTO.Invoice;
import com.android_dev.rentaly_management.DTO.InvoiceRequest;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.LoginRequest;
import com.android_dev.rentaly_management.DTO.LoginResponse;
import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.DTO.MaintenanceRequest;
import com.android_dev.rentaly_management.DTO.Meter;
import com.android_dev.rentaly_management.DTO.MeterReading;
import com.android_dev.rentaly_management.DTO.MeterReadingRequest;
import com.android_dev.rentaly_management.DTO.MeterRequest;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.RoomService;
import com.android_dev.rentaly_management.DTO.Service;
import com.android_dev.rentaly_management.DTO.ServiceRequest;
import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserUpdateRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    @POST("be/user/login")
    Call<LoginResponse> login(
            @Body LoginRequest request
    );

    // -------------------------------------------------------------------------
    // User management
    // -------------------------------------------------------------------------

    @GET("be/user/manage")
    Call<List<UserTenant>> managedUsers(
            @Query("search") String search
    );

    @GET("be/user/manage/{id}")
    Call<UserTenant> managedUser(
            @Path("id") String id
    );

    @POST("be/user/manage")
    Call<UserTenant> createManagedUser(
            @Body User request
    );

    @DELETE("be/user/manage/{id}")
    Call<Void> deleteManagedUser(
            @Path("id") String id
    );

    @PUT("be/user/manage/{id}/role")
    Call<UserTenant> updateManagedUserRole(
            @Path("id") String id,
            @Body UserRoleUpdateRequest request
    );

    @PUT("be/user/manage/{id}")
    Call<UserTenant> updateManagedUser(
            @Path("id") String id,
            @Body UserUpdateRequest request
    );

    // -------------------------------------------------------------------------
    // Announcements
    // -------------------------------------------------------------------------

    @GET("be/announcements")
    Call<List<Announcement>> announcements();

    @POST("be/announcements")
    Call<Announcement> createAnnouncement(
            @Body AnnouncementRequest request
    );

    @PUT("be/announcements/{id}")
    Call<Announcement> updateAnnouncement(
            @Path("id") String id,
            @Body AnnouncementRequest request
    );

    @DELETE("be/announcements/{id}")
    Call<Void> deleteAnnouncement(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Locations
    // -------------------------------------------------------------------------

    @GET("be/locations")
    Call<List<Location>> locations();

    @GET("be/locations/{id}")
    Call<Location> location(
            @Path("id") String id
    );

    @POST("be/locations")
    Call<Location> createLocation(
            @Body LocationRequest request
    );

    @PUT("be/locations/{id}")
    Call<Location> updateLocation(
            @Path("id") String id,
            @Body LocationRequest request
    );

    @DELETE("be/locations/{id}")
    Call<Void> deleteLocation(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Rooms
    // -------------------------------------------------------------------------

    @GET("be/rooms")
    Call<List<Room>> rooms();

    @GET("be/rooms/{id}")
    Call<Room> room(
            @Path("id") String id
    );

    @Multipart
    @POST("be/rooms")
    Call<Room> createRoom(
            @Part("room") RequestBody room,
            @Part List<MultipartBody.Part> images
    );

    @Multipart
    @PUT("be/rooms/{id}")
    Call<Room> updateRoom(
            @Path("id") String id,
            @Part("room") RequestBody room,
            @Part List<MultipartBody.Part> images
    );

    @DELETE("be/rooms/{id}")
    Call<Void> deleteRoom(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Meters
    // -------------------------------------------------------------------------

    @GET("be/meters")
    Call<List<Meter>> meters(
            @Query("roomId") String roomId
    );

    @GET("be/meters/{id}")
    Call<Meter> meter(
            @Path("id") String id
    );

    @POST("be/meters")
    Call<Meter> createMeter(
            @Body MeterRequest request
    );

    @PUT("be/meters/{id}")
    Call<Meter> updateMeter(
            @Path("id") String id,
            @Body MeterRequest request
    );

    @DELETE("be/meters/{id}")
    Call<Void> deleteMeter(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Meter readings
    // -------------------------------------------------------------------------

    @GET("be/meters/{meterId}/readings")
    Call<List<MeterReading>> meterReadings(
            @Path("meterId") String meterId
    );

    @POST("be/meter-readings")
    Call<MeterReading> createMeterReading(
            @Body MeterReadingRequest request
    );

    @Multipart
    @POST("be/meter-readings")
    Call<MeterReading> createMeterReading(
            @Part("reading") RequestBody reading,
            @Part MultipartBody.Part evidence
    );

    @PUT("be/meter-readings/{id}")
    Call<MeterReading> updateMeterReading(
            @Path("id") String id,
            @Body MeterReadingRequest request
    );

    @Multipart
    @PUT("be/meter-readings/{id}")
    Call<MeterReading> updateMeterReading(
            @Path("id") String id,
            @Part("reading") RequestBody reading,
            @Part MultipartBody.Part evidence
    );

    @DELETE("be/meter-readings/{id}")
    Call<Void> deleteMeterReading(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Service catalog
    // -------------------------------------------------------------------------

    @GET("be/services")
    Call<List<Service>> services();

    @POST("be/services")
    Call<Service> createService(
            @Body ServiceRequest request
    );

    @PUT("be/services/{id}")
    Call<Service> updateService(
            @Path("id") String id,
            @Body ServiceRequest request
    );

    @DELETE("be/services/{id}")
    Call<Void> deleteService(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Services assigned to rooms
    // -------------------------------------------------------------------------

    @GET("be/rooms/{roomId}/services")
    Call<List<RoomService>> roomServices(
            @Path("roomId") String roomId
    );

    @POST("be/rooms/{roomId}/services/{serviceId}")
    Call<RoomService> assignRoomService(
            @Path("roomId") String roomId,
            @Path("serviceId") String serviceId
    );

    @PUT("be/rooms/{roomId}/services/{serviceId}")
    Call<RoomService> updateRoomService(
            @Path("roomId") String roomId,
            @Path("serviceId") String serviceId,
            @Body Map<String, Boolean> request
    );

    @DELETE("be/rooms/{roomId}/services/{serviceId}")
    Call<Void> deleteRoomService(
            @Path("roomId") String roomId,
            @Path("serviceId") String serviceId
    );

    // -------------------------------------------------------------------------
    // Rental contracts
    // -------------------------------------------------------------------------

    @GET("be/contracts")
    Call<List<RentalContract>> contracts();

    @GET("be/contracts/{id}")
    Call<RentalContract> contract(
            @Path("id") String id
    );

    @Multipart
    @POST("be/contracts")
    Call<RentalContract> createContract(
            @Part("contract") RequestBody contract,
            @Part MultipartBody.Part document
    );

    @Multipart
    @PUT("be/contracts/{id}")
    Call<RentalContract> updateContract(
            @Path("id") String id,
            @Part("contract") RequestBody contract,
            @Part MultipartBody.Part document
    );

    @DELETE("be/contracts/{id}")
    Call<Void> deleteContract(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Contract tenants
    // -------------------------------------------------------------------------

    @GET("be/contract-tenants")
    Call<List<ContractTenant>> contractTenants();

    @GET("be/contract-tenants/{contractId}/{tenantId}")
    Call<ContractTenant> contractTenant(
            @Path("contractId") String contractId,
            @Path("tenantId") String tenantId
    );

    @POST("be/contract-tenants")
    Call<ContractTenant> createContractTenant(
            @Body ContractTenantRequest request
    );

    @PUT("be/contract-tenants/{contractId}/{tenantId}")
    Call<ContractTenant> updateContractTenant(
            @Path("contractId") String contractId,
            @Path("tenantId") String tenantId,
            @Body ContractTenantRequest request
    );

    @DELETE("be/contract-tenants/{contractId}/{tenantId}")
    Call<Void> deleteContractTenant(
            @Path("contractId") String contractId,
            @Path("tenantId") String tenantId
    );

    // -------------------------------------------------------------------------
    // Invoices
    // -------------------------------------------------------------------------

    @GET("be/invoices")
    Call<List<Invoice>> invoices(
            @Query("contractId") String contractId,
            @Query("status") String status
    );

    @POST("be/invoices")
    Call<Invoice> createInvoice(
            @Body InvoiceRequest request
    );

    @PUT("be/invoices/{id}")
    Call<Invoice> updateInvoice(
            @Path("id") String id,
            @Body InvoiceRequest request
    );

    @DELETE("be/invoices/{id}")
    Call<Void> deleteInvoice(
            @Path("id") String id
    );

    // -------------------------------------------------------------------------
    // Maintenance requests
    // -------------------------------------------------------------------------

    @GET("be/maintenance")
    Call<List<Maintenance>> maintenance(
            @Query("roomId") String roomId,
            @Query("status") String status,
            @Query("priority") String priority
    );

    @POST("be/maintenance")
    Call<Maintenance> createMaintenance(
            @Body MaintenanceRequest request
    );

    @PUT("be/maintenance/{id}")
    Call<Maintenance> updateMaintenance(
            @Path("id") String id,
            @Body MaintenanceRequest request
    );

    @DELETE("be/maintenance/{id}")
    Call<Void> deleteMaintenance(
            @Path("id") String id
    );
}