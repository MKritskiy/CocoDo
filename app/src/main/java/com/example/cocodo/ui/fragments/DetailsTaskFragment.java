package com.example.cocodo.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.RecyclerSubTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.SubTask;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class DetailsTaskFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    public interface OnFragmentButtonClickListener{
        void addTaskButtonClick(int taskId);
        void priorityButtonClick(View view);
    }

    private DetailsTaskFragment.OnFragmentButtonClickListener fragmentButtonClickListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            fragmentButtonClickListener = (DetailsTaskFragment.OnFragmentButtonClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }
    }



    private EditText taskNameEditText, descriptionEditText;
    private Button deadlineButton, priorityButton, reminderButton, addButton, closeButton;
    private int day, month, year, hour, minute;
    private static int taskId;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

    private String taskName, description, deadline, priority_text;
    String[] monthNames = new DateFormatSymbols(new Locale("ru")).getShortMonths();

    private static List<SubTask> subTaskList;
    private static RecyclerView recyclerView;
    public static RecyclerSubTaskListAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.task_details_fragment);
        Window window = dialog.getWindow();

        if (window != null) {
            // Устанавливаем фон фрагмента прозрачным, чтобы была видна нижняя часть экрана
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Устанавливаем анимацию для плавного выплывания
//            window.setWindowAnimations(R.style.DialogAnimation);
            // Устанавливаем параметры для расположения фрагмента внизу экрана
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        return dialog;
    }
//    public static class MyWorker extends Worker {
//
//        private int taskId;
//
//        public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//            super(context, workerParams);
//            this.taskId = getInputData().getInt("taskId", 0);
//        }
//
//        @NonNull
//        @Override
//        public Result doWork() {
//            // Получение списка непроверенных задач
//            List<SubTask> subTaskList = MyDatabase.getDatabase(getApplicationContext())
//                    .taskDao()
//                    .getAllUncheckedSubTasks(taskId);
//            adapter = new RecyclerSubTaskListAdapter(getApplicationContext(), subTaskList, recyclerView, MyDatabase.getDatabase(getApplicationContext()).taskDao());
//
//            // Результат выполнения задачи
//            return Result.success();
//        }
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_details_fragment, container, false);
        taskId = getArguments().getInt("taskId");
        recyclerView = rootView.findViewById(R.id.recycler_view_details_subtasks);
        Log.d("TAG", String.valueOf(taskId));
// Создать экземпляр WorkerParameters


//        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .setInputData(new Data.Builder().putInt("taskId", taskId).build())
//                .build();
//        WorkManager.getInstance(getContext().getApplicationContext()).enqueue(workRequest);
//
//        UUID workRequestId = workRequest.getId();
//        LiveData<WorkInfo> workInfoLiveData = WorkManager.getInstance(getContext().getApplicationContext()).getWorkInfoByIdLiveData(workRequestId);
//        workInfoLiveData.observe(this, workInfo -> {
//            if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
//                recyclerView.setAdapter(adapter);
//            }
//        });

        LinearLayout priorityLayout = (LinearLayout) rootView
                .findViewById(R.id.task_details_priority_layout);
        priorityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentButtonClickListener.priorityButtonClick(rootView);
            }
        });
        priorityButton = (Button) rootView.findViewById(R.id.details_priority_button);
        priorityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.priorityButtonClick(rootView);

            }
        });

        addButton = (Button) rootView.findViewById(R.id.button_details_add_subtask);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.addTaskButtonClick(getArguments().getInt("taskId"));
            }
        });
        return rootView;
    }

    public void updateRecyclerView(){
        Context context = getContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                    subTaskList = MyDatabase.getDatabase(context).taskDao().getAllUncheckedSubTasks(taskId);
                    adapter = new RecyclerSubTaskListAdapter(context, subTaskList, recyclerView, MyDatabase.getDatabase(context).taskDao());
                    recyclerView.setAdapter(adapter);

            }
        }).start();
    }
    public void addSubTask(int taskId, String subTaskName, String subTaskDescription, String subTaskTime){
        if (adapter==null){
            updateRecyclerView();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SubTask newSubTask = new SubTask(
                        taskId,
                        subTaskName,
                        subTaskDescription,
                        subTaskTime);
                MyDatabase.getDatabase(getContext()).taskDao().
                        insertSubTask(newSubTask);
                subTaskList.add(newSubTask);
            }
        }).start();
        adapter.notifyDataSetChanged();
    }

    public void setPriority(int priority) {
        TextView text_priority = getView().findViewById(R.id.text_details_priority);
        text_priority.setText("Приоритет " + String.valueOf(priority));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Задаем обработчик события нажатия на системную кнопку "Назад"
        requireDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // Удаляем фрагмент и затемненный фон
                    dismiss();
                    getParentFragmentManager().beginTransaction().remove(getParentFragmentManager().findFragmentById(android.R.id.content)).commit();
                    return true;
                }
                return false;
            }
        });
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount()<1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
//        adapter.notifyDataSetChanged();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i("TAG", "onPause: ");
        dismiss();
        getParentFragmentManager().beginTransaction().remove(getParentFragmentManager().findFragmentById(android.R.id.content)).commit();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1+1;
        dayFinal = i2;

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), DetailsTaskFragment.this,
                hour, minute, DateFormat.is24HourFormat(getContext()));
        timePickerDialog.show();
    }
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        hourFinal = i;
        minuteFinal = i1;
        deadline = dayFinal + " " +
                monthNames[monthFinal-1] +" " +
                yearFinal + " "+
                hourFinal+ ":" +
                minuteFinal;
        deadlineButton.setText(deadline);
    }

}
