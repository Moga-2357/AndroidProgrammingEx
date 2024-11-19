package com.example.androidproject;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class ListView extends Fragment
{
    private RecyclerView mRv_todo;
    private CustomAdapter mAdapter;
    private DBHelper mDBHelper;
    private ArrayList<TodoItem> mTodoItems;
    private FloatingActionButton mBtn_addwirte;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview, container, false);
        setInit(view); // View를 전달하며 초기화

        // 할 일 목록을 DB에서 로드하여 RecyclerView에 표시
        loadTodoListFromDB();

        return view;
    }

    private void setInit(View view) {
        // DB 객체 생성
        mDBHelper = new DBHelper(getContext());

        // RecyclerView 초기화
        mRv_todo = view.findViewById(R.id.rv_todo);
        mRv_todo.setLayoutManager(new LinearLayoutManager(getContext()));  // LayoutManager 설정

        // 어댑터 초기화
        mTodoItems = new ArrayList<>();
        mAdapter = new CustomAdapter(mTodoItems, getContext());  // 어댑터 생성
        mRv_todo.setAdapter(mAdapter);  // RecyclerView에 어댑터 연결

        // FloatingActionButton 초기화 (추가 버튼)
        mBtn_addwirte = view.findViewById(R.id.btn_addwirte);

        // 추가 버튼 클릭 시 다이얼로그 표시
        mBtn_addwirte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTodoDialog();
            }
        });
    }

    // 할 일 추가를 위한 다이얼로그를 생성하는 메서드
    private void showAddTodoDialog() {
        if (getContext() == null) {
            // Context가 null인 경우 처리
            return;
        }

        // 다이얼로그 생성
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Material_Light_Dialog);
        dialog.setContentView(R.layout.dialog_edit);  // 할 일 추가 레이아웃

        EditText et_title = dialog.findViewById(R.id.et_title);
        EditText et_content = dialog.findViewById(R.id.et_content);
        EditText et_due_date = dialog.findViewById(R.id.et_due_date);
        EditText et_alarm_time = dialog.findViewById(R.id.et_alarm_time);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);

        // 완료 날짜 클릭 시 날짜 선택 다이얼로그 표시
        et_due_date.setFocusable(false);
        et_due_date.setClickable(true);
        et_due_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(et_due_date);
            }
        });

        // 알람 시간 클릭 시 시간 선택 다이얼로그 표시
        et_alarm_time.setFocusable(false);
        et_alarm_time.setClickable(true);
        et_alarm_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(et_alarm_time);
            }
        });



        // 다이얼로그에서 확인 버튼을 눌렀을 때
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 제목, 내용, 완료 날짜, 알람 시간 입력 받기
                    String title = et_title.getText().toString();
                    String content = et_content.getText().toString();
                    String dueDate = et_due_date.getText().toString();  // 완료 날짜
                    String alarmTime = et_alarm_time.getText().toString();  // 알람 시간

                    // 비어있는 입력값을 빈 문자열로 처리 (빈 문자열이 DB에 들어가도 무방)
                    // 제목이 비어 있으면 알림 메시지 표시
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;  // 입력이 없으면 다이얼로그 종료
                    }
                    if (content.isEmpty()) content = "";
                    if (dueDate.isEmpty()) dueDate = "";
                    if (alarmTime.isEmpty()) alarmTime = "";

                    // DB에 할 일 추가 (완료 날짜와 알람 시간 포함)
                    mDBHelper.InsertTodo(title, content, alarmTime, dueDate);

                    // TodoItem 객체 생성
                    TodoItem item = new TodoItem();
                    item.setTitle(title);
                    item.setContent(content);
                    item.setAlarmTime(alarmTime);  // 알람 시간 설정
                    item.setCompletionDate(dueDate);  // 완료 날짜 설정

                    // 어댑터를 통해 RecyclerView에 새 항목 추가
                    mAdapter.addItem(item);
                    mRv_todo.smoothScrollToPosition(0);  // 새로 추가된 항목으로 스크롤

                    // 알람 설정 호출 (알람 시간, 제목, 내용)
                    setAlarm(alarmTime, title, content);

                    // 다이얼로그 닫기
                    dialog.dismiss();

                    // 사용자에게 추가 완료 메시지
                    Toast.makeText(getContext(), "할 일 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "문제가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 다이얼로그 보이기
        dialog.show();
    }

    // 날짜 선택 다이얼로그 표시
    private void showDatePickerDialog(EditText et_due_date) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 날짜 형식: YYYY-MM-DD
                    String formattedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    et_due_date.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    // 시간 선택 다이얼로그 표시
    private void showTimePickerDialog(EditText et_alarm_time) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    // 시간 형식: HH:MM
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    et_alarm_time.setText(formattedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    // 시간 선택 다이얼로그 표시
    private void setAlarm(String alarmTime, String title, String content) {
        try {
            // 알람 시간 출력
            Log.d("AlarmManager", "Setting alarm with time: " + alarmTime);

            // 알람 시간이 null이거나 비어있는 경우 체크
            if (alarmTime == null || alarmTime.isEmpty()) {
                Log.e("AlarmManager", "Invalid alarm time: " + alarmTime);
                Toast.makeText(getContext(), "알람 시간이 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 시간 파싱
            String[] timeParts = alarmTime.split(":");
            if (timeParts.length != 2) {
                Log.e("AlarmManager", "Invalid time format: " + alarmTime);
                Toast.makeText(getContext(), "알람 시간이 잘못된 형식입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Log.d("AlarmManager", "Parsed time: hour=" + hour + ", minute=" + minute);

            // 알람 시간 설정
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 알람 시간 이전일 경우, 다음 날로 설정
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);  // 다음 날로 설정
                Log.d("AlarmManager", "Alarm time was in the past, setting to next day.");
            }

            // 알람 트리거 시간 확인
            Log.d("AlarmManager", "Alarm is set for: " + calendar.getTime().toString());

            // Intent 설정
            Intent intent = new Intent(getContext(), AlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);

            // PendingIntent 생성
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    0,  // requestCode
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            // AlarmManager 설정
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                long triggerAtMillis = calendar.getTimeInMillis();
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                Log.d("AlarmManager", "Alarm set for: " + triggerAtMillis);
            } else {
                Log.d("AlarmManager", "AlarmManager is null!");
                Toast.makeText(getContext(), "알람 설정에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // 예외 발생 시, 어떤 예외가 발생했는지 로그에 남깁니다.
            Log.e("AlarmManager", "Error while setting alarm", e);
            Toast.makeText(getContext(), "알람 설정에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadTodoListFromDB() {
        // DBHelper에서 할 일 목록 가져오기
        mTodoItems = mDBHelper.getTodoList();

        // RecyclerView와 어댑터에 데이터를 설정
        mAdapter = new CustomAdapter(mTodoItems, getContext());
        mRv_todo.setAdapter(mAdapter);
    }
}
