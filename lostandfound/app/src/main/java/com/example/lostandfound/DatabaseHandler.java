package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "items.db";
    public static final String TABLE_NAME = "item_table";
    public static final String COL1 = "id";
    public static final String COL2 = "NAME";
    public static final String COL3 = "DESCRIPTION";
    public static final String COL4 = "DATE";
    public static final String COL5 = "EXTRA";
    public static final String COL6 = "LOCATION";
    public static final String COL7 = "USERNAME";
    public static final String COL8 = "CTIME";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, DESCRIPTION TEXT, DATE TEXT, EXTRA TEXT, LOCATION TEXT, USERNAME TEXT, CTIME TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    //public Item(String name, String Description, String dol, String extra, String location, String image, String id, String username, String Ctime) {

    public boolean insertData(String name, String description, String dol, String extra, String location, String username, String Ctime) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, name);
        contentValues.put(COL3, description);
        contentValues.put(COL4, dol);
        contentValues.put(COL5, extra);
        contentValues.put(COL6, location);
        contentValues.put(COL7, username);
        contentValues.put(COL8, Ctime);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public void deleteAll()
    {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from "+ TABLE_NAME);
        db.close();
    }

    public Cursor getAllData(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

}
