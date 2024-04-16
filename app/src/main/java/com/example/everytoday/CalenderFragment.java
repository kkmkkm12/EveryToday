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
    private TextView monthPercent;

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
        monthPercent = view.findViewById(R.id.ymd);

        achievedAdapter = new AchievedAdapter(); // 어댑터 생성
        noAchievedAdapter = new NoAchievedAdapter();

        achievedGoalListView.setAdapter(achievedAdapter); // 리스트뷰에 어댑터 설정
        noAchievedGoalListView.setAdapter(noAchievedAdapter);

        Calendar selectedDate = Calendar.getInstance();

        monthPercentPrint(selectedDate);

        todayPercent(selectedDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                achievedAdapter.clear();
                noAchievedAdapter.clear();

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                monthPercentPrint(selectedDate);

                todayPercent(selectedDate);
            }
        });
    }

    private Cursor readDB(String day, int valueAchieved){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = DATE + " = ? AND " + ACHIEVED + " = ?";
        String[] selectionArgs = { day,  String.valueOf(valueAchieved) };
        return db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
    }
    private Cursor monthReadDB(String day, boolean state){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        if(!state){
            String selection = DATE + " LIKE ?";
            String[] selectionArgs = { day + "%"};
            return db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        }else{
            String selection = DATE + " LIKE ? AND " + ACHIEVED + " = ?";
            String[] selectionArgs = { day + "%", String.valueOf(1) };
            return db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        }
    }
    private int countFunc(Cursor cursor){
        int count = 0;
        while(cursor.moveToNext()){
            count++;
        }
        return count;
    }

    private int displayDB(Cursor cursor, boolean adapter){
        int count = 0;
        if(adapter) {
            while (cursor.moveToNext()) {
                String goal = cursor.getString(2);
                long achieved = cursor.getLong(3);
                achievedAdapter.addItem(new ListItem(goal, achieved));
                count++;
            }
            achievedAdapter.notifyDataSetChanged(); // 리스트 뷰 갱신
            return count;
        }else{
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
    private void monthPercentPrint(Calendar selectedDate){
        int monthSCount = 0;
        int monthTCount = 0;

        SimpleDateFormat ymFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String ymDate = ymFormat.format(selectedDate.getTime());

        Cursor cursor = monthReadDB(ymDate, false);
        monthTCount = countFunc(cursor);
        cursor = monthReadDB(ymDate, true);
        monthSCount = countFunc(cursor);

        if(monthTCount != 0 && monthSCount != 0){
            monthPercent.setText(ymDate + " 달성률 : " + ((monthSCount * 100) / monthTCount) + "%");
        }
        else if(monthTCount == 0 || monthSCount == 0){
            monthPercent.setText(ymDate + " 달성률 : 00%");
        }
    }

    private void todayPercent(Calendar selectedDate){
        int createSCount = 0;
        int createTCount = 0;

        // 선택된 날짜를 지정된 형식의 문자열로 변환합니다.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = format.format(selectedDate.getTime());

        Cursor cursor = readDB(formattedDate, 1);
        createSCount = displayDB(cursor, true);
        createTCount = createSCount;
        cursor = readDB(formattedDate, 0);
        createTCount += displayDB(cursor, false);
        if(createSCount != 0 && createTCount != 0){
            calendarPercent.setText((createSCount * 100) / createTCount + "%");
        }
        else if(createSCount == 0 || createTCount == 0){
            calendarPercent.setText("00%");
        }
    }
}
