package org.j2page.freebachradio.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.j2page.freebachradio.db.Contract.TrackColumns;

public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FreeBackRadio.db";
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TrackColumns.TABLE_NAME + " (" +
                    TrackColumns._ID + " INTEGER PRIMARY KEY," +
                    TrackColumns.URL + " TEXT NOT NULL UNIQUE," +
                    TrackColumns.TITLE + " TEXT," +
                    TrackColumns.COMPOSER + " TEXT," +
                    TrackColumns.PERFORMER + " TEXT," +
                    TrackColumns.RELEASE + " TEXT," +
                    TrackColumns.IMAGE + " TEXT," +
                    TrackColumns.LOADED + " INTEGER" +
                    " )";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
