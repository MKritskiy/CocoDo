package com.example.cocodo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cocodo.utils.Converters;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

@Database(entities = {Task.class, SubTask.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MyDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // изменяем тип данных
            database.execSQL("ALTER TABLE tasks RENAME TO temp_tasks");
            database.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, task_name TEXT, task_desc TEXT, task_time INTEGER NOT NULL, task_priority INTEGER NOT NULL, isCompleted INTEGER NOT NULL, completed_at INTEGER)");
            database.execSQL("INSERT INTO tasks (id, task_name, task_desc, task_time, task_priority, isCompleted, completed_at) SELECT id, task_name, task_desc, task_time, task_priority, isCompleted, completed_at FROM temp_tasks");
            database.execSQL("DROP TABLE temp_tasks");
        }
    };

    private static volatile MyDatabase INSTANCE;
    private static Context thisContext;

    public static MyDatabase getDatabase(final Context context) {
        thisContext = context;
        if (INSTANCE == null) {
            synchronized (MyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    MyDatabase.class, "my-database").addMigrations(MIGRATION_5_6)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}