package com.example.cocodo.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cocodo.R;

public class NavBarFragment extends Fragment {
    public interface OnFragmentButtonClickListener{
        void addTaskButtonClick();
    }
    private NavBarFragment.OnFragmentButtonClickListener fragmentButtonClickListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            fragmentButtonClickListener = (NavBarFragment.OnFragmentButtonClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav_bar_fragment, container, false);
        ImageButton addButton = view.findViewById(R.id.addButton);
        ImageButton groupButton = view.findViewById(R.id.groupButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.addTaskButtonClick();
            }
        });

        return view;
    }
}
