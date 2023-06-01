package com.example.cocodo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.R;
import com.example.cocodo.database.TaskDao;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class RecyclerTaskListAdapter extends RecyclerView.Adapter<RecyclerTaskListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Task> taskList;
    private final RecyclerView recyclerView;
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

    public RecyclerTaskListAdapter(Context context, List<Task> taskList, RecyclerView recyclerView, TaskDao taskDao) {
        this.mContext = context;
        this.taskList = taskList;
        this.inflater = LayoutInflater.from(context);
        this.recyclerView = recyclerView;
        this.taskDao = taskDao;
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
        Task task = taskList.get(position);
        holder.setPriorityImage(task.getTaskPriority());

        setupNameAndTimeText(holder, task);

        boolean isVisible = !holder.textTimeView.getText().toString().trim().isEmpty();
        holder.textTimeView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.getIsCompleted() > 0);

        if (holder.getAdapterPosition() >= 0) {
            Task currentTask = taskList.get(holder.getAdapterPosition());
            holder.checkBox.setOnCheckedChangeListener(createCheckedChangeListener(holder, currentTask));
        }
    }
    private void setupNameAndTimeText(ViewHolder holder, Task task) {
        holder.textNameView.setText(task.getTaskName());
        holder.textTimeView.setText(task.getTaskTime());
    }

    private CompoundButton.OnCheckedChangeListener createCheckedChangeListener(ViewHolder holder, Task currentTask) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentTask.setIsCompleted(0);
                int pos = holder.getAdapterPosition();
                if (isChecked && pos >= 0) {
                    Task task = taskList.get(pos);
                    if (task.equals(currentTask)) {
                        completeTask(pos, task, holder, isChecked);
                    }
                }
            }
        };
    }

    private void completeTask(int pos, Task task, ViewHolder holder, boolean isChecked) {
        task.setIsCompleted(1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getIsCompleted() == 1 && pos < taskList.size() && task.equals(taskList.get(pos))) {
                    Task removedTask = taskList.remove(pos);
                    int completed = isChecked ? 1 : 0;
                    removedTask.setIsCompleted(completed);
                    updateTask(removedTask);
                    notifyItemRemoved(pos);
                    showSnackbar(removedTask, pos, holder);
                }
            }
        }, 500);
    }

    private void updateTask(Task removedTask) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskDao.update(removedTask);
            }
        }).start();
    }

    private void showSnackbar(Task removedTask, int pos, ViewHolder holder) {
        String undoText = "Отменить";

        View.OnClickListener undoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.add(pos, removedTask);
                notifyItemInserted(pos);
                removedTask.setIsCompleted(0);
                updateTask(removedTask);
                holder.checkBox.setChecked(false);
            }
        };

        Snackbar snackbar = Snackbar.make(recyclerView, "Выполнено", Snackbar.LENGTH_LONG);
        snackbar.setAction(undoText, undoClickListener).show();
    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textNameView;
        final TextView textTimeView;
        final CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            textNameView = view.findViewById(R.id.task_name);
            textTimeView = view.findViewById(R.id.task_time);
            checkBox = view.findViewById(R.id.task_completing);
        }
        public void setPriorityImage(int priority){
            switch (priority){
                case 1:
                    checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(checkBox.getContext().getApplicationContext(), R.color.coco_red)));
                    break;
                case 2:
                    checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(checkBox.getContext().getApplicationContext(), R.color.green)));
                    break;
                case 3:
                    checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(checkBox.getContext().getApplicationContext(), R.color.blue)));
                    break;
                default:
                    checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(checkBox.getContext().getApplicationContext(), R.color.black)));
                    break;
            }
        }
    }

    String getItem(int id) {
        return taskList.get(id).getTaskName();
    }
}