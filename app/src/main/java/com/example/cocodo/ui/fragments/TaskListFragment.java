package com.example.cocodo.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cocodo.R;

public class TaskListFragment extends Fragment {
    public interface UpdaterRecViewList{
        void updateRevView();
        void closeButtonClickListener();
    }
    private TaskListFragment.UpdaterRecViewList updaterRecViewList;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            updaterRecViewList = (TaskListFragment.UpdaterRecViewList) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_list_fragment, container, false);
        Button closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updaterRecViewList.closeButtonClickListener();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updaterRecViewList.updateRevView();
    }
}
