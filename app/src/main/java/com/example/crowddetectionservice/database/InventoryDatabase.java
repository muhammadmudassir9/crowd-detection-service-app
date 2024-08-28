package com.example.crowddetectionservice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class InventoryDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE records (_id INTEGER PRIMARY KEY, file_path TEXT, file_type TEXT, date TEXT, time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS records");
        onCreate(db);
    }

    public void addRecord(String filePath, String fileType, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_path", filePath);
        contentValues.put("file_type", fileType);
        contentValues.put("date", date);
        contentValues.put("time", time);
        db.insert("records", null, contentValues);
        db.close();
    }

    public List<InventoryRecord> getRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM records", null);
        List<InventoryRecord> records = new ArrayList<>();
        while (cursor.moveToNext()) {
            records.add(new InventoryRecord(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
        }
        cursor.close();
        db.close();
        return records;
    }

    public ArrayList<String> getDates() {
        ArrayList<String> dates = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT DISTINCT date FROM records", null);
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return dates;
    }

    public ArrayList<InventoryRecord> getRecordsByDate(String date) {
        ArrayList<InventoryRecord> records = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM records WHERE date = ?", new String[]{date});
        if (cursor.moveToFirst()) {
            do {
                records.add(new InventoryRecord(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4) // Time column
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return records;
    }
}
