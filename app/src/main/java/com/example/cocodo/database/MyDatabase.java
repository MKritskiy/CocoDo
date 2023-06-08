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
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN completed_at INTEGER");
            database.execSQL("UPDATE tasks SET completed_at = datetime(completed_at/1000, 'unixepoch') WHERE completed_at IS NOT NULL");
        }
    };
    private static volatile MyDatabase INSTANCE;

    public static MyDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (MyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    MyDatabase.class, "my-database")
                            .addMigrations(MIGRATION_4_5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Слушатель изменения базы данных

}