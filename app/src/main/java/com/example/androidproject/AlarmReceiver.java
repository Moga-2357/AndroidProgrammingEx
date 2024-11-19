package com.example.androidproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("MyAlarmReceiver", "알람이 발생했습니다!");

        // 알람이 울렸을 때 수행할 작업 (예: 알림 띄우기)
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        // 알림을 띄우는 코드
        showNotification(context, title, content);
    }

    // 알림을 표시하는 메서드
    private void showNotification(Context context, String title, String content) {
        // 알림 채널 ID
        String channelId = "todo_channel";

        // 알림 빌더 설정
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_alarm)  // 알림 아이콘 설정
                .setAutoCancel(true)  // 알림을 클릭하면 자동으로 사라지게 설정
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 기본 우선순위 설정
                .build();

        // 알림 관리자 객체를 가져와서 알림을 표시
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notification);  // 알림 ID 0번으로 표시
        }
    }
}
