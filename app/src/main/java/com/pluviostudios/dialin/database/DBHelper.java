package com.pluviostudios.dialin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by spectre on 8/2/16.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "DBHelper";

    public static final String DATABASE_NAME = "123Go.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CreateConfigTable = "CREATE TABLE " + DBContract.ConfigEntry.TABLE_NAME + " ("
                + DBContract.ConfigEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.ConfigEntry.TITLE_COL + " TEXT NOT NULL,"
                + DBContract.ConfigEntry.BUTTON_COUNT_COL + " INTEGER NOT NULL,"
                + DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX + " INTEGER NOT NULL, "
                + DBContract.ConfigEntry.DATE_MODIFIED + " LONG NOT NULL)";

        final String CreateWidgetTable = "CREATE TABLE " + DBContract.WidgetsEntry.TABLE_NAME + " ("
                + DBContract.WidgetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.WidgetsEntry.APP_WIDGET_ID_COL + " INTEGER NOT NULL,"
                + DBContract.WidgetsEntry.CONFIG_KEY_COL + " LONG NOT NULL,"
                + DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL + " STRING NOT NULL, "
                + " FOREIGN KEY (" + DBContract.WidgetsEntry.CONFIG_KEY_COL + ") REFERENCES "
                + DBContract.ConfigEntry.TABLE_NAME + " (" + DBContract.ConfigEntry._ID + ")"
                + " UNIQUE (" + DBContract.WidgetsEntry.APP_WIDGET_ID_COL + ") ON CONFLICT REPLACE)";

        db.execSQL(CreateConfigTable);
        db.execSQL(CreateWidgetTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ConfigEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.WidgetsEntry.TABLE_NAME);
    }

}
