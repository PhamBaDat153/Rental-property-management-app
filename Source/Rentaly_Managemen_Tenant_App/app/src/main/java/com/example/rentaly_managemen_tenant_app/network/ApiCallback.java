package com.example.rentaly_managemen_tenant_app.network;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
