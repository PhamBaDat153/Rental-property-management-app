package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.LoginRequest;
import com.example.rentaly_managemen_tenant_app.models.LoginResponse;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    public void login(String username, String password, ApiCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(username, password, Constants.LOGIN_TYPE_USE);
        ApiClient.api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getUser_id() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401 || response.code() == 403) {
                    callback.onError("Tên đăng nhập hoặc mật khẩu không đúng");
                } else {
                    callback.onError("Không thể đăng nhập (mã " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable error) {
                callback.onError("Không thể kết nối đến máy chủ. Hãy kiểm tra API đang chạy.");
            }
        });
    }
}
