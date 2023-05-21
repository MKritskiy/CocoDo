package com.example.cocodo.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

@Database(entities = {Task.class, SubTask.class}, version = 4)
public abstract class MyDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}