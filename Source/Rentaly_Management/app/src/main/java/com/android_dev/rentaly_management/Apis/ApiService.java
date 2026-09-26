package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;
import com.android_dev.rentaly_management.DTO.UserUpdateRequest;
import com.android_dev.rentaly_management.DTO.Location;
import com.android_dev.rentaly_management.DTO.LocationRequest;
import com.android_dev.rentaly_management.DTO.Room;

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
    @GET("be/user/login")
    Call<User> login(
            @Query("username") String username,
            @Query("password") String password,
            @Query("loginType") String loginType
    );

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
}
