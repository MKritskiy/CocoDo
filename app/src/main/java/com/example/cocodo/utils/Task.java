package com.example.cocodo.utils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Date;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "task_name")
    private String taskName;

    @ColumnInfo(name = "task_desc")
    private String taskDesc;
    @ColumnInfo(name = "task_time")
    private String taskTime;

    @ColumnInfo(name = "task_priority")
    private int taskPriority;

    @ColumnInfo(name = "isCompleted")
    private int isCompleted;

    @ColumnInfo(name = "completed_at")
    @TypeConverters(Converters.class)
    private Date completedAt;

    public Task(String taskName, String taskDesc, String taskTime) {
        this.taskName = taskName;
        this.taskDesc = taskDesc;
        this.taskTime = taskTime;
        this.taskPriority = 4;
        this.isCompleted = 0;
    }

    // Конструкторы, геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public int getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(int taskPriority) {
        this.taskPriority = taskPriority;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        if (isCompleted == 1)
            this.setCompletedAt(new Date(System.currentTimeMillis()));
        else
            this.setCompletedAt(null);
        this.isCompleted = isCompleted;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}
