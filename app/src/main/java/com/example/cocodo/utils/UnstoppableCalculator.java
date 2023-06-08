package com.example.cocodo.utils;

import com.example.cocodo.database.TaskDao;

import java.sql.Date;
import java.time.ZoneId;
import java.util.List;

public class UnstoppableCalculator {
    private int maxStreak = 0;
    private int currentStreak = 0;
    private Date startDate = null;
    private Date endDate = null;
    private Date currentStartDate = null;
    private Date currentEndDate = null;
    public UnstoppableCalculator calculate(TaskDao taskDao) {
        List<Task> completedTasks = taskDao.getCompletedTasksSortedByDate();
        for (int i = 0; i < completedTasks.size(); i++) {
            Task task = completedTasks.get(i);
            Date completedAt = task.getCompletedAt();
            if (i == 0 || isConsecutive(completedTasks.get(i - 1), task)) {
                // Если текущая задача является продолжением предыдущей, то увеличиваем текущую серию
                currentStreak++;
                if (currentStreak == 1) {
                    // Если текущая серия только началась, запоминаем ее начало
                    currentStartDate = completedAt;
                }
                currentEndDate = completedAt;
            } else {
                // Если текущая задача не является продолжением предыдущей, то начинаем новую серию
                if (currentStreak > maxStreak) {
                    // Если текущая серия оказалась больше всех предыдущих, запоминаем ее
                    maxStreak = currentStreak;
                    startDate = currentStartDate;
                    endDate = currentEndDate;
                }
                currentStreak = 1;
                currentStartDate = completedAt;
                currentEndDate = completedAt;
            }
        }
        if (currentStreak > maxStreak) {
            // Проверяем, не закончился ли список задач максимальной серией
            maxStreak = currentStreak;
            startDate = currentStartDate;
            endDate = currentEndDate;
        }
        return this;
    }
    private static boolean isConsecutive(Task prev, Task curr) {
        // Проверяем, является ли текущая задача продолжением предыдущей
        return prev
                .getCompletedAt()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(1)
                .equals(curr
                        .getCompletedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
    }
    public int getMaxStreak() {
        return maxStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
