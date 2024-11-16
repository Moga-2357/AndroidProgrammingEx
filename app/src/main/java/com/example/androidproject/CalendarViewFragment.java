package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarViewFragment extends Fragment
{

    private CalendarView mCalendarView;
    private RecyclerView mRecyclerView;
    private ScheduleAdapter mAdapter;
    private ArrayList<ScheduleItem> mScheduleList;
    private DBHelper mDBHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendarview, container, false);

        mCalendarView = view.findViewById(R.id.calendarView);
        mRecyclerView = view.findViewById(R.id.rv_schedules);

        // RecyclerView 설정
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mScheduleList = new ArrayList<>();
        mAdapter = new ScheduleAdapter(mScheduleList);
        mRecyclerView.setAdapter(mAdapter);

        // DBHelper 초기화
        mDBHelper = new DBHelper(getContext());

        // CalendarView에서 날짜를 선택했을 때
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // 선택된 날짜 포맷: yyyy-MM-dd
                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                loadSchedulesForDate(selectedDate);
            }
        });

        return view;
    }

    // DB에서 선택된 날짜에 맞는 일정을 로드하는 메서드
    private void loadSchedulesForDate(String selectedDate) {
        // DBHelper에서 해당 날짜에 맞는 일정을 가져오기
        ArrayList<TodoItem> todoItems = mDBHelper.getTodoListForDate(selectedDate);

        // 일정 목록이 비어 있지 않으면 RecyclerView에 추가
        if (todoItems != null && !todoItems.isEmpty()) {
            mScheduleList.clear();
            for (TodoItem item : todoItems) {
                // 할 일 목록에서 일정 항목을 만들어서 추가
                mScheduleList.add(new ScheduleItem(item.getTitle(), item.getCompletionDate()));
            }
            mAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
        } else {
            // 데이터가 없으면 메시지 표시
            Toast.makeText(getContext(), "선택한 날짜에 일정이 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // 선택된 날짜에 대한 알림
        Toast.makeText(getContext(), selectedDate + "의 일정을 표시합니다.", Toast.LENGTH_SHORT).show();
    }
}
