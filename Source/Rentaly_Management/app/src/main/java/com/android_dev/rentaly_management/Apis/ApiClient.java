package com.android_dev.rentaly_management.Apis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private ApiClient() {}

    public static final ApiService api = new Retrofit.Builder()
            // Android Emulator -> host machine. Use the computer's LAN IP on a physical device.
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService.class);
}
