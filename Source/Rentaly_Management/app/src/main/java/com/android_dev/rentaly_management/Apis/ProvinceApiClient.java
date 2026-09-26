package com.android_dev.rentaly_management.Apis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ProvinceApiClient {
    private ProvinceApiClient() { }

    public static final ProvinceApiService api = new Retrofit.Builder()
            .baseUrl("https://provinces.open-api.vn/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProvinceApiService.class);
}
