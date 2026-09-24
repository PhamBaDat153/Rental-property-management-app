package com.android_dev.rentaly_management;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScreenFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView screen = new TextView(requireContext());
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(22);
        screen.setText(requireArguments().getString("screen_title", "Rentaly"));
        return screen;
    }
}
