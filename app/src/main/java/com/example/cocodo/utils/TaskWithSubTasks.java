package com.example.cocodo.utils;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TaskWithSubTasks {
    @Embedded
    public Task task;

    @Relation(parentColumn = "id", entityColumn = "task_id")
    public List<SubTask>  subTasks;
}