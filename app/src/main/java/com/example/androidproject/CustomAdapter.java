package com.example.androidproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private final ArrayList<TodoItem> mTodoItems;  // 할일 항목들을 저장하는 리스트
    private final Context mContext;  // 안드로이드 시스템과 상호작용할 Context 객체
    private final DBHelper mDBHelper;  // 데이터베이스 작업을 담당하는 DBHelper 객체

    // 생성자 선언
    public CustomAdapter(ArrayList<TodoItem> mTodoItems, Context mContext) {
        this.mTodoItems = mTodoItems;  // 할일 목록 데이터를 받아서 저장
        this.mContext = mContext;  // Context 객체를 받아서 저장
        mDBHelper = new DBHelper(mContext);  // DBHelper 객체 생성
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // RecyclerView 항목 레이아웃을 inflate하여 ViewHolder 객체 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // RecyclerView 항목에 데이터를 바인딩
        TodoItem todoItem = mTodoItems.get(position);

        // TextView에 데이터 설정
        holder.titleTextView.setText(todoItem.getTitle());
        holder.contentTextView.setText(todoItem.getContent());
        holder.dueDateTextView.setText(todoItem.getCompletionDate());
        holder.alarmTimeTextView.setText(todoItem.getAlarmTime());
    }

    @Override
    public int getItemCount() {
        return mTodoItems.size();  // 아이템의 개수를 반환
    }

    // ViewHolder 클래스 정의
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView contentTextView;
        private final TextView dueDateTextView;
        private final TextView alarmTimeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 뷰 요소 초기화
            titleTextView = itemView.findViewById(R.id.tv_title);
            contentTextView = itemView.findViewById(R.id.tv_content);
            dueDateTextView = itemView.findViewById(R.id.tv_due_date);
            alarmTimeTextView = itemView.findViewById(R.id.tv_alarm_time);

            // 아이템 클릭 시 수정 또는 삭제 옵션을 제공하는 다이얼로그 띄우기
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int curPos = getAdapterPosition();
                    TodoItem todoItem = mTodoItems.get(curPos);

                    String[] strChoiceItems = {"수정하기", "삭제하기"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("원하는 작업을 선택해주세요");
                    builder.setItems(strChoiceItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            if (position == 0) {
                                // 수정하기
                                showEditDialog(todoItem, curPos);  // 수정 다이얼로그 띄우기
                            } else if (position == 1) {
                                // 삭제하기
                                deleteTodoItem(todoItem, curPos);  // 할 일 삭제
                            }
                        }
                    });
                    builder.show();
                }
            });
        }

        // 수정 다이얼로그 띄우는 메서드
        private void showEditDialog(TodoItem todoItem, int curPos) {
            // 수정 다이얼로그 생성
            Dialog dialog = new Dialog(mContext, android.R.style.Theme_Material_Light_Dialog);
            dialog.setContentView(R.layout.dialog_edit);
            EditText et_title = dialog.findViewById(R.id.et_title);
            EditText et_content = dialog.findViewById(R.id.et_content);
            EditText et_completionDate = dialog.findViewById(R.id.et_due_date);  // 완료 날짜 입력 필드 추가
            Button btn_ok = dialog.findViewById(R.id.btn_ok);

            // 기존 제목, 내용, 완료 날짜로 다이얼로그 필드 초기화
            et_title.setText(todoItem.getTitle());
            et_content.setText(todoItem.getContent());
            et_completionDate.setText(todoItem.getCompletionDate());  // 기존 완료 날짜 표시

            // 수정된 제목, 내용, 완료 날짜를 저장하는 버튼 클릭 리스너
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 수정된 제목, 내용, 완료 날짜 받아오기
                    String newTitle = et_title.getText().toString();
                    String newContent = et_content.getText().toString();
                    String newCompletionDate = et_completionDate.getText().toString();  // 수정된 완료 날짜

                    // DB에 수정된 내용 업데이트
                    String beforeCompletionDate = todoItem.getCompletionDate(); // 기존 완료 날짜를 가져와서 업데이트 시 사용
                    String alarmTime = todoItem.getAlarmTime(); // 알람 시간은 변경하지 않음
                    mDBHelper.UpdateTodo(newTitle, newContent, alarmTime, newCompletionDate, beforeCompletionDate);

                    // UI 업데이트
                    todoItem.setTitle(newTitle);
                    todoItem.setContent(newContent);
                    todoItem.setCompletionDate(newCompletionDate);  // 완료 날짜 수정

                    // RecyclerView 갱신
                    notifyItemChanged(curPos);

                    // 다이얼로그 닫기
                    dialog.dismiss();
                    Toast.makeText(mContext, "목록 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            // 다이얼로그 보이기
            dialog.show();
        }

        // 할 일 삭제 메서드
        private void deleteTodoItem(TodoItem todoItem, int curPos) {
            // DB에서 삭제
            String completionDate = todoItem.getCompletionDate();
            mDBHelper.deleteTodoByDateAndTitle(completionDate, todoItem.getTitle());

            // UI에서 삭제
            mTodoItems.remove(curPos);
            notifyItemRemoved(curPos);

            // 사용자에게 삭제 완료 메시지
            Toast.makeText(mContext, "목록을 삭제했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 새로운 아이템을 추가하는 메서드
    public void addItem(TodoItem _item) {
        mTodoItems.add(0, _item);  // 리스트의 처음에 추가
        notifyItemInserted(0);  // 첫 번째 위치에 추가된 항목을 갱신
    }
}
