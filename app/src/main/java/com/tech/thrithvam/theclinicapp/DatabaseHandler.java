package com.tech.thrithvam.theclinicapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ClinicApp.db";
    private SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Creating Tables
    // IMPORTANT: if you are changing anything in the below function onCreate(), DO DELETE THE DATABASE file in
    // the emulator or uninstall the application in the phone, to run the application
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS UserAccount (UserName TEXT,ClinicID TEXT);";//, Email TEXT, Password TEXT, MobNo TEXT, Gender TEXT);";
        db.execSQL(CREATE_USERACCOUNTS_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME );
        // Create tables again
        onCreate(db);
    }
    //--------------------------User Accounts-----------------------------
    public void UserLogin(String UserName,String clinicID)//, String Email, String Password, String MobNo, String Gender)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO UserAccount (UserName,ClinicID) VALUES ('"+UserName+"','"+clinicID+"');");
        db.close();
    }
    public void UserLogout()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM UserAccount;");
        db.close();
    }
    public String GetUserDetail(String detail)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM UserAccount;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(detail));
        }
        else return null;
    }
}

