package com.example.cocodo.utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.cocodo.R;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

import java.util.List;

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.ViewHolder> {

    private List<SubTask> taskList;

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout taskLayout;
        CheckBox taskCompleting;
        TextView taskName, taskTime;

        public ViewHolder(View itemView) {
            super(itemView);
            taskLayout = itemView.findViewById(R.id.taskLayout);
            taskCompleting = itemView.findViewById(R.id.task_completing);
            taskName = itemView.findViewById(R.id.task_name);
            taskTime = itemView.findViewById(R.id.task_time);
        }
    }

    public TempAdapter(List<SubTask> taskList) {
        this.taskList = taskList;
        Log.d("TempAdapter", "subTaskList size: " + taskList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SubTask task = taskList.get(position);
        holder.taskCompleting.setChecked(task.getIsCompleted()>0);
        holder.taskName.setText(task.getSubTaskName());
        holder.taskTime.setText(task.getSubTaskTime());

        holder.taskCompleting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                task.setIsCompleted(isChecked?1:0);
            }
        });

        holder.taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // действия при нажатии на элемент списка
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}