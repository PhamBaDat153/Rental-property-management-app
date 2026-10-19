package com.example.rentaly_managemen_tenant_app.repository;

import com.example.rentaly_managemen_tenant_app.models.Invoice;
import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.network.ApiClient;
import com.example.rentaly_managemen_tenant_app.network.ApiRequests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepository {
    private final ContractRepository contractRepository = new ContractRepository();

    public void getMyInvoices(String tenantId, ApiCallback<List<Invoice>> callback) {
        contractRepository.getMyContracts(tenantId, new ApiCallback<List<RentalContract>>() {
            @Override
            public void onSuccess(List<RentalContract> contracts) {
                if (contracts.isEmpty()) {
                    callback.onSuccess(Collections.emptyList());
                    return;
                }
                List<Invoice> invoices = Collections.synchronizedList(new ArrayList<>());
                int[] remaining = {contracts.size()};
                boolean[] failed = {false};
                for (RentalContract contract : contracts) {
                    if (contract.contract_id == null) {
                        finishOne(remaining, failed, invoices, callback, null);
                        continue;
                    }
                    ApiClient.api.getInvoices(contract.contract_id.toString(), null)
                            .enqueue(new Callback<List<Invoice>>() {
                                @Override
                                public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                                    if (!response.isSuccessful() || response.body() == null) {
                                        finishOne(remaining, failed, invoices, callback,
                                                "Không thể tải hóa đơn (mã " + response.code() + ").");
                                        return;
                                    }
                                    invoices.addAll(response.body());
                                    finishOne(remaining, failed, invoices, callback, null);
                                }

                                @Override
                                public void onFailure(Call<List<Invoice>> call, Throwable error) {
                                    finishOne(remaining, failed, invoices, callback,
                                            "Không thể kết nối để tải hóa đơn.");
                                }
                            });
                }
            }

            @Override
            public void onError(String message) { callback.onError(message); }
        });
    }

    private void finishOne(int[] remaining, boolean[] failed, List<Invoice> invoices,
                           ApiCallback<List<Invoice>> callback, String error) {
        synchronized (remaining) {
            if (failed[0]) return;
            if (error != null) {
                failed[0] = true;
                callback.onError(error);
                return;
            }
            if (--remaining[0] == 0) callback.onSuccess(invoices);
        }
    }

    public void getInvoice(String id, ApiCallback<Invoice> callback) {
        ApiRequests.enqueue(ApiClient.api.getInvoiceById(id), callback);
    }
}
