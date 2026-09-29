package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;
import com.android_dev.rentaly_management.DTO.UserUpdateRequest;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.RentalContract;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.Query;
import retrofit2.http.PUT;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.RequestBody;

public interface ApiService {
    @POST("be/user/login")
    Call<com.android_dev.rentaly_management.DTO.LoginResponse> login(@Body com.android_dev.rentaly_management.DTO.LoginRequest request);

    @GET("be/user/manage")
    Call<java.util.List<UserTenant>> managedUsers(@Query("search") String search);

    @GET("be/user/manage/{id}")
    Call<UserTenant> managedUser(@Path("id") String id);

    @POST("be/user/manage")
    Call<UserTenant> createManagedUser(@Body User request);

    @DELETE("be/user/manage/{id}")
    Call<Void> deleteManagedUser(@Path("id") String id);

    @PUT("be/user/manage/{id}/role")
    Call<UserTenant> updateManagedUserRole(@Path("id") String id, @Body UserRoleUpdateRequest request);

    @PUT("be/user/manage/{id}")
    Call<UserTenant> updateManagedUser(@Path("id") String id, @Body UserUpdateRequest request);
    @GET("be/announcements") Call<java.util.List<com.android_dev.rentaly_management.DTO.Announcement>> announcements();
    @POST("be/announcements") Call<com.android_dev.rentaly_management.DTO.Announcement> createAnnouncement(@Body com.android_dev.rentaly_management.DTO.AnnouncementRequest request);
    @PUT("be/announcements/{id}") Call<com.android_dev.rentaly_management.DTO.Announcement> updateAnnouncement(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.AnnouncementRequest request);
    @DELETE("be/announcements/{id}") Call<Void> deleteAnnouncement(@Path("id") String id);

    @GET("be/locations") Call<java.util.List<Location>> locations();
    @GET("be/locations/{id}") Call<Location> location(@Path("id") String id);
    @POST("be/locations") Call<Location> createLocation(@Body LocationRequest request);
    @PUT("be/locations/{id}") Call<Location> updateLocation(@Path("id") String id, @Body LocationRequest request);
    @DELETE("be/locations/{id}") Call<Void> deleteLocation(@Path("id") String id);

    @GET("be/rooms") Call<java.util.List<Room>> rooms();
    @GET("be/rooms/{id}") Call<Room> room(@Path("id") String id);
    @Multipart @POST("be/rooms") Call<Room> createRoom(@Part("room") RequestBody room,
                                                          @Part java.util.List<okhttp3.MultipartBody.Part> images);
    @Multipart @PUT("be/rooms/{id}") Call<Room> updateRoom(@Path("id") String id, @Part("room") RequestBody room,
                                                              @Part java.util.List<okhttp3.MultipartBody.Part> images);
    @DELETE("be/rooms/{id}") Call<Void> deleteRoom(@Path("id") String id);

    @GET("be/meters") Call<java.util.List<com.android_dev.rentaly_management.DTO.Meter>> meters(@Query("roomId") String roomId);
    @GET("be/meters/{id}") Call<com.android_dev.rentaly_management.DTO.Meter> meter(@Path("id") String id);
    @POST("be/meters") Call<com.android_dev.rentaly_management.DTO.Meter> createMeter(@Body com.android_dev.rentaly_management.DTO.MeterRequest request);
    @PUT("be/meters/{id}") Call<com.android_dev.rentaly_management.DTO.Meter> updateMeter(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.MeterRequest request);
    @DELETE("be/meters/{id}") Call<Void> deleteMeter(@Path("id") String id);
    @GET("be/meters/{meterId}/readings") Call<java.util.List<com.android_dev.rentaly_management.DTO.MeterReading>> meterReadings(@Path("meterId") String meterId);
    @POST("be/meter-readings") Call<com.android_dev.rentaly_management.DTO.MeterReading> createMeterReading(@Body com.android_dev.rentaly_management.DTO.MeterReadingRequest request);
    @Multipart @POST("be/meter-readings") Call<com.android_dev.rentaly_management.DTO.MeterReading> createMeterReading(@Part("reading") RequestBody reading, @Part okhttp3.MultipartBody.Part evidence);
    @PUT("be/meter-readings/{id}") Call<com.android_dev.rentaly_management.DTO.MeterReading> updateMeterReading(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.MeterReadingRequest request);
    @Multipart @PUT("be/meter-readings/{id}") Call<com.android_dev.rentaly_management.DTO.MeterReading> updateMeterReading(@Path("id") String id, @Part("reading") RequestBody reading, @Part okhttp3.MultipartBody.Part evidence);
    @DELETE("be/meter-readings/{id}") Call<Void> deleteMeterReading(@Path("id") String id);
    @GET("be/services") Call<java.util.List<com.android_dev.rentaly_management.DTO.Service>> services();
    @POST("be/services") Call<com.android_dev.rentaly_management.DTO.Service> createService(@Body com.android_dev.rentaly_management.DTO.ServiceRequest request);
    @PUT("be/services/{id}") Call<com.android_dev.rentaly_management.DTO.Service> updateService(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.ServiceRequest request);
    @DELETE("be/services/{id}") Call<Void> deleteService(@Path("id") String id);
    @GET("be/rooms/{roomId}/services") Call<java.util.List<com.android_dev.rentaly_management.DTO.RoomService>> roomServices(@Path("roomId") String roomId);
    @POST("be/rooms/{roomId}/services/{serviceId}") Call<com.android_dev.rentaly_management.DTO.RoomService> assignRoomService(@Path("roomId") String roomId, @Path("serviceId") String serviceId);
    @PUT("be/rooms/{roomId}/services/{serviceId}") Call<com.android_dev.rentaly_management.DTO.RoomService> updateRoomService(@Path("roomId") String roomId, @Path("serviceId") String serviceId, @Body java.util.Map<String, Boolean> request);
    @DELETE("be/rooms/{roomId}/services/{serviceId}") Call<Void> deleteRoomService(@Path("roomId") String roomId, @Path("serviceId") String serviceId);

