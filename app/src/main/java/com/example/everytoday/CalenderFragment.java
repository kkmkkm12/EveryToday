package com.example.everytoday;

import static com.example.everytoday.DBOpenHelper.ACHIEVED;
import static com.example.everytoday.DBOpenHelper.DATE;
import static com.example.everytoday.DBOpenHelper.GOAL;
import static com.example.everytoday.DBOpenHelper.TABLE_NAME;
import static com.example.everytoday.DBOpenHelper._ID;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalenderFragment extends Fragment {
    private CalendarView calendarView;
    private ListView achievedGoalListView;
    private ListView noAchievedGoalListView;
    private AchievedAdapter achievedAdapter;
    private NoAchievedAdapter noAchievedAdapter;
    private TextView calendarPercent;

    private static final String DB_NAME = "MyDB";
    private static final int DB_VERSION = 1;
    private DBOpenHelper openHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        openHelper = new DBOpenHelper(getActivity(), DB_NAME, null, DB_VERSION);

        calendarView = view.findViewById(R.id.calenderView);
        achievedGoalListView = view.findViewById(R.id.AchievedGoal);
        noAchievedGoalListView = view.findViewById(R.id.noAchievedGoal);
        calendarPercent = view.findViewById(R.id.calenderPercent);

        achievedAdapter = new AchievedAdapter(); // 어댑터 생성
        noAchievedAdapter = new NoAchievedAdapter();

        achievedGoalListView.setAdapter(achievedAdapter); // 리스트뷰에 어댑터 설정
        noAchievedGoalListView.setAdapter(noAchievedAdapter);

        int createSCount = 0;
        int createTCount = 0;
        Calendar selectedDate = Calendar.getInstance();

        // 선택된 날짜를 지정된 형식의 문자열로 변환합니다.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = format.format(selectedDate.getTime());

        Cursor cursor = readDB(formattedDate);
        createSCount = displayDB(cursor);
        createTCount = createSCount;
        cursor = noReadDB(formattedDate);
        createTCount += noDisplayDB(cursor);
        if(createSCount != 0 && createTCount != 0){
            calendarPercent.setText((createSCount * 100) / createTCount + "%");
        }
        else if(createSCount == 0 || createTCount == 0){
            calendarPercent.setText("00%");
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                achievedAdapter.clear();
                noAchievedAdapter.clear();
                int total_count = 0;
                int select_count = 0;

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                // 선택된 날짜를 지정된 형식의 문자열로 변환
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = format.format(selectedDate.getTime());

                Cursor cursor = readDB(formattedDate);
                select_count = displayDB(cursor);
                total_count = select_count;
                cursor = noReadDB(formattedDate);
                total_count += noDisplayDB(cursor);
                if(select_count != 0 && total_count != 0){
                    calendarPercent.setText((select_count * 100) / total_count + "%");
                }
                else if(select_count == 0 || total_count == 0){
                    calendarPercent.setText("00%");
                }

            }
        });
    }

    private Cursor readDB(String day){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = DATE + " = ? AND " + ACHIEVED + " = ?";
        String[] selectionArgs = { day,  String.valueOf(1) };
        return db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
    }

    private Cursor noReadDB(String day){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = DATE + " = ? AND " + ACHIEVED + " = ?";
        String[] selectionArgs = { day,  String.valueOf(0) };
        return db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
    }

    private int displayDB(Cursor cursor){
        int count = 0;
        while(cursor.moveToNext()){
            String goal = cursor.getString(2);
            long achieved = cursor.getLong(3);
            achievedAdapter.addItem(new ListItem(goal, achieved));
            count++;
        }
        achievedAdapter.notifyDataSetChanged(); // 리스트 뷰 갱신
        return count;
    }

    private int noDisplayDB(Cursor cursor){
        int count = 0;
        StringBuilder builder = new StringBuilder();
        while(cursor.moveToNext()){
            String goal = cursor.getString(2);
            long achieved = cursor.getLong(3);
            noAchievedAdapter.addItem(new ListItem(goal, achieved));
            count++;
        }
        noAchievedAdapter.notifyDataSetChanged(); // 리스트 뷰 갱신
        return count;
    }
}
