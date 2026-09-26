package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.Province;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProvinceApiService {
    @GET("api/v2/p/") Call<List<Province>> provinces();
    @GET("api/v1/p/{code}") Call<Province> legacyProvince(@Path("code") int code, @Query("depth") int depth);
    @GET("api/v1/d/{code}") Call<Province> legacyDistrict(@Path("code") int code, @Query("depth") int depth);
}
