package com.example.cocodo.database;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;
import com.example.cocodo.utils.TaskWithSubTasks;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks")
    List<Task> getAll();
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    List<Task> getAllUnchecked();
    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    List<Task> loadAllByIds(int[] taskIds);

    @Query("SELECT * FROM tasks WHERE task_name LIKE :taskName LIMIT 1")
    Task findByName(String taskName);

    @Transaction
    @Query("SELECT * FROM tasks")
    public List<TaskWithSubTasks> getTasksWithSubTasks();

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    public List<SubTask> getSubTasksForTask(int taskId);

    @Insert
    void insertAll(Task... tasks);
    @Update
    void update(Task task);
    @Delete
    void delete(Task task);
}