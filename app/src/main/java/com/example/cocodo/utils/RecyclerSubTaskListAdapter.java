package com.example.cocodo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.R;
import com.example.cocodo.database.TaskDao;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class RecyclerSubTaskListAdapter extends RecyclerView.Adapter<RecyclerSubTaskListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<SubTask> subTaskList;
    private final Context mContext;
    private final RecyclerView recyclerView;

    private int subTaskCheckedListSize;
    private int subTaskAllListSize;

    private final TextView textView;
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

    public RecyclerSubTaskListAdapter(
            Context context,
            List<SubTask> subTaskList,
            RecyclerView recyclerView,
            TaskDao taskDao, TextView textView, int allTaskCount) {
        this.mContext = context;
        this.subTaskList = subTaskList;
        this.textView = textView;
        subTaskAllListSize = allTaskCount;
        subTaskCheckedListSize = subTaskAllListSize- subTaskList.size();

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

        SubTask subTask = subTaskList.get(position);

        setupNameAndTimeText(holder, subTask);

        boolean isVisible = !holder.textTimeView.getText().toString().trim().isEmpty();
        holder.textTimeView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        holder.checkBox.setChecked(subTask.getIsCompleted() > 0);
        if (holder.getAdapterPosition() >= 0) {
            SubTask currentTask = subTaskList.get(holder.getAdapterPosition());
            holder.checkBox.setOnCheckedChangeListener(createCheckedChangeListener(holder, currentTask));
        }
    }

    private void setupNameAndTimeText(ViewHolder holder, SubTask subTask) {
        holder.textNameView.setText(subTask.getSubTaskName());
        holder.textTimeView.setText(subTask.getSubTaskTime());
    }
    private CompoundButton.OnCheckedChangeListener createCheckedChangeListener(RecyclerSubTaskListAdapter.ViewHolder holder, SubTask currentTask) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentTask.setIsCompleted(0);
                int pos = holder.getAdapterPosition();
                if (isChecked && pos >= 0) {
                    SubTask task = subTaskList.get(pos);
                    if (task.equals(currentTask)) {
                        completeTask(pos, task, holder, isChecked);
                    }
                }
            }
        };
    }

    private void completeTask(int pos, SubTask task, RecyclerSubTaskListAdapter.ViewHolder holder, boolean isChecked) {
        task.setIsCompleted(1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getIsCompleted() == 1 && pos < subTaskList.size() && task.equals(subTaskList.get(pos))) {
                    SubTask removedTask = subTaskList.remove(pos);
                    int completed = isChecked ? 1 : 0;
                    removedTask.setIsCompleted(completed);
                    updateTask(removedTask);
                    subTaskCheckedListSize++;
                    textView.setText(subTaskCheckedListSize + "/" + subTaskAllListSize);
                    notifyItemRemoved(pos);
                    showSnackbar(removedTask, pos, holder);
                }
            }
        }, 500);
    }

    private void updateTask(SubTask removedTask) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskDao.updateSubTask(removedTask);
            }
        }).start();
    }

    private void showSnackbar(SubTask removedTask, int pos, RecyclerSubTaskListAdapter.ViewHolder holder) {
        String undoText = "Отменить";

        View.OnClickListener undoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subTaskList.add(pos, removedTask);
                notifyItemInserted(pos);
                removedTask.setIsCompleted(0);
                updateTask(removedTask);
                holder.checkBox.setChecked(false);
                subTaskCheckedListSize--;
                textView.setText(subTaskCheckedListSize + "/" + subTaskAllListSize);
            }
        };

        Snackbar snackbar = Snackbar.make(recyclerView, "Выполнено", Snackbar.LENGTH_LONG);
        snackbar.setAction(undoText, undoClickListener).show();
    }

    @Override
    public int getItemCount() {
        if (subTaskList == null) {
            return 0;
        }
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