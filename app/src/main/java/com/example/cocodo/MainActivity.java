package com.example.cocodo;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cocodo.alarms.TaskNotificationService;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.ui.fragments.AddTaskFragment;
import com.example.cocodo.ui.fragments.BackgroundFragment;
import com.example.cocodo.ui.fragments.DetailsTaskFragment;
import com.example.cocodo.ui.fragments.HeaderFragment;
import com.example.cocodo.ui.fragments.HistoryFragment;
import com.example.cocodo.ui.fragments.NavBarFragment;
import com.example.cocodo.ui.fragments.ProductivityFragment;
import com.example.cocodo.ui.fragments.TaskListFragment;
import com.example.cocodo.utils.RecyclerTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.Task;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity
        extends
        AppCompatActivity
        implements
        AddTaskFragment.OnFragmentButtonClickListener,
        NavBarFragment.OnFragmentButtonClickListener,
        TaskListFragment.UpdaterRecViewList,
        DetailsTaskFragment.OnFragmentButtonClickListener,
        HeaderFragment.CharPieButtonClickListener,
        ProductivityFragment.HistoryButtonClickListener,
        HistoryFragment.UpdaterRecViewList
{
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN completed_at TIMESTAMP");
        }
    };
    static RecyclerView recyclerView;
    static RecyclerTaskListAdapter adapter;
    static FragmentManager fragmentManager;
    private static List<Task> taskList = new ArrayList<>();
    String sharedText;



    public static void onTaskListItemClickListener(View view, Task task) {
        DetailsTaskFragment lastFragment = (DetailsTaskFragment) fragmentManager.findFragmentByTag("TaskDetailsFragment");
        if (lastFragment == null || !lastFragment.isAdded()) {
            Bundle bundle = new Bundle();
            DetailsTaskFragment fragment = new DetailsTaskFragment();
            bundle.putInt("taskId", task.getId());
            fragment.setArguments(bundle);


            fragment.show(fragmentManager, "TaskDetailsFragment");


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        Intent intent = new Intent(this, TaskNotificationService.class);
        startService(intent);
    }

    @Override
    public void addTaskButtonClick() {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSubTask", false);
        fragment.setArguments(bundle);



        fragment.show(fragmentManager, "AddTaskFragment");


    }

    @Override
    public void addTaskButtonClick(int taskId) {
        // Создаем экземпляр вашего фрагмента
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSubTask", true);
        bundle.putInt("taskId", taskId);
        fragment.setArguments(bundle);

        // Показываем фрагмент с помощью менеджера фрагментов
        fragment.show(fragmentManager, "AddTaskFragment");

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
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

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
        backgroundPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundPopupWindow.dismiss();
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundPopupWindow.dismiss();
                popupWindow.dismiss();
            }
        });
        LinearLayout[] layouts = new LinearLayout[]{layout.findViewById(R.id.option1), layout.findViewById(R.id.option2), layout.findViewById(R.id.option3), layout.findViewById(R.id.option4)};
        for (int i = 0; i < layouts.length; i++) {
            int finalI = i;
            layouts[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = (DetailsTaskFragment) fragmentManager.findFragmentByTag("TaskDetailsFragment");
                    if (fragment==null){
                        fragment =  (AddTaskFragment) fragmentManager.findFragmentByTag("AddTaskFragment");
                        ((AddTaskFragment)fragment).setPriority(finalI + 1);
                    } else
                        ((DetailsTaskFragment)fragment).setPriority(finalI + 1);
                    backgroundPopupWindow.dismiss();
                    popupWindow.dismiss();
                }
            });
        }
        popupWindow.showAtLocation(view, Gravity.CENTER | Gravity.BOTTOM, 0, 0);

    }

    @Override
    public void sendTaskButtonClick(String taskName, String taskDesc, String taskTime, int taskPriority) {
        recyclerView = findViewById(R.id.taskRecyclerList);
        long taskTimeLong = 0;
        if (taskTime!=null) {
            DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("ru"));
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                java.util.Date utilDate = df.parse(taskTime.replace(".", ""));
                taskTimeLong = new java.sql.Date(utilDate.getTime()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                taskTimeLong = new java.sql.Date(0).getTime();
            }
        }
        new LoadDataTask(this, new Task(taskName, taskDesc, taskTimeLong, taskPriority)).execute();
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
    public void onCharPieButtonClick() {
        ProductivityFragment fragment = new ProductivityFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right);
        // Показываем фрагмент с помощью менеджера фрагментов
        transaction.add(R.id.contentFragment,fragment, "ProductivityFragment").addToBackStack(null).commit();
    }

    @Override
    public void onHistoryButtonClick() {
        HistoryFragment fragment = new HistoryFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right);
        transaction.add(R.id.contentFragment, fragment, "HistoryFragment").addToBackStack(null).commit();
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private final Context context;
        Task task;
        ItemTouchHelper itemTouchHelper;

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
//              for (Task t:taskList) {
//                  MyDatabase.getDatabase(context).taskDao().deleteAllSubTasks();
//                  MyDatabase.getDatabase(context).taskDao().deleteAllTasks();
//
//              }
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
                        onTaskListItemClickListener(view, taskList.get(position));
                    }
                });
                itemTouchHelper =
                        new ItemTouchHelper(
                                new ItemTouchHelper.SimpleCallback(
                                        0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                        int swipedTaskPosition = viewHolder.getAdapterPosition();
                                        Task swipedTask = taskList.get(swipedTaskPosition);

                                        Executor executor = Executors.newSingleThreadExecutor();
                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                MyDatabase.getDatabase(context).taskDao().deleteTask(swipedTask);
                                                // обновляем список на основном потоке
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        taskList.remove(swipedTaskPosition);
                                                        adapter.notifyItemRemoved(swipedTaskPosition);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                itemTouchHelper.attachToRecyclerView(recyclerView);
            } else {
                MainActivity.adapter.notifyDataSetChanged();
            }
        }
    }
}