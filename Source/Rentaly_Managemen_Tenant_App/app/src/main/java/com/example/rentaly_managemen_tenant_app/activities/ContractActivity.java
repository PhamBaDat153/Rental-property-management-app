package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.ContractRepository;

import java.util.List;

public class ContractActivity extends TenantBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Hợp đồng thuê");
        new ContractRepository().getMyContracts(session.getTenantId(), new ApiCallback<List<RentalContract>>() {
            @Override
            public void onSuccess(List<RentalContract> contracts) {
                showLoaded();
                if (contracts.isEmpty()) {
                    showEmpty("Chưa có hợp đồng được liên kết với tài khoản này.");
                    return;
                }
                for (RentalContract contract : contracts) {
                    addSection("Hợp đồng " + value(contract.contract_id));
                    addLine("Trạng thái: " + value(contract.status));
                    addLine("Thời hạn: " + value(contract.start_date) + " – " + value(contract.end_date));
                    addLine("Tiền thuê: " + value(contract.rent_amount) + " | Tiền cọc: " + value(contract.deposit_required));
                    addLine("Ngày thanh toán: ngày " + value(contract.billing_day)
                            + " | Hạn thanh toán: " + value(contract.payment_due_days) + " ngày");
                    if (contract.terms != null && !contract.terms.trim().isEmpty()) addLine("Điều khoản: " + contract.terms);
                    if (contract.room_id != null) {
                        String roomId = contract.room_id.toString();
                        addButton("Xem phòng trong hợp đồng", v -> {
                            Intent intent = new Intent(ContractActivity.this, RoomInfoActivity.class);
                            intent.putExtra("room_id", roomId);
                            startActivity(intent);
                        });
                    }
                }
            }

            @Override
            public void onError(String error) { showFailure(error); }
        });
    }
}
