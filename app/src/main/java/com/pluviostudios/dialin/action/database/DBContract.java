package com.pluviostudios.dialin.action.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by spectre on 8/2/16.
 */
public class DBContract {

    public static final String TAG = "DBContract";

    public static final String CONTENT_AUTHORITY = "com.pluviostudios.dialin.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CONFIG = "config";
    public static final String PATH_WIDGETS = "widgets";

    public static final class ConfigEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONFIG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;

        public static final String TABLE_NAME = PATH_CONFIG;

        public static final String FILENAME_COL = "file_name";
        public static final String BUTTON_COUNT_COL = "button_count";
        public static final String DATE_CREATED_COL = "date_created";

        public static Uri buildConfigUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildConfigWithButtonCount(int buttonCount) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(buttonCount)).build();
        }

        public static int getButtonCountFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

    }

    public static final class WidgetsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONFIG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;

        public static final String TABLE_NAME = PATH_WIDGETS;

        public static final String WIDGET_ID_COL = "widget_id";
        public static final String CONFIG_KEY_COL = "config_key";

        public static Uri buildWidgetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        public static long getConfigIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

}
