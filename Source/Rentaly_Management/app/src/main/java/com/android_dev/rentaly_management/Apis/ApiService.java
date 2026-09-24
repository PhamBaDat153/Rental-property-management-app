package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("be/user/login")
    Call<User> login(
            @Query("username") String username,
            @Query("password") String password,
            @Query("loginType") String loginType
    );
}
