package com.example.rentaly_managemen_tenant_app.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.rentaly_managemen_tenant_app.R;
import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.ContractRepository;
import com.example.rentaly_managemen_tenant_app.utils.SessionManager;

import java.util.List;

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
        createScreen(title, true);
    }

    protected void createScreen(String title, boolean showBack) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.background));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(16), dp(16), dp(28));
        scroll.addView(page);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20), dp(18), dp(20), dp(20));
        hero.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_header_background);

        TextView eyebrow = text("RENTALY  •  KHU VỰC NGƯỜI THUÊ", 11, true);
        eyebrow.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.primary));
        hero.addView(eyebrow, matchWrap());

        TextView heading = text(title, 24, true);
        heading.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.white));
        LinearLayout.LayoutParams headingParams = matchWrap();
        headingParams.topMargin = dp(7);
        hero.addView(heading, headingParams);

        TextView subtitle = text("Thông tin thuê nhà và tiện ích của bạn", 14, false);
        subtitle.setTextColor(0xffe7edf0);
        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.topMargin = dp(4);
        hero.addView(subtitle, subtitleParams);
        page.addView(hero, matchWrap());

        if (showBack) {
            TextView back = text("‹   Quay lại", 14, true);
            back.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.primary));
            back.setGravity(Gravity.CENTER_VERTICAL);
            back.setPadding(dp(2), dp(14), dp(2), dp(8));
            back.setOnClickListener(v -> finish());
            page.addView(back, matchWrap());
        }

        message = text("Đang tải dữ liệu...", 15, false);
        message.setPadding(dp(16), dp(14), dp(16), dp(14));
        message.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_surface_card);
        message.setElevation(dp(1));
        LinearLayout.LayoutParams messageParams = matchWrap();
        messageParams.topMargin = dp(showBack ? 4 : 18);
        page.addView(message, messageParams);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = matchWrap();
        contentParams.topMargin = dp(8);
        page.addView(content, contentParams);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.background));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        BottomNavigationView navigation = new BottomNavigationView(this);
        navigation.setBackgroundColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.white));
        navigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        navigation.setItemHorizontalTranslationEnabled(false);
        navigation.setItemIconTintList(getColorStateList(
                com.example.rentaly_managemen_tenant_app.R.color.tenant_navigation_tint));
        navigation.setItemTextColor(getColorStateList(
                com.example.rentaly_managemen_tenant_app.R.color.tenant_navigation_tint));
        navigation.setElevation(dp(8));
        navigation.inflateMenu(com.example.rentaly_managemen_tenant_app.R.menu.tenant_bottom_navigation);
        navigation.setSelectedItemId(selectedNavigationItem());
        navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == com.example.rentaly_managemen_tenant_app.R.id.nav_home) {
                navigateTo(MainActivity.class);
            } else if (id == com.example.rentaly_managemen_tenant_app.R.id.nav_room) {
                openRoomFromNavigation();
            } else if (id == com.example.rentaly_managemen_tenant_app.R.id.nav_invoices) {
                navigateTo(InvoiceListActivity.class);
            } else if (id == com.example.rentaly_managemen_tenant_app.R.id.nav_maintenance) {
                openMaintenanceFromNavigation();
            } else if (id == com.example.rentaly_managemen_tenant_app.R.id.nav_profile) {
                navigateTo(ProfileActivity.class);
            }
            return true;
        });
        root.addView(navigation, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(68)));
        setContentView(root);
    }

    private int selectedNavigationItem() {
        if (this instanceof MainActivity) return R.id.nav_home;
        if (this instanceof RoomInfoActivity || this instanceof ElectricityWaterActivity) return R.id.nav_room;
        if (this instanceof InvoiceListActivity || this instanceof InvoiceDetailActivity) return R.id.nav_invoices;
        if (this instanceof MaintenanceListActivity || this instanceof CreateMaintenanceActivity) return R.id.nav_maintenance;
        return R.id.nav_profile;
    }

    private void navigateTo(Class<?> destination) {
        if (destination.isInstance(this)) return;
        startActivity(new android.content.Intent(this, destination));
        overridePendingTransition(0, 0);
    }

    private void openRoomFromNavigation() {
        String roomId = getIntent().getStringExtra("room_id");
        if (roomId != null) {
            openRoom(roomId);
            return;
        }
        loadCurrentContract(new RoomCallback() {
            @Override public void onRoom(String id) { openRoom(id); }
            @Override public void onMissing() {
                android.widget.Toast.makeText(TenantBaseActivity.this,
                        "Chưa tìm thấy phòng gắn với hợp đồng.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMaintenanceFromNavigation() {
        String roomId = getIntent().getStringExtra("room_id");
        if (roomId != null) {
            openMaintenance(roomId);
            return;
        }
        loadCurrentContract(new RoomCallback() {
            @Override public void onRoom(String id) { openMaintenance(id); }
            @Override public void onMissing() {
                android.widget.Toast.makeText(TenantBaseActivity.this,
                        "Chưa tìm thấy phòng gắn với hợp đồng.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentContract(RoomCallback callback) {
        new ContractRepository().getMyContracts(session.getTenantId(), new ApiCallback<List<RentalContract>>() {
            @Override public void onSuccess(List<RentalContract> contracts) {
                for (RentalContract contract : contracts) {
                    if (contract.room_id != null && "ACTIVE".equalsIgnoreCase(contract.status)) {
                        callback.onRoom(contract.room_id.toString());
                        return;
                    }
                }
                for (RentalContract contract : contracts) {
                    if (contract.room_id != null) {
                        callback.onRoom(contract.room_id.toString());
                        return;
                    }
                }
                callback.onMissing();
            }
            @Override public void onError(String error) {
                android.widget.Toast.makeText(TenantBaseActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openRoom(String roomId) {
        android.content.Intent intent = new android.content.Intent(this, RoomInfoActivity.class);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openMaintenance(String roomId) {
        android.content.Intent intent = new android.content.Intent(this, MaintenanceListActivity.class);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private interface RoomCallback {
        void onRoom(String id);
        void onMissing();
    }

    protected TextView text(String value, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value == null || value.trim().isEmpty() ? "—" : value);
        view.setTextSize(sp);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.dark_navy));
        view.setLineSpacing(dp(3), 1f);
        return view;
    }

    protected void addLine(String value) {
        int labelEnd = value == null ? -1 : value.indexOf(": ");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(13), dp(16), dp(13));
        row.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_surface_card);
        row.setElevation(dp(1));

        if (labelEnd > 0 && value.indexOf('\n') < 0) {
            TextView label = text(value.substring(0, labelEnd), 12, true);
            label.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.secondary));
            row.addView(label, matchWrap());
            TextView detail = text(value.substring(labelEnd + 2), 15, false);
            LinearLayout.LayoutParams detailParams = matchWrap();
            detailParams.topMargin = dp(5);
            row.addView(detail, detailParams);
        } else {
            TextView detail = text(value, 15, false);
            row.addView(detail, matchWrap());
        }

        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(10);
        content.addView(row, params);
    }

    protected void addSection(String value) {
        TextView row = text(value, 18, true);
        row.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.primary));
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(16);
        params.bottomMargin = dp(8);
        content.addView(row, params);
    }

    protected void addButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label + "     ›");
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.dark_navy));
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        button.setPadding(dp(18), 0, dp(18), 0);
        button.setMinHeight(dp(58));
        button.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_action_background);
        button.setElevation(dp(1));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(10);
        content.addView(button, params);
    }

    protected Button addPrimaryButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.white));
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(54));
        button.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_primary_button);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(8);
        params.bottomMargin = dp(10);
        content.addView(button, params);
        return button;
    }

    protected void styleInput(EditText input, String hint, int minLines) {
        input.setHint(hint);
        input.setTextSize(15);
        input.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.dark_navy));
        input.setHintTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.secondary));
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_input_background);
        input.setMinLines(minLines);
        if (minLines > 1) input.setGravity(Gravity.TOP | Gravity.START);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(12);
        content.addView(input, params);
    }

    protected void styleSpinner(Spinner spinner) {
        spinner.setPadding(dp(14), dp(8), dp(14), dp(8));
        spinner.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_input_background);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(12);
        content.addView(spinner, params);
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
        message.setTextColor(getColor(com.example.rentaly_managemen_tenant_app.R.color.error));
        message.setBackgroundResource(com.example.rentaly_managemen_tenant_app.R.drawable.tenant_error_background);
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
