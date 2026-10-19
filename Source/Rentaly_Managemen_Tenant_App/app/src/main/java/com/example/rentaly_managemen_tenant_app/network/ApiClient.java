package com.example.rentaly_managemen_tenant_app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.rentaly_managemen_tenant_app.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private ApiClient() {}

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDate.class,
                    (com.google.gson.JsonDeserializer<LocalDate>)
                            (json, type, context) ->
                                    LocalDate.parse(json.getAsString())
            )
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (com.google.gson.JsonDeserializer<LocalDateTime>)
                            (json, type, context) ->
                                    LocalDateTime.parse(json.getAsString())
            )
            .registerTypeAdapter(
                    LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>)
                            (value, type, context) ->
                                    new com.google.gson.JsonPrimitive(
                                            value.toString()
                                    )
            )
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (com.google.gson.JsonSerializer<LocalDateTime>)
                            (value, type, context) ->
                                    new com.google.gson.JsonPrimitive(
                                            value.toString()
                                    )
            )
            .create();

    public static final ApiService api =
            new Retrofit.Builder()
                    .baseUrl(Constants.API_BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create(gson)
                    )
                    .build()
                    .create(ApiService.class);
}
