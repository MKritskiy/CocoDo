package com.example.cocodo.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.UnstoppableCalculator;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProductivityFragment extends Fragment {


    public interface HistoryButtonClickListener {
        void onHistoryButtonClick();
    }

    private ProductivityFragment.HistoryButtonClickListener historyButtonClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            historyButtonClickListener = (ProductivityFragment.HistoryButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }
    }
    private TextView unstoppableCounter, unstoppableDates, historyCheckedTextView;
    private int currentStreak = 1;
    private Date startDate = null;
    private Date endDate = null;
    private int checkedCount = 0;
    ConstraintLayout historyLayout;

    ImageButton productivityBackImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.productivity_fragment, container, false);
        Context context = getContext().getApplicationContext();
        unstoppableCounter = view.findViewById(R.id.unstoppable_counter);
        unstoppableDates = view.findViewById(R.id.unstoppable_dates);
        historyLayout = view.findViewById(R.id.history_layout);
        historyCheckedTextView = view.findViewById(R.id.history_checked_textView);
        productivityBackImageButton = view.findViewById(R.id.productivity_back_image_button);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                UnstoppableCalculator calculator = new UnstoppableCalculator()
                        .calculate(MyDatabase.getDatabase(context).taskDao());
                checkedCount = MyDatabase.getDatabase(context).taskDao().getCompletedTasksSortedByDate().size();
                currentStreak = calculator.getCurrentStreak();
                startDate = calculator.getStartDate();
                endDate = calculator.getEndDate();
            }
        });
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            unstoppableCounter.setText(pluralDays(currentStreak));
            unstoppableDates.setText(formatLongestStreak(startDate, endDate));
            historyCheckedTextView.setText(pluralCheckedTasks(checkedCount));
        } catch (InterruptedException ignored) {
        }

        historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyButtonClickListener.onHistoryButtonClick();
            }
        });
        productivityBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }

    private String pluralDays(int days) {
        String result;
        int lastDigit = days % 10;
        if (lastDigit == 1 && days != 11) {
            result = days + " день";
        } else if (lastDigit >= 2 && lastDigit <= 4 && (days < 10 || days > 20)) {
            result = days + " дня";
        } else {
            result = days + " дней";
        }
        return result;
    }
    private String pluralCheckedTasks(int checkedCount) {
        String result;
        int lastDigit = checkedCount % 10;
        if (lastDigit == 1 && checkedCount != 11) {
            result = checkedCount + " выполненная задача";
        } else if (lastDigit >= 2 && lastDigit <= 4 && (checkedCount < 10 || checkedCount > 20)) {
            result = checkedCount + " выполненные задачи";
        } else {
            result = checkedCount + " выполненных задач";
        }
        return result;
    }
    private String formatLongestStreak(Date startDate, Date endDate) {
        if (endDate==null || startDate==null){
            return "Самое долгое: " + " " + pluralDays(0);
        }
        long diff = endDate.getTime() - startDate.getTime();
        int days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (days == 0)
            days = 1;
        String dateStringStart = LocalDate.parse(startDate.toString())
                .format(DateTimeFormatter
                        .ofPattern("dd MMMM yyyy", new Locale("ru")));
        String dateStringEnd = LocalDate.parse(endDate.toString())
                .format(DateTimeFormatter
                        .ofPattern("dd MMMM yyyy", new Locale("ru")));
        String result = "Самое долгое: " + " " + pluralDays(days) + " ("
                + dateStringStart + " - " + dateStringEnd + ")";
        return result;
    }
}