    @GET("be/contracts") Call<java.util.List<RentalContract>> contracts();
    @GET("be/contracts/{id}") Call<RentalContract> contract(@Path("id") String id);
    @Multipart @POST("be/contracts") Call<RentalContract> createContract(@Part("contract") RequestBody contract,
                                                                            @Part okhttp3.MultipartBody.Part document);
    @Multipart @PUT("be/contracts/{id}") Call<RentalContract> updateContract(@Path("id") String id,
                                                                                @Part("contract") RequestBody contract,
                                                                                @Part okhttp3.MultipartBody.Part document);
    @DELETE("be/contracts/{id}") Call<Void> deleteContract(@Path("id") String id);

    @GET("be/contract-tenants") Call<java.util.List<com.android_dev.rentaly_management.DTO.ContractTenant>> contractTenants();
    @GET("be/contract-tenants/{contractId}/{tenantId}") Call<com.android_dev.rentaly_management.DTO.ContractTenant> contractTenant(@Path("contractId") String contractId, @Path("tenantId") String tenantId);
    @POST("be/contract-tenants") Call<com.android_dev.rentaly_management.DTO.ContractTenant> createContractTenant(@Body com.android_dev.rentaly_management.DTO.ContractTenantRequest request);
    @PUT("be/contract-tenants/{contractId}/{tenantId}") Call<com.android_dev.rentaly_management.DTO.ContractTenant> updateContractTenant(@Path("contractId") String contractId, @Path("tenantId") String tenantId, @Body com.android_dev.rentaly_management.DTO.ContractTenantRequest request);
    @DELETE("be/contract-tenants/{contractId}/{tenantId}") Call<Void> deleteContractTenant(@Path("contractId") String contractId, @Path("tenantId") String tenantId);
    @GET("be/invoices") Call<java.util.List<com.android_dev.rentaly_management.DTO.Invoice>> invoices(@Query("contractId") String contractId, @Query("status") String status);
    @POST("be/invoices") Call<com.android_dev.rentaly_management.DTO.Invoice> createInvoice(@Body com.android_dev.rentaly_management.DTO.InvoiceRequest request);
    @PUT("be/invoices/{id}") Call<com.android_dev.rentaly_management.DTO.Invoice> updateInvoice(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.InvoiceRequest request);
    @DELETE("be/invoices/{id}") Call<Void> deleteInvoice(@Path("id") String id);
    @GET("be/maintenance") Call<java.util.List<com.android_dev.rentaly_management.DTO.Maintenance>> maintenance(@Query("roomId") String roomId, @Query("status") String status, @Query("priority") String priority);
    @POST("be/maintenance") Call<com.android_dev.rentaly_management.DTO.Maintenance> createMaintenance(@Body com.android_dev.rentaly_management.DTO.MaintenanceRequest request);
    @PUT("be/maintenance/{id}") Call<com.android_dev.rentaly_management.DTO.Maintenance> updateMaintenance(@Path("id") String id, @Body com.android_dev.rentaly_management.DTO.MaintenanceRequest request);
    @DELETE("be/maintenance/{id}") Call<Void> deleteMaintenance(@Path("id") String id);
}
