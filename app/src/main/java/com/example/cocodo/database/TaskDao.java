package com.example.cocodo.database;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasksObservable();
    @Query("SELECT * FROM tasks")
    List<Task> getAll();
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    List<Task> getAllUnchecked();
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY task_priority ASC ")
    List<Task> getAllUncheckedTasksSortedByPriority();
    @Insert
    void insertTask(Task task);
    @Query("SELECT * FROM tasks ORDER BY id DESC LIMIT 1")
    Task getLastInsertedTask();
    @Update
    void update(Task task);
    @Query("DELETE FROM tasks")
    void deleteAllTasks();
    @Delete
    void deleteTask(Task task);
    @Insert
    void insertSubTask(SubTask subTask);
    @Update
    void updateSubTask(SubTask subTask);
    @Delete
    void deleteSubTask(SubTask subTask);
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completed_at ASC")
    List<Task> getCompletedTasksSortedByDate();
    @Query("DELETE FROM subtasks")
    void deleteAllSubTasks();
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    List<SubTask> getAllSubTasks(int taskId);
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    LiveData<List<SubTask>> getAllSubTasksObservable(int taskId);
    @Query("SELECT * FROM subtasks WHERE task_id=:taskId AND isCompleted = 0")
    List<SubTask> getAllUncheckedSubTasks(int taskId);
    @Query("SELECT * FROM subtasks WHERE task_id=:taskId AND isCompleted = 1")
    List<SubTask> getAllCheckedSubTasks(int taskId);
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(int taskId);

}