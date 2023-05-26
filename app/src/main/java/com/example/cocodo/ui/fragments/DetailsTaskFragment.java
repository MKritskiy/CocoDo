package com.example.cocodo.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.MainActivity;
import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.RecyclerSubTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.SubTask;
import com.example.cocodo.utils.TempAdapter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailsTaskFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    public interface OnFragmentButtonClickListener{
        void addTaskButtonClick(int taskId);

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

    private String taskName, description, deadline, priority;
    String[] monthNames = new DateFormatSymbols(new Locale("ru")).getShortMonths();

    private static List<SubTask> subTaskList;
    private static RecyclerView recyclerView;
    private static RecyclerSubTaskListAdapter adapter;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_details_fragment, container, false);
        Context context = getContext();
        taskId = getArguments().getInt("taskId");
        recyclerView = rootView.findViewById(R.id.recycler_view_details_subtasks);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PUCK", String.valueOf(MyDatabase.getDatabase(context).taskDao().getAllSubTasks(taskId).size()));
                subTaskList = MyDatabase.getDatabase(context).taskDao().getAllSubTasks(taskId);
                if (subTaskList.size()>0)
                {
                    adapter = new RecyclerSubTaskListAdapter(context, subTaskList);
                    recyclerView.setAdapter(adapter);
                }
                Log.d("TAG", "Sub-task list size: " + subTaskList.size()+ "taskId: "+ taskId);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG", "All logs printed");
                    }
                });
            }
        }).start();


        addButton = (Button) rootView.findViewById(R.id.button_details_add_subtask);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentButtonClickListener.addTaskButtonClick(getArguments().getInt("taskId"));
            }
        });
        return rootView;
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
