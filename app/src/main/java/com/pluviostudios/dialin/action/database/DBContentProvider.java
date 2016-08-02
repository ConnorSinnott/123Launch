package com.pluviostudios.dialin.action.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by spectre on 8/2/16.
 */
public class DBContentProvider extends ContentProvider {

    public static final String TAG = "DBContentProvider";

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mOpenHelper;

    static final int CONFIG = 100;
    static final int CONFIG_WITH_BUTTON_COUNT = 101;

    static final int WIDGET = 200;
    static final int WIDGET_WITH_CONFIG = 201;

    private static final SQLiteQueryBuilder sWidgetByConfigQueryBuilder;

    static {
        sWidgetByConfigQueryBuilder = new SQLiteQueryBuilder();
        sWidgetByConfigQueryBuilder.setTables(
                DBContract.ConfigEntry.TABLE_NAME + " INNER JOIN " +
                        DBContract.ConfigEntry.TABLE_NAME +
                        " ON " + DBContract.WidgetsEntry.TABLE_NAME +
                        "." + DBContract.WidgetsEntry.CONFIG_KEY_COL +
                        " = " + DBContract.ConfigEntry.TABLE_NAME +
                        "." + DBContract.ConfigEntry._ID
        );
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DBContract.PATH_CONFIG, CONFIG);
        matcher.addURI(authority, DBContract.PATH_CONFIG + "/#", CONFIG_WITH_BUTTON_COUNT);

        matcher.addURI(authority, DBContract.PATH_WIDGETS, WIDGET);
        matcher.addURI(authority, DBContract.PATH_WIDGETS + "/#", WIDGET_WITH_CONFIG);

        return matcher;

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CONFIG:
                return DBContract.ConfigEntry.CONTENT_TYPE;
            case WIDGET:
                return DBContract.WidgetsEntry.CONTENT_TYPE;
            case WIDGET_WITH_CONFIG:
                return DBContract.WidgetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case CONFIG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DBContract.ConfigEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CONFIG_WITH_BUTTON_COUNT: {

                int buttonCount = DBContract.ConfigEntry.getButtonCountFromUri(uri);

                selection = createSelection(selection,
                        DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry.BUTTON_COUNT_COL + " = ?");

                selectionArgs = createSelectionArgs(selectionArgs,
                        String.valueOf(buttonCount));

                retCursor = mOpenHelper.getReadableDatabase().query(
                        DBContract.ConfigEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case WIDGET: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DBContract.WidgetsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case WIDGET_WITH_CONFIG: {

                long configID = DBContract.WidgetsEntry.getConfigIdFromUri(uri);

                selection = createSelection(selection,
                        DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry._ID + " = ?");

                selectionArgs = createSelectionArgs(selectionArgs,
                        String.valueOf(configID));

                retCursor = sWidgetByConfigQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CONFIG: {
                long _id = db.insert(DBContract.ConfigEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DBContract.ConfigEntry.buildConfigUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row int " + uri);
                break;
            }
            case WIDGET: {
                long _id = db.insert(DBContract.WidgetsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DBContract.WidgetsEntry.buildWidgetUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row int " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        switch (match) {
            case CONFIG: {
                rowsDeleted = db.delete(
                        DBContract.ConfigEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case WIDGET: {
                rowsDeleted = db.delete(
                        DBContract.WidgetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CONFIG: {
                rowsUpdated = db.update(DBContract.ConfigEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case WIDGET: {
                rowsUpdated = db.update(
                        DBContract.WidgetsEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private String createSelection(String passedSelection, String... additionalSelection) {

        String[] selectionArray;
        if (passedSelection != null) {
            if (additionalSelection != null) {
                selectionArray = new String[additionalSelection.length + 1];
                selectionArray[0] = passedSelection;
                System.arraycopy(additionalSelection, 0, selectionArray, 1, additionalSelection.length);
            } else
                return passedSelection;
        } else {
            selectionArray = additionalSelection;
        }

        String out = "";
        for (int i = 0; i < selectionArray.length; i++) {
            String curr = selectionArray[i];
            out += curr;
            if (i < selectionArray.length - 1) {
                out += " AND ";
            }
        }

        return out;
    }

    private String[] createSelectionArgs(String[] passedSelectionArgs, String... additionalArgs) {

        if (passedSelectionArgs == null || additionalArgs == null) {
            return passedSelectionArgs == null ? additionalArgs : passedSelectionArgs;
        } else {
            String[] newSelection = new String[passedSelectionArgs.length + additionalArgs.length];
            System.arraycopy(passedSelectionArgs, 0, newSelection, 0, passedSelectionArgs.length);
            System.arraycopy(additionalArgs, 0, newSelection, passedSelectionArgs.length, additionalArgs.length);
            return newSelection;
        }

    }


}
