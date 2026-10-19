package com.example.rentaly_managemen_tenant_app.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ApiRequests {
    private ApiRequests() {}

    public static <T> void enqueue(Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    return;
                }
                if (response.code() == 401 || response.code() == 403) {
                    callback.onError("Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại.");
                } else if (response.code() == 404) {
                    callback.onError("Không tìm thấy dữ liệu yêu cầu.");
                } else {
                    callback.onError("Máy chủ trả về lỗi " + response.code() + ".");
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                callback.onError("Không thể kết nối máy chủ. Kiểm tra API và địa chỉ máy chủ.");
            }
        });
    }
}
