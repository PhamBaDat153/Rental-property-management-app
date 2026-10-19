package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.R;

public class ServiceFragment extends Fragment {

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
        View view = inflater.inflate(
                R.layout.fragment_service,
                container,
                false
        );

        setupNavigation(view);

        return view;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void setupNavigation(@NonNull View view) {
        NavController navController =
                NavHostFragment.findNavController(this);

        setupInvoiceNavigation(view, navController);
        setupMaintenanceNavigation(view, navController);
        setupCatalogNavigation(view, navController);
    }

    private void setupInvoiceNavigation(
            @NonNull View view,
            @NonNull NavController navController
    ) {
        // Open the invoice screen.
        view.findViewById(R.id.service_open_invoice)
                .setOnClickListener(
                        v -> navController.navigate(R.id.invoiceFragment)
                );
    }

    private void setupMaintenanceNavigation(
            @NonNull View view,
            @NonNull NavController navController
    ) {
        // Open the maintenance screen.
        view.findViewById(R.id.service_open_maintenance)
                .setOnClickListener(
                        v -> navController.navigate(R.id.maintenanceFragment)
                );
    }

    private void setupCatalogNavigation(
            @NonNull View view,
            @NonNull NavController navController
    ) {
        // Open the service catalog screen.
        view.findViewById(R.id.service_open_catalog)
                .setOnClickListener(
                        v -> navController.navigate(R.id.serviceCatalogFragment)
                );
    }
}