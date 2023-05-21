package com.example.cocodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.ui.fragments.AddTaskFragment;
import com.example.cocodo.ui.fragments.BackgroundFragment;
import com.example.cocodo.ui.fragments.DetailsTaskFragment;
import com.example.cocodo.ui.fragments.NavBarFragment;
import com.example.cocodo.ui.fragments.TaskListFragment;
import com.example.cocodo.utils.RecyclerTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends
        AppCompatActivity
        implements
        AddTaskFragment.OnFragmentButtonClickListener,
        NavBarFragment.OnFragmentButtonClickListener,
        TaskListFragment.UpdaterRecViewList
{
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `subtasks` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`task_id` INTEGER NOT NULL, "
                    + "`subtask_name` TEXT, "
                    + "`subtask_desc` TEXT, "
                    + "`subtask_time` TEXT, "
                    + "`subtask_priority` INTEGER NOT NULL, "
                    + "`isCompleted` INTEGER NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY(`task_id`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE"
                    + ")");
        }
    };

    // Создание объекта БД
    static MyDatabase db;

    private static List<Task> taskList = new ArrayList<>();
    static RecyclerView recyclerView;
    static RecyclerTaskListAdapter adapter;

    static FragmentManager fragmentManager;

    private static class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        Task task;

        public LoadDataTask(Context context, Task task) {
            this.context = context.getApplicationContext();
            this.task = task;
            if (db==null){
                db = Room.databaseBuilder(context.getApplicationContext(), MyDatabase.class, "tasks").build();
            }
        }

        public LoadDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (task!=null) {
                db.taskDao().insertAll(task);
                taskList.add(task);
            } else {
                MainActivity.taskList = db.taskDao().getAllUnchecked();
//               for (Task t:taskList) {
//                   db.taskDao().delete(t);
//               }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (adapter == null) {
                MainActivity.adapter = new RecyclerTaskListAdapter(context, MainActivity.taskList, MainActivity.recyclerView, db.taskDao());
                MainActivity.recyclerView.setAdapter(MainActivity.adapter);
                adapter.setOnItemClickListener(new RecyclerTaskListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onTaskListItemClickListener(view, position);
                    }
                });
            } else {
                MainActivity.adapter.notifyDataSetChanged();
            }
        }
    }
    String sharedText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager=getSupportFragmentManager();

//        Intent intent = getIntent();
//        String action = intent.getAction();
//        String type = intent.getType();
//
//
//
//        if (Intent.ACTION_SEND.equals(action) && type != null) {
//            if ("text/plain".equals(type)) {
//                Log.i("Test", "onCreate: ");
//                handleSendText(intent);
//            } // Handle text being sent
//        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        Log.i("Test", "onNewIntent: ");
//        handleSendText(intent);
//    }
//
//    void handleSendText(Intent intent) {
//        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
//        Log.i("Test", "onHandleSendText: "+ sharedText);
//
//        if (sharedText != null) {
//            recyclerView = findViewById(R.id.taskRecyclerList);
//            new LoadDataTask(this, new Task(sharedText, "", "")).execute();
//        }
//    }
    public static void onTaskListItemClickListener(View view, int position){
        Bundle bundle = new Bundle();
        Fragment backgroundFragment = new BackgroundFragment();
        DetailsTaskFragment fragment = new DetailsTaskFragment();
        bundle.putInt("POSITION", position);
        fragment.setArguments(bundle);
        fragmentManager
                .beginTransaction()
                .add(android.R.id.content, backgroundFragment)
                .commit();
        fragment.show(fragmentManager, "TaskDetailsFragment");
    }
    @Override
    public void addTaskButtonClick() {
        // Создаем экземпляр затемненного фона
        Fragment backgroundFragment = new BackgroundFragment();
        // Создаем экземпляр вашего фрагмента
        AddTaskFragment fragment = new AddTaskFragment();
        // Показываем затемненный фон с помощью менеджера фрагментов
        fragmentManager.beginTransaction()
                .add(android.R.id.content, backgroundFragment)
                .commit();
        // Показываем фрагмент с помощью менеджера фрагментов
        fragment.show(fragmentManager, "AddTaskFragment");
    }

    @Override
    public void sendTaskButtonClick(String taskName, String taskDesc, String taskTime) {
        recyclerView = findViewById(R.id.taskRecyclerList);
        new LoadDataTask(this, new Task(taskName, taskDesc, taskTime)).execute();
    }

    @Override
    public void updateRevView() {
        recyclerView = findViewById(R.id.taskRecyclerList);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount()<1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        db = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "my-database")
                .addMigrations(MIGRATION_3_4).build();
        new LoadDataTask(this).execute();
    }

    @Override
    public void closeButtonClickListener() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Текст, который нужно передать");
        startActivity(Intent.createChooser(shareIntent, "Поделиться через..."));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = getIntent();
//        String action = intent.getAction();
//        String type = intent.getType();
//        Log.i("Test", "onResume: "+action+" " +type);
//        new LoadDataTask(this).execute();
    }
}