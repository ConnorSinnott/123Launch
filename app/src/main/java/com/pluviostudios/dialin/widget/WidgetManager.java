package com.pluviostudios.dialin.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;

import java.util.ArrayList;

/**
 * Created by spectre on 8/4/16.
 */
public class WidgetManager {

    public static final String TAG = "WidgetManager";

    public static ArrayList<Integer> getWidgetsUsingConfig(Context context, long configurationID) {

        ArrayList<Integer> out = new ArrayList<>();

        final String[] projection = new String[]{DBContract.WidgetsEntry.WIDGET_ID_COL};

        Cursor c = context.getContentResolver().query(
                DBContract.WidgetsEntry.buildWidgetWithConfigId(configurationID),
                projection,
                null, null, null, null
        );

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    out.add(c.getInt(0));
                } while (c.moveToNext());
            }
            c.close();
        }
        return out;

    }

    public static void updateWidgets(Context context, int... appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            final String[] projection = new String[]{
                    DBContract.WidgetsEntry.CONFIG_KEY_COL,
                    DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL,
                    DBContract.ConfigEntry.BUTTON_COUNT_COL
            };

            Cursor c = context.getContentResolver().query(
                    DBContract.WidgetsEntry.buildWidgetInnerJoinConfigWithWidgetId(appWidgetId),
                    projection,
                    null, null, null, null
            );

            if (c != null) {
                if (c.moveToFirst()) {
                    String rawPath = c.getString(0);
                    long configId = c.getLong(1);
                    int buttonCount = c.getInt(2);
                    c.close();

                    ArrayList<Integer> path = convertStringToPath(rawPath);

                    Node previewNode = StorageManager.loadNodeForWidget(context, configId, path);

                    RemoteViews views = generateRemoteViewsFromNode(context, previewNode, buttonCount);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                c.close();
            }
        }
    }

    /**
     * This function either appends index to the end of the widgetsCurrent path, or launches the current action
     * and clears the path. It should call updateWidgets after to update the UI
     */
    public static void onWidgetButtonClicked(Context context, int appWidgetId, int index) {

        final String[] projection = new String[]{
                DBContract.WidgetsEntry.CONFIG_KEY_COL,
                DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL,
                DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX};

        Cursor c = context.getContentResolver().query(
                DBContract.WidgetsEntry.buildWidgetInnerJoinConfigWithWidgetId(appWidgetId),
                projection,
                null, null, null, null
        );

        // Get Widget Info
        if (c != null) {
            if (c.moveToFirst()) {

                long configId = c.getLong(1);
                int launchButtonIndex = c.getInt(2);
                String rawPath = c.getString(0);
                ArrayList<Integer> path = convertStringToPath(rawPath);
                c.close();


                ContentValues contentValues = new ContentValues();

                if (index == launchButtonIndex) {

                    // Get the current Node to launch the action
                    Node node = StorageManager.loadNodeForWidget(context, configId, path);
                    node.getAction().execute();

                    // Clear path
                    contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, "");

                } else {

                    // Otherwise append a new path segment and update widget
                    contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, rawPath + " " + index);

                }

                context.getContentResolver().update(
                        DBContract.WidgetsEntry.buildWidgetWithId(appWidgetId),
                        contentValues,
                        null, null
                );

                updateWidgets(context, appWidgetId);

            }
            c.close();
        }
        Log.e(TAG, "onWidgetButtonClicked: Error updating widget");
    }

    private static RemoteViews generateRemoteViewsFromNode(Context context, Node node, int buttonCount) {

//        LinearLayout buttonsView = ButtonsFragment.generateButtons(context, buttonCount, false, null);

        // Might have to inflate a blank frame layout and .addView the buttons view

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), buttonsView);
//        RemoteViews nettt = new RemoteViews(
//        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

        return null;

    }

    private static PendingIntent generateButtonPendingIntent(Context context, int appWidgetId, int index) {
        Intent intent = new Intent(WidgetReceiver.INTENT_TYPE);
        intent.putExtra(WidgetReceiver.EXTRA_INDEX, index);
        intent.putExtra(WidgetReceiver.EXTRA_APPWIDGETID, appWidgetId);
        return PendingIntent.getBroadcast(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Convert raw widgetPath to int[]
    private static ArrayList<Integer> convertStringToPath(String path) {
        String[] pathIndexes = path.split(" ");
        ArrayList<Integer> out = new ArrayList<>();
        for (String x : pathIndexes) {
            out.add(Integer.parseInt(x));
        }
        return out;
    }

    public static void addWidgetToDB(Context context, int appWidgetId, long configurationId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.WidgetsEntry.WIDGET_ID_COL, appWidgetId);
        contentValues.put(DBContract.WidgetsEntry.CONFIG_KEY_COL, configurationId);
        contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, "");
        context.getContentResolver().insert(
                DBContract.WidgetsEntry.CONTENT_URI,
                contentValues
        );

    }

    public static void removeWidgetFromDB(Context context, int appWidgetId) {

        final String selection = DBContract.WidgetsEntry.WIDGET_ID_COL + "=?";
        final String[] selectionArgs = new String[]{String.valueOf(appWidgetId)};

        context.getContentResolver().delete(
                DBContract.WidgetsEntry.CONTENT_URI,
                selection,
                selectionArgs
        );

    }

}
