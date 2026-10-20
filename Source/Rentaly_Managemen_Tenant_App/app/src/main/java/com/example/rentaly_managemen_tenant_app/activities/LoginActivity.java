package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rentaly_managemen_tenant_app.R;
import com.example.rentaly_managemen_tenant_app.models.LoginResponse;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.AuthRepository;
import com.example.rentaly_managemen_tenant_app.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_LOCAL_NETWORK_PERMISSION = 2001;
    private static final String ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK";

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView errorText;
    private final AuthRepository authRepository = new AuthRepository();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openTenantHome();
            return;
        }

        usernameInput = findViewById(R.id.tenant_username);
        passwordInput = findViewById(R.id.tenant_password);
        loginButton = findViewById(R.id.tenant_login_button);
        progressBar = findViewById(R.id.tenant_login_progress);
        errorText = findViewById(R.id.tenant_login_error);
        loginButton.setOnClickListener(view -> login());
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (username.isEmpty()) {
            usernameInput.setError("Nhập tên đăng nhập");
            usernameInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Nhập mật khẩu");
            passwordInput.requestFocus();
            return;
        }

        // Android 17 blocks LAN connections by default for apps targeting API 37+.
        if (Build.VERSION.SDK_INT >= 37
                && checkSelfPermission(ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ACCESS_LOCAL_NETWORK},
                    REQUEST_LOCAL_NETWORK_PERMISSION
            );
            return;
        }

        setLoading(true);
        authRepository.login(username, password, new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse user) {
                sessionManager.saveSession(user.getUser_id().toString(), user.getUser_name());
                openTenantHome();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                errorText.setText(message);
                errorText.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCAL_NETWORK_PERMISSION) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            login();
        } else {
            errorText.setText("Cần cấp quyền truy cập mạng cục bộ để kết nối tới máy chủ API.");
            errorText.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        errorText.setVisibility(android.view.View.GONE);
    }

    private void openTenantHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
