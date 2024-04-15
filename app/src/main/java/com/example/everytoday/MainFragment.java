package com.example.everytoday;

import static com.example.everytoday.DBOpenHelper.ACHIEVED;
import static com.example.everytoday.DBOpenHelper.DATE;
import static com.example.everytoday.DBOpenHelper.GOAL;
import static com.example.everytoday.DBOpenHelper.TABLE_NAME;
import static com.example.everytoday.DBOpenHelper._ID;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainFragment extends Fragment {

    private TextView countDown;
    private TextView monthday;
    private TextView clock;
    private TextView mainPercent;
    private Thread thread;
    private int printPercent = 0;

    private static final String DB_NAME = "MyDB";
    private static final int DB_VERSION = 1;
    private DBOpenHelper openHelper;

    private Handler handler;

    private boolean isThreadRunning = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        openHelper = new DBOpenHelper(getActivity(), DB_NAME, null, DB_VERSION);

        countDown = view.findViewById(R.id.countDown);
        monthday = view.findViewById(R.id.monthdayTextView);
        clock = view.findViewById(R.id.clock);

        handler = new Handler();
        mainPercent = view.findViewById(R.id.mainPercent);

        updateTime();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat todayformat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        monthday.setText(todayformat.format(calendar.getTime()));

        Cursor cursor = readDB(format.format(calendar.getTime()));
        int total_count = displayDB(cursor);
        cursor = selectDB(format.format(calendar.getTime()));
        int select_count = displayDB(cursor);
        cursor.close();
        if(total_count != 0 && select_count != 0){
            printPercent = (select_count * 100) / total_count;
        }

        threadStart();

        startUpdateTime();
    }

    private void startUpdateTime(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateTime(){
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH", Locale.getDefault());
        SimpleDateFormat sdfMinute = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat sdfSecond = new SimpleDateFormat("ss", Locale.getDefault());

        // 현재 시, 분, 초를 추출하여 int 변수에 저장
        int realHour = Integer.parseInt(sdfHour.format(new Date()));
        int currentHour = 23 - realHour;
        String formatHour = String.format(Locale.getDefault(), "%02d", currentHour);
        int realMinute = Integer.parseInt(sdfMinute.format(new Date()));
        int currentMinute = 59 - realMinute;
        String formatMinute = String.format(Locale.getDefault(), "%02d", currentMinute);
        int currentSecond = 59 - Integer.parseInt(sdfSecond.format(new Date()));
        String formatSecond = String.format(Locale.getDefault(), "%02d", currentSecond);
        countDown.setText(formatHour + ":" + formatMinute + ":" + formatSecond);

        String ampm = "am ";
        if(Integer.parseInt(sdfHour.format(new Date())) >= 12){
            ampm = "pm ";
        }
        ampm += String.format("%02d", realHour % 12);
        ampm += ":" + String.format("%02d", realMinute);
        clock.setText(ampm);
    }

    private Cursor readDB(String currentDay){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = DATE + " = ?";
        String[] selectionArgs = { currentDay };
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        return cursor;
    }

    private Cursor selectDB(String currentDay){
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = ACHIEVED + " = ? AND " + DATE + " = ?";
        String[] selectionArgs = { String.valueOf(1), currentDay };
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        return cursor;
    }

    private int displayDB(Cursor cursor){
        int count = 0;
        while(cursor.moveToNext()){
            String goal = cursor.getString(2);
            count++;
        }
        return count;
    }

    private void threadStart(){
        thread = new Thread(){
            @Override
            public void run() {
                super.run();
                final int[] i = {-1};
                int sleepping = 20;

                while(isThreadRunning && i[0] < printPercent){
                    try{
                        sleep(sleepping);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    i[0]++;
                    if(printPercent - 10 <= i[0])

                        sleepping += 15;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainPercent.setText(i[0] + "%");
                        }

                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 프래그먼트가 화면에서 사라지면 스레드를 중지
        isThreadRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 프래그먼트가 다시 화면에 나타날 때 스레드를 다시 시작
        isThreadRunning = true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 프래그먼트의 뷰가 제거될 때 핸들러와 스레드를 해제
        handler.removeCallbacksAndMessages(null);
    }
}