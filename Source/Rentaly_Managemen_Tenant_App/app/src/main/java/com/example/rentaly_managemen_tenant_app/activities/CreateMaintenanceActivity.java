package com.example.rentaly_managemen_tenant_app.activities;

import android.os.Bundle;
import android.view.View;
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
        titleInput = new EditText(this);
        titleInput.setHint("Tiêu đề yêu cầu");
        titleInput.setSingleLine(true);
        content.addView(titleInput);

        descriptionInput = new EditText(this);
        descriptionInput.setHint("Mô tả sự cố");
        descriptionInput.setMinLines(3);
        descriptionInput.setGravity(android.view.Gravity.TOP);
        content.addView(descriptionInput);

        addLine("Mức độ ưu tiên");
        priorityInput = new Spinner(this);
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        priorityInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, priorities));
        content.addView(priorityInput);
        submitButton = new android.widget.Button(this);
        submitButton.setText("Gửi yêu cầu");
        content.addView(submitButton);
        submitButton.setOnClickListener(v -> submit());
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
        MaintenanceRequest request = new MaintenanceRequest(
                UUID.fromString(session.getUserId()), UUID.fromString(roomId), title,
                description, priorityInput.getSelectedItem().toString(), "PENDING");
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
                submitButton.setText("Gửi yêu cầu");
                message.setText(error);
                message.setVisibility(View.VISIBLE);
                message.setTextColor(0xffb3261e);
            }
        });
    }
}
