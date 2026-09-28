package com.android_dev.rentaly_management.Apis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Type;

public final class ApiClient {
    private ApiClient() {}

    public static final com.google.gson.Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>)
                    (json, type, context) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>)
                    (json, type, context) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>)
                    (value, type, context) -> new com.google.gson.JsonPrimitive(value.toString()))
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                    (value, type, context) -> new com.google.gson.JsonPrimitive(value.toString()))
            .create();

    public static final ApiService api = new Retrofit.Builder()
            // Android Emulator -> host machine. Use the computer's LAN IP on a physical device.
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);
}
