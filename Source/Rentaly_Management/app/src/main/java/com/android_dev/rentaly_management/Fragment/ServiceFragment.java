package com.android_dev.rentaly_management.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.android_dev.rentaly_management.R;

public class ServiceFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        view.findViewById(R.id.service_open_meter).setOnClickListener(v -> openMeterPicker());
        view.findViewById(R.id.service_open_catalog).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.serviceCatalogFragment));
        return view;
    }

    private void openMeterPicker() {
        Bundle args = new Bundle();
        args.putBoolean("pick_room", true);
        NavHostFragment.findNavController(this).navigate(R.id.propertiesFragment, args);
    }
}
