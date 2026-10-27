package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rentaly_managemen_tenant_app.R;
import com.example.rentaly_managemen_tenant_app.models.Invoice;
import com.example.rentaly_managemen_tenant_app.models.RentalContract;
import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.ContractRepository;
import com.example.rentaly_managemen_tenant_app.repository.InvoiceRepository;
import com.example.rentaly_managemen_tenant_app.repository.UserRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends TenantBaseActivity {
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        createScreen("Tổng quan", false);
        new UserRepository().getProfile(session.getUserId(), new ApiCallback<UserTenant>() {
            @Override
            public void onSuccess(UserTenant profile) {
                if (profile.tenant_id != null) session.saveTenantId(profile.tenant_id.toString());
                showLoaded();
                addWelcomeCard(profile.full_name == null ? session.getUserName() : profile.full_name);
                loadCurrentRoomActions();
            }

            @Override
            public void onError(String error) {
                showFailure(error);
                addButton("Hồ sơ của tôi", v -> open(ProfileActivity.class));
                addQuickAccess();
            }
        });
    }

    private void addWelcomeCard(String name) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(getColor(R.color.surface), getColor(R.color.outline), 12));

        TextView greeting = text("Xin chào, " + (name == null ? "bạn" : name), 20, true);
        greeting.setTextColor(getColor(R.color.dark_navy));
        card.addView(greeting, matchWrap());

        TextView caption = text("Mọi thông tin thuê nhà của bạn ở một nơi.", 14, false);
        caption.setTextColor(getColor(R.color.secondary));
        LinearLayout.LayoutParams captionParams = matchWrap();
        captionParams.topMargin = dp(6);
        card.addView(caption, captionParams);

        LinearLayout.LayoutParams cardParams = matchWrap();
        cardParams.topMargin = dp(16);
        content.addView(card, cardParams);

    }

    private void loadCurrentRoomActions() {
        new ContractRepository().getMyContracts(session.getTenantId(), new ApiCallback<List<RentalContract>>() {
            @Override
            public void onSuccess(List<RentalContract> contracts) {
                if (contracts == null || contracts.isEmpty()) {
                    addSection("Hợp đồng thuê");
                    addLine("Chưa có hợp đồng được liên kết với tài khoản này.");
                    loadInvoiceSummary();
                    return;
                }
                RentalContract current = contracts.get(0);
                for (RentalContract contract : contracts) {
                    if ("ACTIVE".equalsIgnoreCase(contract.status)) {
                        current = contract;
                        break;
                    }
                }
                roomId = current.room_id == null ? null : current.room_id.toString();
                addContractCard(current);
                loadInvoiceSummary();
            }

            @Override
            public void onError(String error) {
                addSection("Hợp đồng thuê");
                addLine("Chưa tải được thông tin hợp đồng.");
                loadInvoiceSummary();
            }
        });
    }

    private void addContractCard(RentalContract contract) {
        addSection("Nơi ở hiện tại");
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(rounded(getColor(R.color.surface), getColor(R.color.outline), 12));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Hợp đồng thuê", 16, true);
        title.setTextColor(getColor(R.color.dark_navy));
        titleRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView status = text(contractStatus(contract.status), 12, true);
        status.setTextColor(getColor(R.color.dark_navy));
        status.setPadding(dp(10), dp(6), dp(10), dp(6));
        status.setBackground(rounded(getColor(R.color.surface), getColor(R.color.primary), 18));
        titleRow.addView(status, new LinearLayout.LayoutParams(-2, -2));
        card.addView(titleRow, matchWrap());

        TextView rent = text(formatMoney(contract.rent_amount), 23, true);
        rent.setTextColor(getColor(R.color.dark_navy));
        LinearLayout.LayoutParams rentParams = matchWrap();
        rentParams.topMargin = dp(14);
        card.addView(rent, rentParams);
        TextView perMonth = text("Tiền thuê mỗi tháng", 13, false);
        perMonth.setTextColor(getColor(R.color.secondary));
        LinearLayout.LayoutParams monthParams = matchWrap();
        monthParams.topMargin = dp(2);
        card.addView(perMonth, monthParams);

        TextView dates = text("Thời hạn: " + value(contract.start_date) + " – " + value(contract.end_date), 14, false);
        dates.setTextColor(getColor(R.color.secondary));
        LinearLayout.LayoutParams dateParams = matchWrap();
        dateParams.topMargin = dp(12);
        card.addView(dates, dateParams);

        TextView action = text("Xem chi tiết hợp đồng   ›", 14, true);
        action.setTextColor(getColor(R.color.dark_navy));
        action.setGravity(Gravity.CENTER_VERTICAL);
        action.setMinHeight(dp(48));
        action.setOnClickListener(v -> open(ContractActivity.class));
        LinearLayout.LayoutParams actionParams = matchWrap();
        actionParams.topMargin = dp(6);
        card.addView(action, actionParams);
        card.setOnClickListener(v -> open(ContractActivity.class));

        LinearLayout.LayoutParams cardParams = matchWrap();
        cardParams.bottomMargin = dp(8);
        content.addView(card, cardParams);
    }

    private void loadInvoiceSummary() {
        new InvoiceRepository().getMyInvoices(session.getTenantId(), new ApiCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> invoices) {
                addSection("Hóa đơn");
                if (invoices == null || invoices.isEmpty()) {
                    addLine("Bạn chưa có hóa đơn nào.");
                    addQuickAccess();
                    return;
                }
                Invoice latest = invoices.get(0);
                for (Invoice invoice : invoices) {
                    if (invoice.due_date != null && (latest.due_date == null
                            || invoice.due_date.isBefore(latest.due_date))) latest = invoice;
                }
                Invoice selected = latest;
                String number = latest.invoice_number == null
                        ? value(latest.invoice_id) : latest.invoice_number;
                addButton("▧   " + number + "   ·   " + invoiceStatus(latest.status), v -> openInvoice(selected));
                addLine("Tổng thanh toán: " + formatMoney(latest.total_amount)
                        + "\nHạn thanh toán: " + value(latest.due_date));
                addButton("Xem tất cả hóa đơn", v -> open(InvoiceListActivity.class));
                addQuickAccess();
            }

            @Override
            public void onError(String error) {
                addSection("Hóa đơn");
                addLine("Chưa tải được danh sách hóa đơn.");
                addButton("Mở hóa đơn", v -> open(InvoiceListActivity.class));
                addQuickAccess();
            }
        });
    }

    private void openInvoice(Invoice invoice) {
        if (invoice.invoice_id == null) return;
        Intent intent = new Intent(this, InvoiceDetailActivity.class);
        intent.putExtra("invoice_id", invoice.invoice_id.toString());
        startActivity(intent);
    }

    private void addQuickAccess() {
        addSection("Truy cập nhanh");
        addButton("Thông tin phòng", v -> openRoomFromHome(RoomInfoActivity.class));
        addButton("Điện, nước và chỉ số", v -> openRoomFromHome(ElectricityWaterActivity.class));
        addButton("Gửi yêu cầu bảo trì", v -> openRoomFromHome(MaintenanceListActivity.class));
        addButton("Thông báo", v -> open(AnnouncementActivity.class));
    }

    private String contractStatus(String status) {
        if (status == null) return "Đang cập nhật";
        if ("ACTIVE".equalsIgnoreCase(status)) return "Đang hiệu lực";
        if ("EXPIRED".equalsIgnoreCase(status)) return "Đã kết thúc";
        if ("TERMINATED".equalsIgnoreCase(status)) return "Đã chấm dứt";
        return status;
    }

    private String invoiceStatus(String status) {
        if (status == null) return "Chưa cập nhật";
        if ("PAID".equalsIgnoreCase(status)) return "Đã thanh toán";
        if ("OVERDUE".equalsIgnoreCase(status)) return "Quá hạn";
        if ("UNPAID".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) return "Chưa thanh toán";
        return status;
    }

    private void openRoomFromHome(Class<?> screen) {
        if (roomId == null) {
            android.widget.Toast.makeText(this, "Chưa tìm thấy phòng gắn với hợp đồng.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, screen);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "—";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void open(Class<?> screen) {
        startActivity(new Intent(this, screen));
    }
}
