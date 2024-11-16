package com.example.androidproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "hongdroid.db";

    // @NonNull을 사용하여 Context가 null이 아니도록 강제
    public DBHelper(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS TodoList(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "alarmTime TEXT, " + // 알람 시간 (예: "14:30")
                "completionDate TEXT);"); // 일정 완료 날짜 (예: "2024-11-14")
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // 기존 테이블 삭제 후 새로운 테이블을 생성하는 방식으로 변경
            db.execSQL("DROP TABLE IF EXISTS TodoList");
            onCreate(db); // 새로운 테이블 생성
        }
    }

    // 특정 날짜에 맞는 Todo 리스트 가져오기
    public ArrayList<TodoItem> getTodoListForDate(String date) {
        ArrayList<TodoItem> todoItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // 특정 날짜에 해당하는 일정 가져오기 (completionDate와 일치하는 일정)
            String query = "SELECT * FROM TodoList WHERE completionDate = ?";
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    String alarmTime = cursor.getString(cursor.getColumnIndexOrThrow("alarmTime"));
                    String completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completionDate"));

                    TodoItem todoItem = new TodoItem();
                    todoItem.setId(id);
                    todoItem.setTitle(title);
                    todoItem.setContent(content);
                    todoItem.setAlarmTime(alarmTime);
                    todoItem.setCompletionDate(completionDate);
                    todoItems.add(todoItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBHelper", "Error while fetching todo list for date", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return todoItems;
    }

    // getTodoList 메소드에서 Cursor 자원 관리를 try-finally 블록으로 수정
    public ArrayList<TodoItem> getTodoList() {
        ArrayList<TodoItem> todoItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM TodoList ORDER BY completionDate DESC", null);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    String alarmTime = cursor.getString(cursor.getColumnIndexOrThrow("alarmTime"));
                    String completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completionDate"));

                    TodoItem todoItem = new TodoItem();
                    todoItem.setId(id);
                    todoItem.setTitle(title);
                    todoItem.setContent(content);
                    todoItem.setAlarmTime(alarmTime);
                    todoItem.setCompletionDate(completionDate);
                    todoItems.add(todoItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBHelper", "Error while fetching todo list", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // cursor 자원 반납
            }
        }
        return todoItems;
    }

    // InsertTodo 메소드에서 SQL 파라미터 바인딩을 직접 하지 않고 SQLiteStatement 사용
    public void InsertTodo(String _title, String _content, String _alarmTime, String _completionDate) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO TodoList (title, content, alarmTime, completionDate) VALUES(?, ?, ?, ?)";

        try {
            SQLiteStatement statement = db.compileStatement(sql);
            statement.bindString(1, _title);
            statement.bindString(2, _content);
            statement.bindString(3, _alarmTime);
            statement.bindString(4, _completionDate);
            statement.executeInsert();  // 삽입 실행
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBHelper", "InsertTodo error: " + e.getMessage());
        } finally {
            db.close();  // 데이터베이스 연결 종료
        }
    }

    // UpdateTodo 메소드에서 SQL 파라미터 바인딩을 직접 하지 않고 SQLiteStatement 사용
    public void UpdateTodo(String _title, String _content, String _alarmTime, String _completionDate, String _beforeCompletionDate) {
        // null 값 체크 후 빈 문자열로 처리
        if (_title == null) _title = "";
        if (_content == null) _content = "";
        if (_alarmTime == null) _alarmTime = "";
        if (_completionDate == null) _completionDate = "";

        SQLiteDatabase db = null;
        SQLiteStatement statement = null;
        try {
            db = getWritableDatabase();
            String sql = "UPDATE TodoList SET title=?, content=?, alarmTime=?, completionDate=? WHERE completionDate=?";
            statement = db.compileStatement(sql);
            statement.bindString(1, _title);
            statement.bindString(2, _content);
            statement.bindString(3, _alarmTime);
            statement.bindString(4, _completionDate);
            statement.bindString(5, _beforeCompletionDate);
            statement.executeUpdateDelete();  // 업데이트 실행
        } catch (Exception e) {
            e.printStackTrace();  // 예외 발생 시 출력
            Log.e("DBHelper", "Error while updating todo", e);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    // deleteTodoByDateAndTitle에서 삭제하려는 데이터 존재 여부 체크 추가
    public void deleteTodoByDateAndTitle(String _completionDate, String _title) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        try {
            // 삭제하려는 데이터가 존재하는지 확인
            String checkSql = "SELECT COUNT(*) FROM TodoList WHERE completionDate=? AND title=?";
            cursor = db.rawQuery(checkSql, new String[]{_completionDate, _title});
            cursor.moveToFirst();
            int count = cursor.getInt(0);

            if (count > 0) {
                // 데이터가 존재할 때만 삭제
                String deleteSql = "DELETE FROM TodoList WHERE completionDate=? AND title=?";
                db.execSQL(deleteSql, new Object[]{_completionDate, _title});
            } else {
                // 데이터가 존재하지 않으면 메시지나 로그를 출력할 수 있음
                Log.d("DBHelper", "No matching data found to delete.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBHelper", "Error while deleting todo", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
