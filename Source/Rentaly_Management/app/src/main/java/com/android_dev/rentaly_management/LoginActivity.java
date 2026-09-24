package com.android_dev.rentaly_management;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android_dev.rentaly_management.Apis.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private TextView usernameTxt;
    private TextView passwordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginBtn = findViewById(R.id.button);
        usernameTxt = findViewById(R.id.editTextText);
        passwordTxt = findViewById(R.id.editTextTextPassword);

        loginBtn.setOnClickListener(view -> login());
    }

    private void login() {
        String username = usernameTxt.getText().toString().trim();
        String password = passwordTxt.getText().toString();

        if (username.isEmpty()) {
            usernameTxt.setError("Nhập tên đăng nhập");
            usernameTxt.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordTxt.setError("Nhập mật khẩu");
            passwordTxt.requestFocus();
            return;
        }

        setLoginEnabled(false);
        ApiClient.api.login(username, password, "Manage").enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                setLoginEnabled(true);
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    startActivity(new android.content.Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Tên đăng nhập hoặc mật khẩu không đúng",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
                setLoginEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Không thể kết nối đến máy chủ",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoginEnabled(boolean enabled) {
        loginBtn.setEnabled(enabled);
        loginBtn.setText(enabled ? "Đăng nhập" : "Đang đăng nhập...");
    }
}
