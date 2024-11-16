package com.example.androidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.androidproject.CalendarView;
import com.example.androidproject.ListView;
import com.example.androidproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BottomNavigationView 초기화
        BottomNavigationView mBottomNavigationView = findViewById(R.id.bottom_navigation);

        // 앱 실행 시 기본으로 보여줄 화면은 ListView
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, new ListView()) // ListView를 기본 화면으로 설정
                    .commit();
        }

        // BottomNavigation 아이템 클릭 리스너 설정
        mBottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_list) {
                    // 기존 Fragment 제거 후 새 ListView Fragment로 교체
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_container, new ListView())  // ListView Fragment 교체
                            .addToBackStack(null)                           // 백스택에 추가 (뒤로가기 버튼 사용 가능)
                            .commit();

                } else if (itemId == R.id.nav_calendar) {
                    // 기존 Fragment 제거 후 새 CalendarView Fragment로 교체
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_container, new CalendarView())  // CalendarView Fragment 교체
                            .addToBackStack(null)                                // 백스택에 추가 (뒤로가기 버튼 사용 가능)
                            .commit();
                }
                return true;
            }
        });
    }
}