package com.android_dev.rentaly_management.Fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android_dev.rentaly_management.View.DonutChartView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.Invoice;
import com.android_dev.rentaly_management.DTO.Maintenance;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenFragment extends Fragment {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int REQUEST_ROOMS = 0;
    private static final int REQUEST_CONTRACTS = 1;
    private static final int REQUEST_INVOICES = 2;
    private static final int REQUEST_MAINTENANCE = 3;

    private static final int REQUEST_COUNT = 4;
    private static final int MAX_PRIORITY_ITEMS = 3;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String PRIORITY_HIGH = "HIGH";

    private static final String ARG_CREATE_ROOM = "create_room";
    private static final String ARG_CREATE_CONTRACT = "create_contract";
    private static final String ARG_CREATE_INVOICE = "create_invoice";
    private static final String ARG_CREATE_ANNOUNCEMENT =
            "create_announcement";

    private static final String ARG_SHOW_ROOMS = "show_rooms";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<Room> rooms = new ArrayList<>();
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Maintenance> maintenanceRequests = new ArrayList<>();

    private final boolean[] loaded = new boolean[REQUEST_COUNT];
    private final boolean[] failed = new boolean[REQUEST_COUNT];

    private int loadGeneration;

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    private View rootView;

    private TextView greeting;
    private TextView errorMessage;

    private ProgressBar loadingIndicator;

    private LinearLayout kpiContainer;
    private DonutChartView paymentChart;
    private LinearLayout paymentLegend;
    private LinearLayout attentionContainer;
    private LinearLayout roomSummaryContainer;
    private LinearLayout actionsContainer;
    private LinearLayout priorityContainer;

    // -------------------------------------------------------------------------
    // Fragment lifecycle
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(
                R.layout.fragment_dashboard,
                container,
                false
        );

        bindViews();
        setupGreeting();
        setupActions();

        loadDashboardData();

        return rootView;
    }

    private void bindViews() {
        greeting = rootView.findViewById(
                R.id.dashboard_greeting
        );

        errorMessage = rootView.findViewById(
                R.id.dashboard_error
        );

        loadingIndicator = rootView.findViewById(
                R.id.dashboard_loading
        );

        kpiContainer = rootView.findViewById(R.id.dashboard_kpis);
        paymentChart = rootView.findViewById(R.id.dashboard_payment_chart);
        paymentLegend = rootView.findViewById(R.id.dashboard_payment_legend);

        attentionContainer = rootView.findViewById(
                R.id.dashboard_attention
        );

        roomSummaryContainer = rootView.findViewById(
                R.id.dashboard_rooms
        );

        actionsContainer = rootView.findViewById(
                R.id.dashboard_actions
        );

        priorityContainer = rootView.findViewById(
                R.id.dashboard_priority
        );
    }

    private void setupGreeting() {
        String name = requireActivity()
                .getIntent()
                .getStringExtra("user_name");

        if (name == null || name.trim().isEmpty()) {
            greeting.setText(
                    getString(R.string.dashboard_greeting_default)
            );
            return;
        }

        greeting.setText(
                getString(R.string.dashboard_greeting, name)
        );
    }

    // -------------------------------------------------------------------------
    // Load dashboard data
    // -------------------------------------------------------------------------

    private void loadDashboardData() {
        int generation = ++loadGeneration;

        resetData();
        setLoading(true);

        errorMessage.setVisibility(View.GONE);
        errorMessage.setOnClickListener(null);

        requestRooms(generation);
        requestContracts(generation);
        requestInvoices(generation);
        requestMaintenance(generation);
    }

    private void resetData() {
        rooms.clear();
        contracts.clear();
        invoices.clear();
        maintenanceRequests.clear();

        for (int index = 0; index < REQUEST_COUNT; index++) {
            loaded[index] = false;
            failed[index] = false;
        }
    }

    // -------------------------------------------------------------------------
    // Room request
    // -------------------------------------------------------------------------

    private void requestRooms(int generation) {
        ApiClient.api.rooms().enqueue(
                new Callback<List<Room>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Room>> call,
                            @NonNull Response<List<Room>> response
                    ) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        boolean requestFailed =
                                !response.isSuccessful()
                                        || response.body() == null;

                        if (!requestFailed) {
                            rooms.clear();
                            rooms.addAll(response.body());
                        }

                        finishRequest(
                                REQUEST_ROOMS,
                                generation,
                                requestFailed
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Room>> call,
                            @NonNull Throwable throwable
                    ) {
                        finishRequest(
                                REQUEST_ROOMS,
                                generation,
                                true
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Contract request
    // -------------------------------------------------------------------------

    private void requestContracts(int generation) {
        ApiClient.api.contracts().enqueue(
                new Callback<List<RentalContract>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Response<List<RentalContract>> response
                    ) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        boolean requestFailed =
                                !response.isSuccessful()
                                        || response.body() == null;

                        if (!requestFailed) {
                            contracts.clear();
                            contracts.addAll(response.body());
                        }

                        finishRequest(
                                REQUEST_CONTRACTS,
                                generation,
                                requestFailed
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<RentalContract>> call,
                            @NonNull Throwable throwable
                    ) {
                        finishRequest(
                                REQUEST_CONTRACTS,
                                generation,
                                true
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Invoice request
    // -------------------------------------------------------------------------

    private void requestInvoices(int generation) {
        ApiClient.api.invoices(null, null).enqueue(
                new Callback<List<Invoice>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Invoice>> call,
                            @NonNull Response<List<Invoice>> response
                    ) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        boolean requestFailed =
                                !response.isSuccessful()
                                        || response.body() == null;

                        if (!requestFailed) {
                            invoices.clear();
                            invoices.addAll(response.body());
                        }

                        finishRequest(
                                REQUEST_INVOICES,
                                generation,
                                requestFailed
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Invoice>> call,
                            @NonNull Throwable throwable
                    ) {
                        finishRequest(
                                REQUEST_INVOICES,
                                generation,
                                true
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Maintenance request
    // -------------------------------------------------------------------------

    private void requestMaintenance(int generation) {
        ApiClient.api.maintenance(null, null, null).enqueue(
                new Callback<List<Maintenance>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<Maintenance>> call,
                            @NonNull Response<List<Maintenance>> response
                    ) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        boolean requestFailed =
                                !response.isSuccessful()
                                        || response.body() == null;

                        if (!requestFailed) {
                            maintenanceRequests.clear();
                            maintenanceRequests.addAll(response.body());
                        }

                        finishRequest(
                                REQUEST_MAINTENANCE,
                                generation,
                                requestFailed
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<Maintenance>> call,
                            @NonNull Throwable throwable
                    ) {
                        finishRequest(
                                REQUEST_MAINTENANCE,
                                generation,
                                true
                        );
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Request completion
    // -------------------------------------------------------------------------

    private void finishRequest(
            int requestIndex,
            int generation,
            boolean requestFailed
    ) {
        if (!isCurrentGeneration(generation)) {
            return;
        }

        loaded[requestIndex] = true;
        failed[requestIndex] = requestFailed;

        if (!areAllRequestsLoaded()) {
            return;
        }

        setLoading(false);
        updateErrorState();
        renderDashboard();
    }

    private boolean areAllRequestsLoaded() {
        for (boolean isLoaded : loaded) {
            if (!isLoaded) {
                return false;
            }
        }

        return true;
    }

    private int getFailedRequestCount() {
        int count = 0;

        for (boolean requestFailed : failed) {
            if (requestFailed) {
                count++;
            }
        }

        return count;
    }

    private void updateErrorState() {
        int failureCount = getFailedRequestCount();

        if (failureCount == 0) {
            errorMessage.setVisibility(View.GONE);
            errorMessage.setOnClickListener(null);
            return;
        }

        int messageResource = failureCount == REQUEST_COUNT
                ? R.string.dashboard_full_error
                : R.string.dashboard_partial_error;

        errorMessage.setText(getString(messageResource));
        errorMessage.setVisibility(View.VISIBLE);

        errorMessage.setOnClickListener(
                view -> loadDashboardData()
        );
    }

    private boolean isCurrentGeneration(int generation) {
        return isAdded()
                && rootView != null
                && generation == loadGeneration;
    }

    // -------------------------------------------------------------------------
    // Dashboard rendering
    // -------------------------------------------------------------------------

    private void renderDashboard() {
        if (rootView == null) {
            return;
        }

        renderKpis();
        renderPaymentChart();
        renderAttention();
        renderRoomSummary();
        renderPriorityItems();
    }

    // -------------------------------------------------------------------------
    // KPI section
    // -------------------------------------------------------------------------

    private void renderKpis() {
        kpiContainer.removeAllViews();

        List<Room> validRooms = getValidRooms();
        Set<UUID> occupiedRoomIds = getOccupiedRoomIds(validRooms);
        int availableCount = 0;
        for (Room room : validRooms) {
            if (!occupiedRoomIds.contains(room.room_id)
                    && STATUS_AVAILABLE.equalsIgnoreCase(room.status)) {
                availableCount++;
            }
        }

        List<Invoice> overdueInvoices = getOverdueInvoices();
        List<Maintenance> openMaintenance = getOpenMaintenance();

        addKpiRow(
                createKpiCard(
                        getString(R.string.dashboard_kpi_occupied),
                        String.valueOf(occupiedRoomIds.size()),
                        view -> navigate(R.id.propertiesFragment, createRoomsBundle())
                ),
                createKpiCard(
                        getString(R.string.dashboard_kpi_available),
                        String.valueOf(availableCount),
                        view -> navigate(R.id.propertiesFragment, createRoomsBundle())
                )
        );
        addKpiRow(
                createKpiCard(
                        getString(R.string.dashboard_kpi_overdue),
                        String.valueOf(overdueInvoices.size()),
                        view -> navigate(R.id.invoiceFragment, null)
                ),
                createKpiCard(
                        getString(R.string.dashboard_kpi_maintenance),
                        String.valueOf(openMaintenance.size()),
                        view -> navigate(R.id.maintenanceFragment, null)
                )
        );
    }

    private Button createKpiCard(
            @NonNull String label,
            @NonNull String value,
            @NonNull View.OnClickListener listener
    ) {
        Button card = new Button(requireContext());
        card.setText(label + "\n" + value);
        card.setAllCaps(false);
        card.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        card.setTextSize(15);
        card.setTextColor(requireContext().getResources().getColor(R.color.dark_navy));
        card.setMinHeight(76);
        card.setPadding(16, 8, 16, 8);
        card.setBackgroundResource(com.android_dev.rentaly_management.R.drawable.user_detail_card);
        card.setStateListAnimator(null);
        card.setOnClickListener(listener);
        return card;
    }

    private void addKpiRow(@NonNull Button left, @NonNull Button right) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rightParams.setMargins(8, 0, 0, 8);
        row.addView(right, rightParams);
        kpiContainer.addView(row);
    }

    private void renderPaymentChart() {
        if (paymentChart == null || paymentLegend == null) return;

        paymentLegend.removeAllViews();
        int paidCount = 0;
        int unpaidCount = 0;
        int overdueCount = 0;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (Invoice invoice : invoices) {
            if (invoice == null) continue;
            BigDecimal amount = invoice.total_amount == null ? BigDecimal.ZERO : invoice.total_amount;
            if (STATUS_RECEIVED.equalsIgnoreCase(invoice.status)) {
                paidCount++;
                paidAmount = paidAmount.add(amount);
            } else if (invoice.due_date != null && invoice.due_date.isBefore(today)) {
                overdueCount++;
                overdueAmount = overdueAmount.add(amount);
            } else {
                unpaidCount++;
                unpaidAmount = unpaidAmount.add(amount);
            }
        }

        int total = paidCount + unpaidCount + overdueCount;
        if (total == 0) {
            paymentChart.setData(new float[0], new int[0], "");
            paymentLegend.addView(createLegendText(getString(R.string.dashboard_payment_empty), false));
            return;
        }

        paymentChart.setData(
                new float[]{paidAmount.floatValue(), unpaidAmount.floatValue(), overdueAmount.floatValue()},
                new int[]{0xFF3B8D6B, 0xFF548CA8, 0xFFC66A3D},
                getString(R.string.dashboard_payment_total, total)
        );

        addPaymentLegend(paymentLegend, "Đã thanh toán", paidCount, paidAmount, 0xFF3B8D6B);
        addPaymentLegend(paymentLegend, "Chưa thanh toán", unpaidCount, unpaidAmount, 0xFF548CA8);
        addPaymentLegend(paymentLegend, "Quá hạn", overdueCount, overdueAmount, 0xFFC66A3D);
    }

    private void addPaymentLegend(@NonNull LinearLayout parent, @NonNull String label, int count, @NonNull BigDecimal amount, int color) {
        if (count == 0) return;
        TextView legend = createLegendText(getString(R.string.dashboard_payment_legend, label, count, formatMoney(amount)), true);
        legend.setCompoundDrawablesWithIntrinsicBounds(createLegendDot(color), null, null, null);
        legend.setCompoundDrawablePadding(12);
        parent.addView(legend);
    }

    private TextView createLegendText(@NonNull String text, boolean important) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextColor(requireContext().getResources().getColor(important ? R.color.dark_navy : R.color.secondary));
        view.setTextSize(14);
        view.setPadding(0, 8, 0, 8);
        return view;
    }

    private android.graphics.drawable.Drawable createLegendDot(int color) {
        android.graphics.drawable.GradientDrawable dot = new android.graphics.drawable.GradientDrawable();
        dot.setColor(color);
        dot.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        dot.setSize(18, 18);
        return dot;
    }

    private String formatMoney(@NonNull BigDecimal amount) {
        return NumberFormat.getNumberInstance(java.util.Locale.US).format(amount.setScale(0, java.math.RoundingMode.HALF_UP));
    }

    // -------------------------------------------------------------------------
    // Attention section
    // -------------------------------------------------------------------------

    private void renderAttention() {
        attentionContainer.removeAllViews();

        addRequestFailureCards();

        List<Invoice> overdueInvoices = getOverdueInvoices();
        List<Maintenance> openMaintenance = getOpenMaintenance();
        List<RentalContract> expiringContracts = getExpiringContracts();

        boolean hasAttentionItems = !overdueInvoices.isEmpty()
                || !openMaintenance.isEmpty()
                || !expiringContracts.isEmpty();

        if (!hasAttentionItems && getFailedRequestCount() == 0) {
            addTextCard(
                    attentionContainer,
                    getString(R.string.dashboard_no_attention),
                    getString(R.string.dashboard_all_clear),
                    null
            );
            return;
        }

        addOpenMaintenanceCard(openMaintenance);
        addOverdueInvoicesCard(overdueInvoices);
        addExpiringContractsCard(expiringContracts);
    }

    private void addRequestFailureCards() {
        if (failed[REQUEST_MAINTENANCE]) {
            addTextCard(
                    attentionContainer,
                    getString(
                            R.string.dashboard_maintenance_unavailable
                    ),
                    getString(R.string.dashboard_retry),
                    view -> retry(REQUEST_MAINTENANCE)
            );
        }

        if (failed[REQUEST_INVOICES]) {
            addTextCard(
                    attentionContainer,
                    getString(
                            R.string.dashboard_invoices_unavailable
                    ),
                    getString(R.string.dashboard_retry),
                    view -> retry(REQUEST_INVOICES)
            );
        }

        if (failed[REQUEST_CONTRACTS]) {
            addTextCard(
                    attentionContainer,
                    getString(
                            R.string.dashboard_contracts_unavailable
                    ),
                    getString(R.string.dashboard_retry),
                    view -> retry(REQUEST_CONTRACTS)
            );
        }
    }

    private List<Invoice> getOverdueInvoices() {
        List<Invoice> overdueInvoices = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Invoice invoice : invoices) {
            if (invoice == null || invoice.due_date == null) {
                continue;
            }

            boolean isOverdue = invoice.due_date.isBefore(today);
            boolean isReceived = STATUS_RECEIVED.equalsIgnoreCase(
                    invoice.status
            );

            if (isOverdue && !isReceived) {
                overdueInvoices.add(invoice);
            }
        }

        return overdueInvoices;
    }

    private List<Maintenance> getOpenMaintenance() {
        List<Maintenance> openItems = new ArrayList<>();

        for (Maintenance item : maintenanceRequests) {
            if (item == null) {
                continue;
            }

            if (isOpenMaintenance(item)) {
                openItems.add(item);
            }
        }

        return openItems;
    }

    private boolean isOpenMaintenance(@NonNull Maintenance item) {
        return STATUS_PENDING.equalsIgnoreCase(item.status)
                || STATUS_PROCESSING.equalsIgnoreCase(item.status);
    }

    private List<RentalContract> getExpiringContracts() {
        List<RentalContract> expiringContracts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(30);

        for (RentalContract contract : contracts) {
            if (contract == null || contract.end_date == null) {
                continue;
            }

            boolean isActive = STATUS_ACTIVE.equalsIgnoreCase(
                    contract.status
            );

            boolean hasNotExpired =
                    !contract.end_date.isBefore(today);

            boolean expiresWithin30Days =
                    !contract.end_date.isAfter(deadline);

            if (isActive && hasNotExpired && expiresWithin30Days) {
                expiringContracts.add(contract);
            }
        }

        expiringContracts.sort(
                Comparator.comparing(
                        contract -> contract.end_date
                )
        );

        return expiringContracts;
    }

    private void addOpenMaintenanceCard(
            @NonNull List<Maintenance> openMaintenance
    ) {
        if (openMaintenance.isEmpty()) {
            return;
        }

        addTextCard(
                attentionContainer,
                getString(
                        R.string.dashboard_open_maintenance,
                        openMaintenance.size()
                ),
                getHighPriorityText(openMaintenance),
                view -> navigate(
                        R.id.maintenanceFragment,
                        null
                )
        );
    }

    private void addOverdueInvoicesCard(
            @NonNull List<Invoice> overdueInvoices
    ) {
        if (overdueInvoices.isEmpty()) {
            return;
        }

        addTextCard(
                attentionContainer,
                getString(
                        R.string.dashboard_overdue_invoices,
                        overdueInvoices.size()
                ),
                getString(R.string.dashboard_all_locations),
                view -> navigate(
                        R.id.invoiceFragment,
                        null
                )
        );
    }

    private void addExpiringContractsCard(
            @NonNull List<RentalContract> expiringContracts
    ) {
        if (expiringContracts.isEmpty()) {
            return;
        }

        addTextCard(
                attentionContainer,
                getString(
                        R.string.dashboard_expiring_contracts,
                        expiringContracts.size()
                ),
                getString(R.string.dashboard_expiring_period),
                view -> navigate(
                        R.id.contractsFragment,
                        null
                )
        );
    }

    // -------------------------------------------------------------------------
    // Room summary section
    // -------------------------------------------------------------------------

    private void renderRoomSummary() {
        roomSummaryContainer.removeAllViews();

        if (failed[REQUEST_ROOMS] || failed[REQUEST_CONTRACTS]) {
            addTextCard(
                    roomSummaryContainer,
                    getString(R.string.dashboard_rooms_unavailable),
                    getString(R.string.dashboard_retry),
                    view -> retryRoomSummary()
            );
            return;
        }

        List<Room> validRooms = getValidRooms();
        Set<UUID> occupiedRoomIds = getOccupiedRoomIds(validRooms);

        int availableCount = 0;
        int unavailableCount = 0;

        for (Room room : validRooms) {
            if (occupiedRoomIds.contains(room.room_id)) {
                continue;
            }

            if (STATUS_AVAILABLE.equalsIgnoreCase(room.status)) {
                availableCount++;
            } else {
                unavailableCount++;
            }
        }

        String summaryText =
                getString(
                        R.string.dashboard_total_rooms,
                        validRooms.size()
                )
                        + "\n"
                        + getString(
                        R.string.dashboard_occupied_rooms,
                        occupiedRoomIds.size()
                )
                        + "    "
                        + getString(
                        R.string.dashboard_available_rooms,
                        availableCount
                )
                        + "    "
                        + getString(
                        R.string.dashboard_unavailable_rooms,
                        unavailableCount
                );

        addTextCard(
                roomSummaryContainer,
                summaryText,
                getString(R.string.dashboard_all_locations),
                view -> navigate(
                        R.id.propertiesFragment,
                        createRoomsBundle()
                )
        );
    }

    private List<Room> getValidRooms() {
        List<Room> validRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room != null && room.room_id != null) {
                validRooms.add(room);
            }
        }
        return validRooms;
    }

    private Set<UUID> getOccupiedRoomIds(@NonNull List<Room> validRooms) {
        Set<UUID> roomIds = new HashSet<>();
        for (Room room : validRooms) {
            roomIds.add(room.room_id);
        }

        Set<UUID> occupiedRoomIds = new HashSet<>();
        for (RentalContract contract : contracts) {
            if (contract != null
                    && contract.room_id != null
                    && roomIds.contains(contract.room_id)
                    && STATUS_ACTIVE.equalsIgnoreCase(contract.status)) {
                occupiedRoomIds.add(contract.room_id);
            }
        }
        return occupiedRoomIds;
    }

    private Bundle createRoomsBundle() {
        Bundle arguments = new Bundle();

        arguments.putBoolean(ARG_SHOW_ROOMS, true);

        return arguments;
    }

    // -------------------------------------------------------------------------
    // Priority section
    // -------------------------------------------------------------------------

    private void renderPriorityItems() {
        priorityContainer.removeAllViews();

        List<Maintenance> openMaintenance = getOpenMaintenance();

        openMaintenance.sort(
                (first, second) -> Boolean.compare(
                        !isHighPriority(first),
                        !isHighPriority(second)
                )
        );

        int itemCount = Math.min(
                MAX_PRIORITY_ITEMS,
                openMaintenance.size()
        );

        for (int index = 0; index < itemCount; index++) {
            Maintenance item = openMaintenance.get(index);

            addTextCard(
                    priorityContainer,
                    getString(
                            R.string.dashboard_maintenance_item,
                            getValue(item.title),
                            getRoomCode(item.room_id)
                    ),
                    getString(
                            R.string.dashboard_maintenance_priority,
                            getPriorityLabel(item.priority),
                            getMaintenanceStatusLabel(item.status)
                    ),
                    view -> navigate(
                            R.id.maintenanceFragment,
                            null
                    )
            );
        }

        if (priorityContainer.getChildCount() == 0) {
            addFirstOverdueInvoice();
        }
    }

    private boolean isHighPriority(@NonNull Maintenance item) {
        return PRIORITY_HIGH.equalsIgnoreCase(item.priority);
    }

    private void addFirstOverdueInvoice() {
        LocalDate today = LocalDate.now();

        for (Invoice invoice : invoices) {
            if (invoice == null || invoice.due_date == null) {
                continue;
            }

            boolean isOverdue = invoice.due_date.isBefore(today);
            boolean isReceived = STATUS_RECEIVED.equalsIgnoreCase(
                    invoice.status
            );

            if (!isOverdue || isReceived) {
                continue;
            }

            addTextCard(
                    priorityContainer,
                    getString(
                            R.string.dashboard_invoice_item,
                            getValue(invoice.invoice_number)
                    ),
                    getString(
                            R.string.dashboard_invoice_status,
                            getValue(invoice.contract_id)
                    ),
                    view -> navigate(
                            R.id.invoiceFragment,
                            null
                    )
            );

            return;
        }
    }

    // -------------------------------------------------------------------------
    // Quick actions
    // -------------------------------------------------------------------------

    private void setupActions() {
        actionsContainer.removeAllViews();

        addActionRow(
                createActionButton(
                        R.string.dashboard_add_room,
                        view -> navigate(
                                R.id.propertiesFragment,
                                createBooleanBundle(
                                        ARG_CREATE_ROOM,
                                        true
                                )
                        )
                ),
                createActionButton(
                        R.string.dashboard_add_contract,
                        view -> navigate(
                                R.id.contractsFragment,
                                createBooleanBundle(
                                        ARG_CREATE_CONTRACT,
                                        true
                                )
                        )
                )
        );

        addActionRow(
                createActionButton(
                        R.string.dashboard_add_invoice,
                        view -> navigate(
                                R.id.invoiceFragment,
                                createBooleanBundle(
                                        ARG_CREATE_INVOICE,
                                        true
                                )
                        )
                ),
                createActionButton(
                        R.string.dashboard_add_announcement,
                        view -> navigate(
                                R.id.announcementFragment,
                                createBooleanBundle(
                                        ARG_CREATE_ANNOUNCEMENT,
                                        true
                                )
                        )
                )
        );
    }

    private Bundle createBooleanBundle(
            @NonNull String key,
            boolean value
    ) {
        Bundle arguments = new Bundle();

        arguments.putBoolean(key, value);

        return arguments;
    }

    private Button createActionButton(
            int textResource,
            @NonNull View.OnClickListener listener
    ) {
        Button button = new Button(requireContext());

        button.setText(textResource);
        button.setMinHeight(52);

        button.setTextColor(
                requireContext().getResources()
                        .getColor(R.color.dark_navy)
        );

        button.setTextSize(13);
        button.setAllCaps(false);

        button.setGravity(
                Gravity.START | Gravity.CENTER_VERTICAL
        );

        button.setPadding(16, 0, 16, 0);
        button.setBackgroundResource(R.drawable.dashboard_action);
        button.setStateListAnimator(null);
        button.setOnClickListener(listener);

        return button;
    }

    private void addActionRow(
            @NonNull Button leftButton,
            @NonNull Button rightButton
    ) {
        LinearLayout row = new LinearLayout(requireContext());

        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(
                leftButton,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        LinearLayout.LayoutParams rightParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        rightParams.setMargins(8, 0, 0, 8);

        row.addView(rightButton, rightParams);

        actionsContainer.addView(row);
    }

    // -------------------------------------------------------------------------
    // Dashboard cards
    // -------------------------------------------------------------------------

    private void addTextCard(
            @NonNull LinearLayout parent,
            @NonNull String title,
            @NonNull String subtitle,
            @Nullable View.OnClickListener listener
    ) {
        LinearLayout card = new LinearLayout(requireContext());

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 14, 16, 14);
        card.setBackgroundResource(R.drawable.user_detail_card);

        card.setClickable(listener != null);
        card.setFocusable(listener != null);

        if (listener != null) {
            card.setOnClickListener(listener);
        }

        TextView titleView = new TextView(requireContext());

        titleView.setText(title);

        titleView.setTextColor(
                requireContext().getResources()
                        .getColor(R.color.dark_navy)
        );

        titleView.setTextSize(16);

        titleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        card.addView(titleView);

        TextView subtitleView = new TextView(requireContext());

        subtitleView.setText(
                subtitle + (listener == null ? "" : "  ›")
        );

        subtitleView.setTextColor(
                requireContext().getResources()
                        .getColor(R.color.secondary)
        );

        subtitleView.setTextSize(14);
        subtitleView.setPadding(0, 4, 0, 0);

        card.addView(subtitleView);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 10);

        parent.addView(card, cardParams);
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void navigate(
            int destination,
            @Nullable Bundle arguments
    ) {
        NavHostFragment.findNavController(this).navigate(
                destination,
                arguments == null ? new Bundle() : arguments
        );
    }

    // -------------------------------------------------------------------------
    // Retry failed requests
    // -------------------------------------------------------------------------

    private void retry(int requestIndex) {
        if (requestIndex < 0 || requestIndex >= REQUEST_COUNT) {
            return;
        }

        int generation = ++loadGeneration;

        loaded[requestIndex] = false;
        failed[requestIndex] = false;

        prepareRetry();

        requestByIndex(requestIndex, generation);
    }

    private void retryRoomSummary() {
        int generation = ++loadGeneration;

        loaded[REQUEST_ROOMS] = false;
        loaded[REQUEST_CONTRACTS] = false;

        failed[REQUEST_ROOMS] = false;
        failed[REQUEST_CONTRACTS] = false;

        prepareRetry();

        requestRooms(generation);
        requestContracts(generation);
    }

    private void prepareRetry() {
        setLoading(true);

        errorMessage.setVisibility(View.GONE);
        errorMessage.setOnClickListener(null);
    }

    private void requestByIndex(
            int requestIndex,
            int generation
    ) {
        switch (requestIndex) {
            case REQUEST_ROOMS:
                requestRooms(generation);
                break;

            case REQUEST_CONTRACTS:
                requestContracts(generation);
                break;

            case REQUEST_INVOICES:
                requestInvoices(generation);
                break;

            case REQUEST_MAINTENANCE:
                requestMaintenance(generation);
                break;

            default:
                setLoading(false);
                break;
        }
    }

    private String getHighPriorityText(
            @NonNull List<Maintenance> items
    ) {
        for (Maintenance item : items) {
            if (item != null && isHighPriority(item)) {
                return getString(R.string.dashboard_high_priority);
            }
        }

        return getString(R.string.dashboard_all_locations);
    }

    // -------------------------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------------------------

    private String getRoomCode(@Nullable UUID roomId) {
        for (Room room : rooms) {
            if (room != null
                    && room.room_id != null
                    && room.room_id.equals(roomId)) {
                return getValue(room.room_code);
            }
        }

        return getString(R.string.dashboard_unknown_room);
    }

    private String getValue(@Nullable Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return getString(R.string.dashboard_unknown_room);
        }

        return value.toString();
    }

    private String getPriorityLabel(@Nullable String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) return "Cao";
        if ("LOW".equalsIgnoreCase(priority)) return "Thấp";
        return "Bình thường";
    }

    private String getMaintenanceStatusLabel(
            @Nullable String status
    ) {
        return STATUS_PROCESSING.equalsIgnoreCase(status)
                ? "Đang xử lý"
                : "Đang chờ";
    }

    // -------------------------------------------------------------------------
    // Loading helpers
    // -------------------------------------------------------------------------

    private void setLoading(boolean isLoading) {
        if (loadingIndicator == null) {
            return;
        }

        loadingIndicator.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    // -------------------------------------------------------------------------
    // Fragment cleanup
    // -------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        // Invalidate callbacks associated with the destroyed view.
        loadGeneration++;

        rootView = null;

        greeting = null;
        errorMessage = null;
        loadingIndicator = null;

        attentionContainer = null;
        roomSummaryContainer = null;
        actionsContainer = null;
        kpiContainer = null;
        paymentChart = null;
        paymentLegend = null;
        priorityContainer = null;

        super.onDestroyView();
    }
}