package com.example.cocodo.utils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subtasks")
public class SubTask {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "task_id")
    public int taskId;

    @ColumnInfo(name = "subtask_name")
    public String subTaskName;

    @ColumnInfo(name = "subtask_desc")
    public String subTaskDesc;

    @ColumnInfo(name = "subtask_time")
    public String subTaskTime;

    @ColumnInfo(name = "subtask_priority")
    public int subTaskPriority;

    @ColumnInfo(name = "isCompleted")
    public int isCompleted;

    public SubTask(int taskId, String subTaskName, String subTaskDesc, String subTaskTime) {
        this.taskId = taskId;
        this.subTaskName = subTaskName;
        this.subTaskDesc = subTaskDesc;
        this.subTaskTime = subTaskTime;
        isCompleted = 0;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public String getSubTaskTime() {
        return subTaskTime;
    }

    public int getIsCompleted() {
        return isCompleted;
    }
    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }
}
