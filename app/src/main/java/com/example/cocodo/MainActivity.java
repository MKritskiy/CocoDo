package com.example.cocodo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cocodo.api.ApiClient;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity
        extends
        AppCompatActivity
        implements
        AddTaskFragment.OnFragmentButtonClickListener,
        NavBarFragment.OnFragmentButtonClickListener,
        TaskListFragment.UpdaterRecViewList,
        DetailsTaskFragment.OnFragmentButtonClickListener {
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
    static RecyclerView recyclerView;
    static RecyclerTaskListAdapter adapter;
    static FragmentManager fragmentManager;
    private static List<Task> taskList = new ArrayList<>();
    String sharedText;

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
    public static void onTaskListItemClickListener(View view, int position) {
        DetailsTaskFragment lastFragment = (DetailsTaskFragment) fragmentManager.findFragmentByTag("TaskDetailsFragment");
        if (lastFragment == null || !lastFragment.isAdded()) {
            Bundle bundle = new Bundle();
            DetailsTaskFragment fragment = new DetailsTaskFragment();
            Task task = taskList.get(position);
            bundle.putInt("taskId", task.getId());
            fragment.setArguments(bundle);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
            transaction.add(android.R.id.content, new BackgroundFragment());

            transaction.add(fragment, "TaskDetailsFragment")
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
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
    public void makeReq(){
        ApiClient.ApiInterface apiInterface = ApiClient.getApiInterface();

        Call<List<ApiClient.Post>> call = apiInterface.getPosts();
        call.enqueue(new Callback<List<ApiClient.Post>>() {
            @Override
            public void onResponse(Call<List<ApiClient.Post>> call, Response<List<ApiClient.Post>> response) {
                if (response.isSuccessful()) {
                    List<ApiClient.Post> posts = response.body();
                    for (ApiClient.Post post : posts) {
                        Log.i("TAG", "Post ID: " + post.getId());
                        Log.i("TAG", "Post Title: " + post.getTitle());
                        Log.i("TAG", "Post Body: " + post.getBody());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiClient.Post>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Call<ApiClient.Post> createPostCall = apiInterface.createPost(123, "My Title", "My Body");
        createPostCall.enqueue(new Callback<ApiClient.Post>() {
            @Override
            public void onResponse(Call<ApiClient.Post> call, Response<ApiClient.Post> response) {
                if (response.isSuccessful()) {
                    ApiClient.Post post = response.body();
                    Log.i("TAG", "New Post ID: " + post.getId());
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiClient.Post> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        int postId = 100;
        Call<ApiClient.Post> getPostCall = apiInterface.getPost(postId);
        getPostCall.enqueue(new Callback<ApiClient.Post>() {
            @Override
            public void onResponse(Call<ApiClient.Post> call, Response<ApiClient.Post> response) {
                if (response.isSuccessful()) {
                    ApiClient.Post post = response.body();
                    Log.i("TAG", "Post ID: " + post.getId());
                    Log.i("TAG", "Post Title: " + post.getTitle());
                    Log.i("TAG", "Post Body: " + post.getBody());
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiClient.Post> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void addTaskButtonClick() {
        // Создаем экземпляр вашего фрагмента
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSubTask", false);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        // Показываем затемненный фон с помощью менеджера фрагментов
        transaction.add(android.R.id.content, new BackgroundFragment());

        // Показываем фрагмент с помощью менеджера фрагментов
        transaction.add(fragment, "AddTaskFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addTaskButtonClick(int taskId) {
        // Создаем экземпляр вашего фрагмента
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSubTask", true);
        bundle.putInt("taskId", taskId);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        // Показываем затемненный фон с помощью менеджера фрагментов
        transaction.add(android.R.id.content, new BackgroundFragment());

        // Показываем фрагмент с помощью менеджера фрагментов
        transaction.add(fragment, "AddTaskFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void priorityButtonClick(View view) {
        PopupWindow backgroundPopupWindow = new PopupWindow(this);
        View backgroundView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.background_dim, null);
        backgroundPopupWindow.setContentView(backgroundView);
        backgroundPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        backgroundPopupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        backgroundPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        backgroundPopupWindow.setFocusable(true);
        backgroundPopupWindow.setAnimationStyle(R.style.BackgroundAnimation);
        backgroundPopupWindow.showAtLocation(view, Gravity.CENTER | Gravity.BOTTOM, 0, 0);


        PopupWindow popupWindow = new PopupWindow(this);
        View layout = getLayoutInflater().inflate(R.layout.popup_window, null);
        popupWindow.setContentView(layout);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);

//      popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setAnimationStyle(R.style.DialogAnimation);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundPopupWindow.dismiss();
                popupWindow.dismiss();
            }
        });
        backgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundPopupWindow.dismiss();
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(view, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        LinearLayout[] layouts = new LinearLayout[]{layout.findViewById(R.id.option1), layout.findViewById(R.id.option2), layout.findViewById(R.id.option3), layout.findViewById(R.id.option4)};
        for (int i = 0; i < layouts.length; i++) {
            int finalI = i;
            layouts[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailsTaskFragment fragment = (DetailsTaskFragment) fragmentManager.findFragmentByTag("TaskDetailsFragment");
                    fragment.setPriority(finalI + 1);
                    backgroundPopupWindow.dismiss();
                    popupWindow.dismiss();
                }
            });
        }
    }

    @Override
    public void sendTaskButtonClick(String taskName, String taskDesc, String taskTime) {
        recyclerView = findViewById(R.id.taskRecyclerList);
        new LoadDataTask(this, new Task(taskName, taskDesc, taskTime)).execute();
    }

    @Override
    public void sendSubTaskButtonClick(int taskId, String subTaskName, String subTaskDescription, String subTaskTime) {
        Log.i("TAG", taskId + " " + subTaskName);
        DetailsTaskFragment fragment = (DetailsTaskFragment) fragmentManager.findFragmentByTag("TaskDetailsFragment");
        fragment.addSubTask(taskId, subTaskName, subTaskDescription, subTaskTime);
    }

    @Override
    public void updateTaskRecView() {
        recyclerView = findViewById(R.id.taskRecyclerList);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount() < 1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
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

    private static class LoadDataTask extends AsyncTask<Void, Void, Void> {

        Task task;
        private final Context context;

        public LoadDataTask(Context context, Task task) {
            this.context = context.getApplicationContext();
            this.task = task;
        }

        public LoadDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (task != null) {
                MyDatabase.getDatabase(context).taskDao().insertTask(task);
                Task newTask = MyDatabase.getDatabase(context).taskDao().getLastInsertedTask();
                taskList.add(newTask);
            } else {
                MainActivity.taskList = MyDatabase.getDatabase(context).taskDao().getAllUncheckedTasksSortedByPriority();
                adapter = null;
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
                MainActivity.adapter = new RecyclerTaskListAdapter(context, MainActivity.taskList, MainActivity.recyclerView, MyDatabase.getDatabase(context).taskDao());
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
}