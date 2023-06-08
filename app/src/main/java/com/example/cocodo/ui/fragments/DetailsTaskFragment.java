package com.example.cocodo.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
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
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.database.TaskDao;
import com.example.cocodo.utils.RecyclerSubTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.Task;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DetailsTaskFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private ItemTouchHelper itemCheckedTouchHelper;

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
    private TextView priority_textView, dateTextView, subtaskCountTextView;
    private Button deadlineButton, priorityButton, reminderButton, addButton, closeButton, detailsWrapButton;
    private LinearLayout priorityLayout, dateLayout, subtaskHeader, detailsSubTaskList;
    private CheckBox checkBox;
    ImageView priorityImage;

    int subTaskCheckedListSize, subTaskAllListSize;
    private int day, month, year, hour, minute;
    private static int taskId;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

    private String taskName, description, deadline, priority_text;
    String[] monthNames = new DateFormatSymbols(new Locale("ru")).getShortMonths();

    private static List<SubTask> subTaskList, subCheckedTaskList;
    private static Task currentTask;
    private static RecyclerView recyclerView, checkedRecView;
    public static RecyclerSubTaskListAdapter adapter, checkedAdapter;

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

    private TaskDao taskDao;
    ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_details_fragment, container, false);


        taskId = getArguments().getInt("taskId");
        Context context = getContext().getApplicationContext();
        recyclerView = rootView.findViewById(R.id.recycler_view_details_subtasks);
        taskNameEditText = rootView.findViewById(R.id.task_details_edit_text);
        descriptionEditText = rootView.findViewById(R.id.edit_details_desc);
        dateLayout = rootView.findViewById(R.id.task_details_date_layout);
        dateTextView = rootView.findViewById(R.id.text_details_date);
        checkBox = rootView.findViewById(R.id.task_completing);
        priorityImage = rootView.findViewById(R.id.priority_image);
        priority_textView = rootView.findViewById(R.id.text_details_priority);
        priorityLayout = (LinearLayout) rootView
                .findViewById(R.id.task_details_priority_layout);
        priorityButton = (Button) rootView.findViewById(R.id.details_priority_button);
        addButton = (Button) rootView.findViewById(R.id.button_details_add_subtask);
        subtaskHeader = rootView.findViewById(R.id.subtask_header);
        subtaskCountTextView = rootView.findViewById(R.id.subtask_count_text_view);
        checkedRecView = rootView.findViewById(R.id.recycler_view_details_checked_subtasks);
        detailsWrapButton = rootView.findViewById(R.id.details_wrap_button);
        detailsSubTaskList = rootView.findViewById(R.id.details_sub_task_list);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Получить список подзадач в другом потоке
                subTaskList = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getAllUncheckedSubTasks(taskId);

                subTaskAllListSize = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getAllSubTasks(taskId).size();
                subTaskCheckedListSize = subTaskAllListSize - subTaskList.size();
                currentTask = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getTaskById(taskId);
                adapter = new RecyclerSubTaskListAdapter(
                        context,
                        subTaskList,
                        recyclerView,
                        MyDatabase.getDatabase(context).taskDao(), subtaskCountTextView, subTaskAllListSize);
                recyclerView.setAdapter(adapter);
                subCheckedTaskList = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getAllCheckedSubTasks(taskId);
                checkedAdapter = new RecyclerSubTaskListAdapter(
                        context,
                        subCheckedTaskList,
                        checkedRecView,
                        MyDatabase.getDatabase(context).taskDao(), subtaskCountTextView, subTaskAllListSize);
                checkedRecView.setAdapter(checkedAdapter);

                taskDao = MyDatabase.getDatabase(context).taskDao();
                itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                        0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int swipedTaskPosition = viewHolder.getAdapterPosition();
                        SubTask swipedTask = subTaskList.get(swipedTaskPosition);

                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                MyDatabase.getDatabase(context).taskDao().deleteSubTask(swipedTask);
                                subTaskList = MyDatabase
                                        .getDatabase(context)
                                        .taskDao()
                                        .getAllUncheckedSubTasks(taskId);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyItemRemoved(swipedTaskPosition);
                                    }
                                });
                            }
                        });
                    }
                });
                itemCheckedTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                        0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int swipedTaskPosition = viewHolder.getAdapterPosition();
                        SubTask swipedTask = subCheckedTaskList.get(swipedTaskPosition);

                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                MyDatabase.getDatabase(context).taskDao().deleteSubTask(swipedTask);
                                subCheckedTaskList = MyDatabase
                                        .getDatabase(context)
                                        .taskDao()
                                        .getAllCheckedSubTasks(taskId);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkedAdapter.notifyItemRemoved(swipedTaskPosition);
                                    }
                                });
                            }
                        });

                    }
                });
            }
        });
        executorService.shutdown();

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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.addTaskButtonClick(getArguments().getInt("taskId"));
            }
        });
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme,DetailsTaskFragment.this,
                        year, month, day);
                datePickerDialog.show();
            }
        });
        detailsWrapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailsSubTaskList.getVisibility() == View.GONE) {
                    detailsSubTaskList.setVisibility(View.VISIBLE);
                    detailsWrapButton.setText("Свернуть");
                } else {
                    detailsSubTaskList.setVisibility(View.GONE);
                    detailsWrapButton.setText("Подробнее");
                }
            }
        });
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            downloadPriority();
            taskNameEditText.setText(currentTask.getTaskName());
            checkBox.setChecked(currentTask.getIsCompleted() > 0);
            if (currentTask.getTaskTime() != null)
                dateTextView.setText(currentTask.getTaskTime());
            if (currentTask.getTaskDesc() != null)
                descriptionEditText.setText(currentTask.getTaskDesc());
            if (subTaskAllListSize < 1)
                subtaskHeader.setVisibility(View.GONE);
            else
                subtaskHeader.setVisibility(View.VISIBLE);
            subtaskCountTextView.setText(subTaskCheckedListSize + "/" + subTaskAllListSize);
            taskDao.getAllSubTasksObservable(taskId).observe(this.getViewLifecycleOwner(), new Observer<List<SubTask>>() {
                @Override
                public void onChanged(List<SubTask> tasks) {
                    updateRecyclerView();
                }
            });
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
        Context context = getContext().getApplicationContext();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                subTaskList = MyDatabase.getDatabase(context).taskDao().getAllUncheckedSubTasks(taskId);
                subCheckedTaskList = MyDatabase.getDatabase(context).taskDao().getAllCheckedSubTasks(taskId);
                subTaskAllListSize = MyDatabase
                        .getDatabase(context)
                        .taskDao()
                        .getAllSubTasks(taskId).size();
                subTaskCheckedListSize = subTaskAllListSize - subTaskList.size();
            }
        });
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            adapter = new RecyclerSubTaskListAdapter(context, subTaskList, recyclerView, MyDatabase.getDatabase(context).taskDao(), subtaskCountTextView, subTaskAllListSize);
            checkedAdapter = new RecyclerSubTaskListAdapter(context, subCheckedTaskList, checkedRecView, MyDatabase.getDatabase(context).taskDao(), subtaskCountTextView, subTaskAllListSize);
            recyclerView.setAdapter(adapter);
            checkedRecView.setAdapter(checkedAdapter);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            itemCheckedTouchHelper.attachToRecyclerView(checkedRecView);

            subtaskCountTextView.setText(subTaskCheckedListSize + "/" + subTaskAllListSize);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
            if (recyclerView.getItemDecorationCount() < 1)
                recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
            if (checkedRecView.getItemDecorationCount() < 1)
                checkedRecView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        } catch (InterruptedException ignored) {
        }
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
                subTaskAllListSize++;
                if (subTaskList.size() > 0)
                    subtaskHeader.setVisibility(View.VISIBLE);
                subtaskCountTextView.setText(subTaskCheckedListSize + "/" + subTaskAllListSize);
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
                priorityImage.setImageResource(R.drawable.flag_red);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.coco_red)));
                break;
            case 2:
                priorityImage.setImageResource(R.drawable.flag_green);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.green)));
                break;
            case 3:
                priorityImage.setImageResource(R.drawable.flag_blue);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.blue)));
                break;
            default:
                priorityImage.setImageResource(R.drawable.flag_red);
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext().getApplicationContext(), R.color.black)));
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // Задаем обработчик события нажатия на системную кнопку "Назад"
        requireDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount() < 1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        if (checkedRecView.getItemDecorationCount() < 1)
            checkedRecView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("TAG", "onPause: ");

        currentTask.setIsCompleted(checkBox.isChecked() ? 1 : 0);
        currentTask.setTaskName(taskNameEditText.getText().toString());
        currentTask.setTaskDesc(descriptionEditText.getText().toString());
        currentTask.setTaskTime(dateTextView.getText().toString());


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (currentTask != null) {
                    MyDatabase.getDatabase(getContext()).taskDao().update(currentTask);
                    fragmentButtonClickListener.updateTaskRecView();
                }
            }
        });

        dismiss();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1 + 1;
        dayFinal = i2;

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        ContextThemeWrapper themedContext = new ContextThemeWrapper(getContext(), R.style.RedTimePickerTheme);
        TimePickerDialog timePickerDialog = new TimePickerDialog(themedContext, DetailsTaskFragment.this,
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
                (hourFinal < 10 ? "0" + hourFinal : hourFinal) + ":" +
                (minuteFinal < 10 ? "0" + minuteFinal : minuteFinal);
        dateTextView.setText(deadline);
    }

}
