package com.example.cocodo.database;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks")
    List<Task> getAll();
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    List<Task> getAllUnchecked();

    @Insert
    void insertTask(Task task);

    @Query("SELECT * FROM tasks ORDER BY id DESC LIMIT 1")
    Task getLastInsertedTask();

    @Update
    void update(Task task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Insert
    void insertSubTask(SubTask subTask);

    @Update
    void updateSubTask(SubTask subTask);

    @Delete
    void deleteSubTask(SubTask subTask);

    @Query("DELETE FROM subtasks")
    void deleteAllSubTasks();

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    List<SubTask> getAllSubTasks(int taskId);
    @Query("SELECT * FROM subtasks WHERE task_id=:taskId AND isCompleted = 0")
    List<SubTask> getAllUncheckedSubTasks(int taskId);
}