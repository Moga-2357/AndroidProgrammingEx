package com.example.androidproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();

        // 알림 채널 생성 (안드로이드 8.0 이상에서만 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "todo_channel",  // 채널 ID
                    "Todo App Notifications",  // 채널 이름
                    NotificationManager.IMPORTANCE_DEFAULT);  // 채널 중요도

            // 알림 관리자 객체를 가져와서 채널을 시스템에 등록
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
