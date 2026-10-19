package com.example.rentaly_managemen_tenant_app.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rentaly_managemen_tenant_app.utils.SessionManager;

public abstract class TenantBaseActivity extends AppCompatActivity {
    protected SessionManager session;
    protected LinearLayout content;
    protected TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }
    }

    protected void createScreen(String title) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(20), dp(20), dp(28));
        scroll.addView(page);

        TextView heading = text(title, 25, true);
        page.addView(heading, matchWrap());
        TextView back = text("‹  Quay lại", 15, false);
        back.setTextColor(0xff336b84);
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = matchWrap();
        backParams.topMargin = dp(8);
        backParams.bottomMargin = dp(16);
        page.addView(back, backParams);

        message = text("Đang tải dữ liệu...", 15, false);
        page.addView(message, matchWrap());
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = matchWrap();
        contentParams.topMargin = dp(12);
        page.addView(content, contentParams);
        setContentView(scroll);
    }

    protected TextView text(String value, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value == null || value.trim().isEmpty() ? "—" : value);
        view.setTextSize(sp);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(0xff202124);
        view.setLineSpacing(dp(3), 1f);
        return view;
    }

    protected void addLine(String value) {
        TextView row = text(value, 16, false);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundColor(0xfff2f5f7);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);
        content.addView(row, params);
    }

    protected void addSection(String value) {
        TextView row = text(value, 18, true);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(12);
        params.bottomMargin = dp(8);
        content.addView(row, params);
    }

    protected void addButton(String label, View.OnClickListener listener) {
        android.widget.Button button = new android.widget.Button(this);
        button.setText(label);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);
        content.addView(button, params);
    }

    protected void showEmpty(String value) {
        message.setVisibility(View.VISIBLE);
        message.setText(value);
    }

    protected void showLoaded() {
        message.setVisibility(View.GONE);
    }

    protected void showFailure(String error) {
        message.setVisibility(View.VISIBLE);
        message.setText(error);
        message.setTextColor(0xffb3261e);
    }

    protected String value(Object value) {
        return value == null ? "—" : value.toString();
    }

    protected int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
