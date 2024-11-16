package com.example.androidproject;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

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

    private void loadTodoListFromDB() {
        // DBHelper에서 할 일 목록 가져오기
        mTodoItems = mDBHelper.getTodoList();

        // RecyclerView와 어댑터에 데이터를 설정
        mAdapter = new CustomAdapter(mTodoItems, getContext());
        mRv_todo.setAdapter(mAdapter);
    }
}
