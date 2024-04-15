package com.example.everytoday;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "MyGoal";
    public static final String _ID = "_id";
    public static final String DATE = "date";
    public static final String GOAL = "goal";
    public static final String ACHIEVED = "achieved";

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
         + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
         + DATE + " TEXT NOT NULL, "
         + GOAL + " TEXT NOT NULL, "
         + ACHIEVED + " INTEGER NOT NULL DEFAULT 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
