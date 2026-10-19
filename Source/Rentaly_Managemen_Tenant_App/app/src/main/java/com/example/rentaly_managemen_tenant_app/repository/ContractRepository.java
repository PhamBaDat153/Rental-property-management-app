package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.ContractTenant;
import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractRepository {
    public void getMyContracts(String tenantId, ApiCallback<List<RentalContract>> callback) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            callback.onError("Không tìm thấy hồ sơ người thuê cho tài khoản này.");
            return;
        }
        ApiRequests.enqueue(ApiClient.api.getContractTenants(), new ApiCallback<List<ContractTenant>>() {
            @Override
            public void onSuccess(List<ContractTenant> assignments) {
                List<String> contractIds = new ArrayList<>();
                for (ContractTenant assignment : assignments) {
                    if (assignment.tenant_id != null && tenantId.equals(assignment.tenant_id.toString())
                            && assignment.contract_id != null
                            && !contractIds.contains(assignment.contract_id.toString())) {
                        contractIds.add(assignment.contract_id.toString());
                    }
                }
                loadContracts(contractIds, callback);
            }

            @Override
            public void onError(String message) { callback.onError(message); }
        });
    }

    private void loadContracts(List<String> ids, ApiCallback<List<RentalContract>> callback) {
        if (ids.isEmpty()) {
            callback.onSuccess(Collections.emptyList());
            return;
        }
        List<RentalContract> loaded = Collections.synchronizedList(new ArrayList<>());
        int[] remaining = {ids.size()};
        boolean[] failed = {false};
        for (String id : ids) {
            ApiClient.api.getContractById(id).enqueue(new Callback<RentalContract>() {
                @Override
                public void onResponse(Call<RentalContract> call, Response<RentalContract> response) {
                    synchronized (remaining) {
                        if (failed[0]) return;
                        if (response.isSuccessful() && response.body() != null) {
                            loaded.add(response.body());
                        } else {
                            failed[0] = true;
                            callback.onError("Không thể tải thông tin hợp đồng (mã " + response.code() + ").");
                            return;
                        }
                        if (--remaining[0] == 0) callback.onSuccess(loaded);
                    }
                }

                @Override
                public void onFailure(Call<RentalContract> call, Throwable error) {
                    synchronized (remaining) {
                        if (failed[0]) return;
                        failed[0] = true;
                        callback.onError("Không thể kết nối để tải hợp đồng.");
                    }
                }
            });
        }
    }
}
