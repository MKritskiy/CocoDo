package com.example.cocodo.utils;

import com.example.cocodo.database.TaskDao;

import java.sql.Date;
import java.time.ZoneId;
import java.util.List;

public class UnstopableCalculator {
    private int maxStreak = 0;
    private int currentStreak = 0;
    private Date startDate = null;
    private Date currentStartDate = null;

    public UnstopableCalculator calculate(TaskDao taskDao){
        List<Task> completedTasks = taskDao.getCompletedTasksSortedByDate();
        for (int i = 0; i < completedTasks.size(); i++) {
            Task task = completedTasks.get(i);
            Date completedAt = task.getCompletedAt();
            if (i == 0 || isConsecutive(completedTasks.get(i - 1), task)) {
                // Если текущая задача является продолжением пре��ыдущей, то увеличиваем текущую серию
                currentStreak++;
                if (currentStreak == 1) {
                    // Если текущая серия только началась, запоминаем ее начало
                    currentStartDate = completedAt;
                }
            } else {
                // Если текущая задача не является продолжением предыдущей, то начинаем новую серию
                if (currentStreak > maxStreak) {
                    // Если текущая серия оказалась больше всех предыдущих, запоминаем ее
                    maxStreak = currentStreak;
                    startDate = currentStartDate;
                }
                currentStreak = 1;
                currentStartDate = completedAt;
            }
        }
        if (currentStreak > maxStreak) {
            // Проверяем, не закончился ли списо�� задач максимальной серией
            maxStreak = currentStreak;
            startDate = currentStartDate;
        }
        return this;
    }
    private static boolean isConsecutive(Task prev, Task curr) {
        // Проверяем, является ли текущая задача пр��должением предыдущей
        return prev.getCompletedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1).equals(curr.getCompletedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getCurrentStartDate() {
        return currentStartDate;
    }

    public void setCurrentStartDate(Date currentStartDate) {
        this.currentStartDate = currentStartDate;
    }
}
