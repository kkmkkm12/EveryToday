package com.example.everytoday;

import static com.example.everytoday.DBOpenHelper.ACHIEVED;
import static com.example.everytoday.DBOpenHelper.DATE;
import static com.example.everytoday.DBOpenHelper.GOAL;
import static com.example.everytoday.DBOpenHelper.TABLE_NAME;
import static com.example.everytoday.DBOpenHelper._ID;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalListFragment extends Fragment implements GoalListCallback {

    private ListView listView;
    private ListItemAdapter adapter;
    private Button addListButton;
    private Dialog dialog;
    private String addString;
    private TextView subPercent;

    private static final String DB_NAME = "MyDB";
    private static final int DB_VERSION = 1;
    private DBOpenHelper openHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subPercent = view.findViewById(R.id.subPercent);

        adapter = new ListItemAdapter(GoalListFragment.this); // 어댑터 생성
        listView = view.findViewById(R.id.goalListView);
        listView.setAdapter(adapter); // 리스트뷰에 어댑터 설정

        openHelper = new DBOpenHelper(getActivity(), DB_NAME, null, DB_VERSION);

        percent();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Cursor cursor = readDB(format.format(calendar.getTime()));

        if (!(cursor != null && cursor.getCount() > 0)) {
            calendar.add(Calendar.DATE, -1);
            cursor = readDB(format.format(calendar.getTime()));
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String goal = cursor.getString(2);
                    writeDB(goal);
                }

                calendar.add(Calendar.DATE, 1);
                cursor = readDB(format.format(calendar.getTime()));
            }
        }
        if (cursor != null && cursor.getCount() > 0) {
            String str = displayDB(cursor).toString();
            cursor.close();
        }

        addListButton = view.findViewById(R.id.addListButton);
        addListButton.setBackgroundColor(Color.parseColor("#6699FF"));

        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.activity_add_list);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.show();

                EditText goalEditText = dialog.findViewById(R.id.goalEditText);
                Button addButton = dialog.findViewById(R.id.addButton);
                addButton.setBackgroundColor(Color.parseColor("#6699FF"));

                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addString = goalEditText.getText().toString();
                        goalEditText.setText("");
                        adapter.addItem(new ListItem(addString, 0));
                        adapter.notifyDataSetChanged();

                        writeDB(addString);

                        dialog.dismiss();
                    }
                });

            }
        });
    }

    private StringBuilder displayDB(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String date = cursor.getString(1);
            String goal = cursor.getString(2);
            long achieved = cursor.getLong(3);
            adapter.addItem(new ListItem(goal, achieved));

            builder.append(id).append(" : ");
            builder.append(date).append(" : ");
            builder.append(goal);
        }
        return builder;
    }

    private Cursor readDB(String currentDay) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED,};
        String selection = DATE + " = ?";
        String[] selectionArgs = {currentDay};
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        //startManagingCursor(cursor);
        return cursor;
    }

    public void percent() {
        if (totalReadDB() != null) {
            Cursor cursor = totalReadDB();
            int total_count = countDB(cursor);
            int select_count = countDB(selectReadDB());
            totalReadDB().close();
            selectReadDB().close();
            if (total_count != 0 && select_count != 0) {
                subPercent.setText(String.valueOf((select_count * 100) / total_count) + "%");
            } else if (total_count == 0 || select_count == 0)
                subPercent.setText(String.valueOf(0) + "0%");
        }
    }

    private int countDB(Cursor cursor) {
        int count = 0;
        while (cursor.moveToNext()) {
            count++;
        }
        return count;
    }

    private Cursor totalReadDB() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED,};
        String selection = DATE + " = ?";
        String[] selectionArgs = {format.format(calendar.getTime())};
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        return cursor;
    }

    private Cursor selectReadDB() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED,};
        String selection = DATE + " = ? AND " + ACHIEVED + " = ?";
        String[] selectionArgs = {format.format(calendar.getTime()), String.valueOf(1)};
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        return cursor;
    }

    private void writeDB(String goal) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, format.format(calendar.getTime()));
        values.put(GOAL, goal);
        values.put(ACHIEVED, 0);
        db.insertOrThrow(TABLE_NAME, null, values);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (openHelper != null) {
            openHelper.close();
        }
    }

    @Override
    public void updatePercent() {
        percent();
    }
}
