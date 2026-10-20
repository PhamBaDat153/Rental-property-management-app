package com.android_dev.rentaly_management.Apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private ApiClient() {
    }

    // Cấu hình Gson để chuyển đổi dữ liệu ngày tháng giữa JSON và Java.
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, context) ->
                            LocalDate.parse(json.getAsString())
            )
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                            LocalDateTime.parse(json.getAsString())
            )
            .registerTypeAdapter(
                    LocalDate.class,
                    (JsonSerializer<LocalDate>) (value, type, context) ->
                            new JsonPrimitive(value.toString())
            )
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (value, type, context) ->
                            new JsonPrimitive(value.toString())
            )
            .create();

    // Tạo Retrofit client để gọi API backend.
    public static final ApiService api = new Retrofit.Builder()
            // Dùng 10.0.2.2 để Android Emulator truy cập máy tính host.
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(
                    GsonConverterFactory.create(gson)
            )
            .build()
            .create(ApiService.class);
}