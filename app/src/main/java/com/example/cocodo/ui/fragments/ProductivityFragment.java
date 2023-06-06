package com.example.cocodo.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.UnstopableCalculator;

public class ProductivityFragment extends Fragment {

    private TextView unstopableCounter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unstopableCounter =view.findViewById(R.id.unstopable_counter);
        unstopableCounter.setText(new UnstopableCalculator().calculate(MyDatabase.getDatabase(getContext().getApplicationContext()).taskDao()).getCurrentStreak());

        return view;
    }
}
