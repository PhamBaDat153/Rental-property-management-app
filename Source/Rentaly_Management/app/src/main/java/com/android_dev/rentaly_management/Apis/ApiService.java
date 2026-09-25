package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.User;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.DTO.UserRoleUpdateRequest;
import com.android_dev.rentaly_management.DTO.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.Query;
import retrofit2.http.PUT;

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
}
