package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.rentaly_managemen_tenant_app.models.Maintenance;
import com.example.rentaly_managemen_tenant_app.models.MaintenanceRequest;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.MaintenanceRepository;

import java.util.UUID;

public class CreateMaintenanceActivity extends TenantBaseActivity {
    private EditText titleInput;
    private EditText descriptionInput;
    private Spinner priorityInput;
    private android.widget.Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Tạo yêu cầu bảo trì");
        addSection("Mô tả sự cố cần hỗ trợ");
        titleInput = new EditText(this);
        titleInput.setSingleLine(true);
        styleInput(titleInput, "Tiêu đề yêu cầu", 1);

        descriptionInput = new EditText(this);
        descriptionInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        styleInput(descriptionInput, "Mô tả chi tiết vấn đề", 4);

        addSection("Mức độ ưu tiên");
        priorityInput = new Spinner(this);
        String[] priorityLabels = {"Thấp", "Trung bình", "Cao"};
        priorityInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, priorityLabels));
        styleSpinner(priorityInput);
        submitButton = addPrimaryButton("Gửi yêu cầu bảo trì", v -> submit());
    }

    private void submit() {
        String roomId = getIntent().getStringExtra("room_id");
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        if (roomId == null) {
            showFailure("Không tìm thấy phòng.");
            return;
        }
        if (title.isEmpty()) {
            titleInput.setError("Nhập tiêu đề yêu cầu");
            titleInput.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            descriptionInput.setError("Mô tả sự cố");
            descriptionInput.requestFocus();
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Đang gửi...");
        showLoaded();
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        MaintenanceRequest request = new MaintenanceRequest(
                UUID.fromString(session.getUserId()), UUID.fromString(roomId), title,
                description, priorities[priorityInput.getSelectedItemPosition()], "PENDING");
        new MaintenanceRepository().createRequest(request, new ApiCallback<Maintenance>() {
            @Override
            public void onSuccess(Maintenance result) {
                android.widget.Toast.makeText(CreateMaintenanceActivity.this,
                        "Đã gửi yêu cầu bảo trì", android.widget.Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                submitButton.setEnabled(true);
                submitButton.setText("Gửi yêu cầu bảo trì");
                showFailure(error);
            }
        });
    }
}
