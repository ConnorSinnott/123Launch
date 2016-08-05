package com.pluviostudios.dialin.database;

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
    public static final String PATH_WIDGETS = "widget";

    public static final class ConfigEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONFIG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;

        public static final String TABLE_NAME = PATH_CONFIG;

        public static final String TITLE_COL = "config_title";
        public static final String BUTTON_COUNT_COL = "button_count";
        public static final String DATE_MODIFIED = "date_created";
        public static final String LAUNCH_BUTTON_INDEX = "launch_button_index";

        public static Uri buildConfigUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        // Queries

        public static Uri buildConfigWithId(long id) {
            return CONTENT_URI.buildUpon().appendQueryParameter(DBContentProvider.CONFIG_ID, String.valueOf(id)).build();
        }

        public static Uri buildConfigWithButtonCount(int buttonCount) {
            return CONTENT_URI.buildUpon().appendQueryParameter(DBContentProvider.QUERY_BUTTON_COUNT, String.valueOf(buttonCount)).build();
        }


    }

    public static final class WidgetsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WIDGETS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONFIG;

        public static final String TABLE_NAME = PATH_WIDGETS;

        public static final String WIDGET_ID_COL = "widget_id";
        public static final String CONFIG_KEY_COL = "config_key";
        public static final String WIDGET_CURRENT_PATH_COL = "widget_current_path";

        public static Uri buildWidgetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        // Queries

        public static Uri buildWidgetWithId(long id) {
            return CONTENT_URI.buildUpon().appendQueryParameter(DBContentProvider.WIDGET_ID, String.valueOf(id)).build();
        }

        public static Uri buildWidgetWithConfigId(long id) {
            return CONTENT_URI.buildUpon().appendQueryParameter(DBContentProvider.CONFIG_ID, String.valueOf(id)).build();
        }

        public static Uri buildWidgetInnerJoinConfig() {
            return CONTENT_URI.buildUpon().appendQueryParameter(DBContentProvider.QUERY_INNER_JOIN, String.valueOf(true)).build();
        }

        public static Uri buildWidgetInnerJoinConfigWithConfigId(long id) {
            return buildWidgetInnerJoinConfig().buildUpon().appendQueryParameter(DBContentProvider.CONFIG_ID, String.valueOf(id)).build();
        }

        public static Uri buildWidgetInnerJoinConfigWithWidgetId(long id) {
            return buildWidgetInnerJoinConfig().buildUpon().appendQueryParameter(DBContentProvider.WIDGET_ID, String.valueOf(id)).build();
        }

    }

}
