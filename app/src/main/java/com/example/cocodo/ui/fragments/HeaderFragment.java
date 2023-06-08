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

public class HeaderFragment extends Fragment {

    public interface CharPieButtonClickListener {
        public void onCharPieButtonClick();
    }

    private CharPieButtonClickListener charPieButtonClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            charPieButtonClickListener = (HeaderFragment.CharPieButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }
    }

    ImageButton charPieButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.header_fragment, container, false);
        charPieButton = view.findViewById(R.id.charPieButton);
        charPieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                charPieButtonClickListener.onCharPieButtonClick();
            }
        });
        return view;
    }

}
