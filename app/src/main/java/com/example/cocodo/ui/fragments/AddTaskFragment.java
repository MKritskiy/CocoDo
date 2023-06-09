package com.example.cocodo.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
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
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTaskFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    public interface OnFragmentButtonClickListener{
        void sendTaskButtonClick(String taskName, String taskDescription, String taskTime, int taskPriority);
        void sendSubTaskButtonClick(int taskId, String subTaskName, String subTaskDescription, String subTaskTime);
        void priorityButtonClick(View view);
    }


    private OnFragmentButtonClickListener fragmentButtonClickListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            fragmentButtonClickListener = (OnFragmentButtonClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }

    }
    private EditText taskNameEditText, descriptionEditText;
    private Button deadlineButton, priorityButton, reminderButton, sendButton;
    private int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;
    int taskPriority=4;
    private String taskName, description, deadline;
    String[] monthNames = new DateFormatSymbols(new Locale("ru")).getShortMonths();
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_task_fragment);
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
        View rootView = inflater.inflate(R.layout.add_task_fragment, container, false);
        taskNameEditText = rootView.findViewById(R.id.task_name_edit_text);
        descriptionEditText = rootView.findViewById(R.id.description_edit_text);
        deadlineButton = rootView.findViewById(R.id.deadline_button);
        priorityButton = rootView.findViewById(R.id.priority_button);
        sendButton = rootView.findViewById(R.id.send_button);
        boolean isSubTask = getArguments().getBoolean("isSubTask");
        deadlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme,AddTaskFragment.this,
                        year, month, day);
                datePickerDialog.show();
            }
        });
        Handler handler = new Handler();
        priorityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (priorityButton.isClickable()) {
                    priorityButton.setClickable(false);
                    fragmentButtonClickListener.priorityButtonClick(rootView);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            priorityButton.setClickable(true);
                        }
                    }, 200);
                }
            }
        });
        // Устанавливаем слушатель на кнопку "Отправить"
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия кнопки "Отправить"
                taskName = taskNameEditText.getText().toString();
                description = descriptionEditText.getText().toString();
                boolean hasReminder = false; // Пока не реализовано
                if (!taskNameEditText.getText().toString().isEmpty()){
                    if (!isSubTask)
                        fragmentButtonClickListener.sendTaskButtonClick(taskName, description, deadline, taskPriority);
                    else
                        fragmentButtonClickListener.sendSubTaskButtonClick(getArguments().getInt("taskId"), taskName, description, deadline);
                    onPause();
                }
            }
        });

        return rootView;
    }

    public void setPriority(int priority) {
        priorityButton.setText("Приоритет "+ priority);
        setPriorityImage(priority);
        taskPriority=priority;
    }
    private void setPriorityImage(int priority) {
        Drawable newDrawable;
        switch (priority) {
            case 1:
                newDrawable = getResources().getDrawable(R.drawable.flag_red); // вставьте свой ресурс с изображением вместо new_image
                priorityButton.setCompoundDrawablesWithIntrinsicBounds(newDrawable, null, null, null);
                break;
            case 2:
                newDrawable = getResources().getDrawable(R.drawable.flag_green); // вставьте свой ресурс с изображением вместо new_image
                priorityButton.setCompoundDrawablesWithIntrinsicBounds(newDrawable, null, null, null);
                break;
            case 3:
                newDrawable = getResources().getDrawable(R.drawable.flag_blue); // вставьте свой ресурс с изображением вместо new_image
                priorityButton.setCompoundDrawablesWithIntrinsicBounds(newDrawable, null, null, null);
                break;
            default:
                priorityButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                priorityButton.setText("Приоритет");
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
                    // Удаляем фрагмент и затемненный фон
                    dismiss();
                    getParentFragmentManager().beginTransaction().remove(getParentFragmentManager().findFragmentById(android.R.id.content)).commit();
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();

        dismiss();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        yearFinal = i;
        monthFinal = i1+1;
        dayFinal = i2;

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        ContextThemeWrapper themedContext = new ContextThemeWrapper(getContext(), R.style.RedTimePickerTheme);
        TimePickerDialog timePickerDialog = new TimePickerDialog(themedContext, AddTaskFragment.this,
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
                (hourFinal < 10 ? "0" + hourFinal : hourFinal) + ":" +
                (minuteFinal < 10 ? "0" + minuteFinal : minuteFinal);
        deadlineButton.setText(deadline);
    }

}
