package com.example.cocodo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.R;
import com.example.cocodo.database.TaskDao;

import java.util.List;

public class RecyclerSubTaskListAdapter extends RecyclerView.Adapter<RecyclerSubTaskListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<SubTask> subTaskList;
    private final Context mContext;

    private OnItemClickListener listener;

    // Добавляем переменную OnItemClickListener
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Создаем метод установки слушателя нажатия
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    TaskDao taskDao;

    public RecyclerSubTaskListAdapter(Context context, List<SubTask> subTaskList) {
        this.mContext = context;
        this.subTaskList = subTaskList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(v, adapterPosition);
                }
            }
        });

        SubTask subTask = subTaskList.get(position);

        setupNameAndTimeText(holder, subTask);

        boolean isVisible = !holder.textTimeView.getText().toString().trim().isEmpty();
        holder.textTimeView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        holder.checkBox.setChecked(subTask.getIsCompleted() > 0);
    }

    private void setupNameAndTimeText(ViewHolder holder, SubTask subTask) {
        holder.textNameView.setText(subTask.getSubTaskName());
        holder.textTimeView.setText(subTask.getSubTaskTime());
    }


    @Override
    public int getItemCount() {
        return subTaskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textNameView;
        final TextView textTimeView;
        final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            textNameView = view.findViewById(R.id.task_name);
            textTimeView = view.findViewById(R.id.task_time);
            checkBox = view.findViewById(R.id.task_completing);
        }
    }

    String getItem(int id) {
        return subTaskList.get(id).getSubTaskName();
    }
}