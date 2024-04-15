package com.example.everytoday;

import static com.example.everytoday.DBOpenHelper.ACHIEVED;
import static com.example.everytoday.DBOpenHelper.DATE;
import static com.example.everytoday.DBOpenHelper.GOAL;
import static com.example.everytoday.DBOpenHelper.TABLE_NAME;
import static com.example.everytoday.DBOpenHelper._ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ListItemAdapter extends BaseAdapter {

    ArrayList<ListItem> items = new ArrayList<ListItem>();
    Context context;

    private GoalListCallback callback;
    public ListItemAdapter(GoalListCallback callback) {
        this.callback = callback;
    }



    private static final String DB_NAME = "MyDB";
    private static final int DB_VERSION = 1;
    private DBOpenHelper openHelper;



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        ListItem listItem = items.get(position);
        openHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        Button goalStr = convertView.findViewById(R.id.goalCheckButton);
        goalStr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listItem.setSelect(listItem.getSelect() == 0 ? 1 : 0);

                notifyDataSetChanged();
            }
        });

        goalStr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeItem(listItem);
                Toast.makeText(context, goalStr.getText().toString() + "이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                deleteData(goalStr.getText().toString());
                return false;
            }
        });

        goalStr.setText(listItem.getGoalStr());

        Cursor cursor = readDB(goalStr.getText().toString());
        long a = displayDB(cursor);



        if(listItem.getSelect() == 1){
            goalStr.setBackgroundColor(Color.parseColor("#6699FF"));
            updateData(goalStr.getText().toString(), 1);
            updateData();
        }
        else if (listItem.getSelect() == 0){
            goalStr.setBackgroundColor(Color.parseColor("#CCCCCC"));
            updateData(goalStr.getText().toString(), 0);
            updateData();
        }
        cursor.close();

        return convertView;
    }

    private long displayDB(Cursor cursor){
        long builder = 0;
        while(cursor.moveToNext()){
            long achieved = cursor.getLong(3);

            builder = achieved;
        }
        return builder;
    }

    private Cursor readDB(String btnName){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] from = {_ID, DATE, GOAL, ACHIEVED, };
        String selection = GOAL + " = ? AND " + DATE + " = ?";
        String[] selectionArgs = { btnName, format.format(calendar.getTime())};
        Cursor cursor = db.query(TABLE_NAME, from, selection, selectionArgs, null, null, _ID + " " + "ASC");
        return cursor;
    }






    private void updateData(String Goal, long newSelect) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACHIEVED, newSelect);

        String selection = GOAL + " = ? AND " + DATE + " = ?";
        String[] selectionArgs = {String.valueOf(Goal), format.format(calendar.getTime())};

        int count = db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    private void deleteData(String specificGoal) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SQLiteDatabase db = openHelper.getWritableDatabase();

        String selection = GOAL + " = ? AND " + DATE + " = ?";
        String[] selectionArgs = { specificGoal, format.format(calendar.getTime()) };

        db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public void removeItem(ListItem item){
        items.remove(item);
    }
    public void addItem(ListItem item){
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void updateData() {
        if (callback != null) {
            callback.updatePercent();
        }
    }
}
