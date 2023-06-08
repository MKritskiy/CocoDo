package com.example.cocodo.database;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cocodo.ui.fragments.DetailsTaskFragment;
import com.example.cocodo.utils.Converters;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

@Database(entities = {Task.class, SubTask.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MyDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    private static volatile MyDatabase INSTANCE;

    public static MyDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (MyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    MyDatabase.class, "my-database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}