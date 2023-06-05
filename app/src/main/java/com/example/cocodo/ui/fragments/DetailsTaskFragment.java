package com.example.cocodo.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.RecyclerSubTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DetailsTaskFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    public interface OnFragmentButtonClickListener {
        void addTaskButtonClick(int taskId);

        void priorityButtonClick(View view);

        void updateTaskRecView();
    }

    private DetailsTaskFragment.OnFragmentButtonClickListener fragmentButtonClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentButtonClickListener = (DetailsTaskFragment.OnFragmentButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }
    }


    private EditText taskNameEditText, descriptionEditText;
    private TextView priority_textView, date_textView;
    private Button deadlineButton, priorityButton, reminderButton, addButton, closeButton;
    private LinearLayout priorityLayout;
    private CheckBox checkBox;
    ImageView priority_image;

    private int day, month, year, hour, minute;
    private static int taskId;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

    private String taskName, description, deadline, priority_text;
    String[] monthNames = new DateFormatSymbols(new Locale("ru")).getShortMonths();

    private static List<SubTask> subTaskList;
    private static Task currentTask;
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
            window.setWindowAnimations(R.style.DialogAnimation);
            // Устанавливаем параметры для расположения фрагмента внизу экрана
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_details_fragment, container, false);
        taskId = getArguments().getInt("taskId");
        recyclerView = rootView.findViewById(R.id.recycler_view_details_subtasks);
        Log.d("TAG", String.valueOf(taskId));
        Context context = getContext().getApplicationContext();
        taskNameEditText = rootView.findViewById(R.id.task_details_edit_text);
        date_textView = rootView.findViewById(R.id.text_details_date);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        checkBox = rootView.findViewById(R.id.task_completing);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Получить список подзадач в другом потоке
                subTaskList = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getAllUncheckedSubTasks(taskId);
                currentTask = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getTaskById(taskId);
                Log.d("TAG", currentTask.getTaskName());
                adapter = new RecyclerSubTaskListAdapter(
                        context,
                        subTaskList,
                        recyclerView,
                        MyDatabase.getDatabase(context).taskDao());
                recyclerView.setAdapter(adapter);
            }
        });
        executorService.shutdown();

        priority_image = rootView.findViewById(R.id.priority_image);
        priority_textView = rootView.findViewById(R.id.text_details_priority);
        checkBox = rootView.findViewById(R.id.task_completing);
        priorityLayout = (LinearLayout) rootView
                .findViewById(R.id.task_details_priority_layout);
        Handler handler = new Handler();
        priorityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (priorityLayout.isClickable()) {
                    priorityLayout.setClickable(false);
                    fragmentButtonClickListener.priorityButtonClick(rootView);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            priorityLayout.setClickable(true);
                        }
                    }, 200);
                }
            }
        });
        priorityButton = (Button) rootView.findViewById(R.id.details_priority_button);

        priorityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (priorityLayout.isClickable()) {
                    priorityLayout.setClickable(false);
                    fragmentButtonClickListener.priorityButtonClick(rootView);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            priorityLayout.setClickable(true);
                        }
                    }, 200);
                }
            }
        });

        addButton = (Button) rootView.findViewById(R.id.button_details_add_subtask);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.addTaskButtonClick(getArguments().getInt("taskId"));
            }
        });
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            downloadPriority();
            taskNameEditText.setText(currentTask.getTaskName());
            if (currentTask.getTaskTime()!=null)
                date_textView.setText(currentTask.getTaskTime());
            else {
                LinearLayout dateLayout = rootView.findViewById(R.id.task_details_date_layout);
                dateLayout.setVisibility(View.GONE);
            }
        } catch (InterruptedException ignored) {
        }
        return rootView;
    }

    private void downloadPriority() {
        int taskPriority = currentTask.getTaskPriority();
        priority_textView.setText("Приоритет " + String.valueOf(taskPriority));
        if (taskPriority < 4 && taskPriority > 0) {
            priorityButton.setVisibility(View.GONE);
            priorityLayout.setVisibility(View.VISIBLE);
            setPriorityImage(taskPriority);
        } else {
            priorityButton.setVisibility(View.VISIBLE);
            priorityLayout.setVisibility(View.GONE);
            setPriorityImage(taskPriority);
        }

    }

    public void updateRecyclerView() {
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

    public void addSubTask(int taskId, String subTaskName, String subTaskDescription, String subTaskTime) {
        if (adapter == null) {
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
        currentTask.setTaskPriority(priority);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                MyDatabase.getDatabase(getContext().getApplicationContext()).taskDao().update(currentTask);
            }
        });
        executorService.shutdown();
        downloadPriority();
    }

    private void setPriorityImage(int priority) {
        switch (priority) {
            case 1:
                priority_image.setImageResource(R.drawable.flag_red);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.coco_red)));
                break;
            case 2:
                priority_image.setImageResource(R.drawable.flag_green);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.green)));
                break;
            case 3:
                priority_image.setImageResource(R.drawable.flag_blue);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.blue)));
                break;
            default:
                priority_image.setImageResource(R.drawable.flag_red);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.black)));

                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Задаем обработчик события нажатия на системную кнопку "Назад"

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount() < 1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("TAG", "onPause: ");
        if (taskNameEditText.getText().toString()!=currentTask.getTaskName()){
            currentTask.setTaskName(taskNameEditText.getText().toString());
            currentTask.setIsCompleted(checkBox.isChecked()?1:0);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    MyDatabase.getDatabase(getContext().getApplicationContext()).taskDao().update(currentTask);
                }
            });
        }
        fragmentButtonClickListener.updateTaskRecView();
        dismiss();
        getParentFragmentManager().beginTransaction().remove(getParentFragmentManager().findFragmentById(android.R.id.content)).commit();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1 + 1;
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
                monthNames[monthFinal - 1] + " " +
                yearFinal + " " +
                hourFinal + ":" +
                minuteFinal;
        deadlineButton.setText(deadline);
    }

}
