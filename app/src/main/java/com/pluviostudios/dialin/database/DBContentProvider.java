package com.pluviostudios.dialin.database;

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
    static final int WIDGET = 200;

    private static final SQLiteQueryBuilder sWidgetByConfigQueryBuilder;
    public static final String QUERY_INNER_JOIN = "innerJoin";
    public static final String WIDGET_ID = "queryID";
    public static final String CONFIG_ID = "configId";
    public static final String QUERY_BUTTON_COUNT = "buttonCount";

    static {
        sWidgetByConfigQueryBuilder = new SQLiteQueryBuilder();
        sWidgetByConfigQueryBuilder.setTables(
                DBContract.ConfigEntry.TABLE_NAME + " INNER JOIN " +
                        DBContract.WidgetsEntry.TABLE_NAME +
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
        matcher.addURI(authority, DBContract.PATH_WIDGETS, WIDGET);

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

                if (uri.getQueryParameter(CONFIG_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(CONFIG_ID));

                }

                if (uri.getQueryParameter(QUERY_BUTTON_COUNT) != null) {

                    selection = createSelection(selection,
                            DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry.BUTTON_COUNT_COL + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(QUERY_BUTTON_COUNT));

                }

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

                if (uri.getQueryParameter(WIDGET_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.WidgetsEntry.TABLE_NAME + "." + DBContract.WidgetsEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(WIDGET_ID));

                }

                if (uri.getQueryParameter(CONFIG_ID) != null) {

                    if (uri.getQueryParameter(QUERY_INNER_JOIN) != null && Boolean.valueOf(uri.getQueryParameter(QUERY_INNER_JOIN))) {

                        selection = createSelection(selection,
                                DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry._ID + " = ?");

                        selectionArgs = createSelectionArgs(selectionArgs,
                                uri.getQueryParameter(CONFIG_ID));

                    } else {

                        selection = createSelection(selection,
                                DBContract.WidgetsEntry.TABLE_NAME + "." + DBContract.WidgetsEntry.CONFIG_KEY_COL + " = ?");

                        selectionArgs = createSelectionArgs(selectionArgs,
                                uri.getQueryParameter(CONFIG_ID));

                    }

                }

                if (uri.getQueryParameter(QUERY_INNER_JOIN) != null && Boolean.valueOf(uri.getQueryParameter(QUERY_INNER_JOIN))) {

                    retCursor = sWidgetByConfigQueryBuilder.query(
                            mOpenHelper.getReadableDatabase(),
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );

                } else {

                    retCursor = mOpenHelper.getReadableDatabase().query(
                            DBContract.WidgetsEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );

                }
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

                if (uri.getQueryParameter(CONFIG_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(CONFIG_ID));

                }

                rowsDeleted = db.delete(
                        DBContract.ConfigEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case WIDGET: {

                if (uri.getQueryParameter(WIDGET_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.WidgetsEntry.TABLE_NAME + "." + DBContract.WidgetsEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(WIDGET_ID));

                }

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

                if (uri.getQueryParameter(CONFIG_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.ConfigEntry.TABLE_NAME + "." + DBContract.ConfigEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(CONFIG_ID));

                }

                rowsUpdated = db.update(DBContract.ConfigEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case WIDGET: {

                if (uri.getQueryParameter(WIDGET_ID) != null) {

                    selection = createSelection(selection,
                            DBContract.WidgetsEntry.TABLE_NAME + "." + DBContract.WidgetsEntry._ID + " = ?");

                    selectionArgs = createSelectionArgs(selectionArgs,
                            uri.getQueryParameter(WIDGET_ID));

                }

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
