package com.example.cocodo.alarms;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cocodo.MainActivity;
import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskNotificationService extends Service {
    private static final String TAG = TaskNotificationService.class.getSimpleName();
    private static final long INTERVAL_MINUTES = 1;
    private static final long INTERVAL_MS = INTERVAL_MINUTES * 60 * 500;
    String channelId;
    List<Task> tasksDueWithin24Hours;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            checkTasksDueWithin24Hours();
            mHandler.postDelayed(mRunnable, INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler.postDelayed(mRunnable, INTERVAL_MS);
        channelId = "taskChanelId";
        CharSequence channelName = "TaskNotify";
        String channelDescription = "Notifications about tasks deadline";

        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkTasksDueWithin24Hours() {
        // Get tasks that are due within 24 hours
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long currentTimestamp = System.currentTimeMillis();
                long nextDayTimestamp = currentTimestamp + 24 * 60 * 60 * 1000;
                tasksDueWithin24Hours = MyDatabase.getDatabase(getApplicationContext()).taskDao().getTasksDueWithin24Hours(currentTimestamp, nextDayTimestamp);
            }
        });
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            // Create notification if tasks are due
            Log.i("TAG", tasksDueWithin24Hours.size() + "");

            if (!tasksDueWithin24Hours.isEmpty()) {
                String notificationTitle = "Почти всё на сегодня";
                String notificationText = pluralLeftTask(tasksDueWithin24Hours.size());

                // Создать объект Builder с использованием идентификатора канала уведомлений
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.bell)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                // Создайте интент для запуска активности при нажатии на уведомление
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                // Отображение уведомления
                int notificationId = 1;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                notificationManager.notify(notificationId, builder.build());
            }

            // Stop the service
            stopSelf();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    private String pluralLeftTask(int size) {
        String result;
        int lastDigit = size % 10;
        if (lastDigit == 1 && size != 11) {
            result = "Осталась " + size + " задача";
        } else if (lastDigit >= 2 && lastDigit <= 4 && (size < 10 || size > 20)) {
            result ="Осталось "+ size + " задачи";
        } else {
            result = "Осталось " + size + " задач";
        }
        return result;
    }
}