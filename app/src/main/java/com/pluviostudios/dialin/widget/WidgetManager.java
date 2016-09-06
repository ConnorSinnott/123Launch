package com.pluviostudios.dialin.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.google.common.primitives.Ints;
import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSetManager;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;

import java.util.ArrayList;

/**
 * Created by spectre on 8/4/16.
 */
public class WidgetManager {

    public static final String TAG = "WidgetManager";


    /**
     * Queries the database for a list of AppWidgetIds currently using the configuration with the passed ConfigurationID
     *
     * @param context         Application context
     * @param configurationID The requested configurationID
     * @return A list of AppWidgetIds which are currently using the provided ConfigurationId
     */
    public static ArrayList<Integer> getWidgetsUsingConfig(Context context, long configurationID) {

        // Create the output ArrayList
        ArrayList<Integer> out = new ArrayList<>();

        // Query the database for AppWidgetIds using the provided configurationID
        final String[] projection = new String[]{DBContract.WidgetsEntry.APP_WIDGET_ID_COL};

        Cursor c = context.getContentResolver().query(
                DBContract.WidgetsEntry.buildWidgetWithConfigId(configurationID),
                projection,
                null, null, null, null
        );

        // If the cursor is not null and there are entries
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // Add each appWidgetId to the output ArrayList
                    out.add(c.getInt(0));
                } while (c.moveToNext());
            }
            c.close();
        }

        // Return the output arrayList
        return out;

    }

    public static void updateWidgets(Context context, ArrayList<Integer> appWidgetIds) {
        updateWidgets(context, Ints.toArray(appWidgetIds));
    }

    /**
     * Queries the database and updates the provided appWidgetIds using their current path and configuration
     *
     * @param context      Application Context
     * @param appWidgetIds The appWidgetIds to be updated
     */
    public static void updateWidgets(Context context, int... appWidgetIds) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // For each passed appWidgetId
        for (int appWidgetId : appWidgetIds) {

            // Query the database for all required information
            final String[] projection = new String[]{
                    DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, // The widget's current path (Ex. "0 2 1"). This determines how the node tree will be traversed
                    DBContract.WidgetsEntry.CONFIG_KEY_COL, // The widget's configurationId. This will determine which node tree will be loaded for the widget
                    DBContract.ConfigEntry.BUTTON_COUNT_COL, // The widget's button count. Used to generate the view
                    DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX // The widget's launch button index. Which button execute the node.
            };

            Cursor c = context.getContentResolver().query(
                    DBContract.WidgetsEntry.buildWidgetInnerJoinConfigWithAppWidgetId(appWidgetId),
                    projection,
                    null, null, null, null
            );

            // If the cursor is not null and there are entries
            if (c != null) {
                if (c.moveToFirst()) {

                    // Extract all the information mentioned above
                    String rawPath = c.getString(0);
                    long configId = c.getLong(1);
                    int buttonCount = c.getInt(2);
                    int launchButtonIndex = c.getInt(3);

                    boolean isVertical = WidgetTools.isWidgetVertical(context, appWidgetId);

                    // Convert the rawPath (Ex. "0 2 1") into an ArrayList (essentially String.split(" ") )
                    ArrayList<Integer> path = convertStringToPath(rawPath);

                    // Load the configuration via ID, and generate the node located at the end of the pathLoad the node at the end of the path.
                    Node previewNode = StorageManager.loadNodeForWidget(context, configId, path);

                    // Generate the remoteViews using the previewNode
                    RemoteViews views = generateRemoteViewsFromNode(context, appWidgetId, buttonCount, launchButtonIndex, isVertical, previewNode);

                    // Update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views);

                } else {

                    // Configuration is missing
                    RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_config_missing);
                    appWidgetManager.updateAppWidget(appWidgetId, view);

                }
                c.close();
            }
        }
    }

    /**
     * Using the passed appWidgetId, query the database for the current path associated with that id.
     * Then, either append to the path the passed index, or execute the node at the end of the path and reset the path.
     *
     * @param context     Application context
     * @param appWidgetId The appWidgetId which had a button clicked
     * @param index       Which button was clicked
     */
    public static void onWidgetButtonClicked(Context context, int appWidgetId, int index) {

        final String[] projection = new String[]{
                DBContract.WidgetsEntry.CONFIG_KEY_COL,
                DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL,
        };

        Cursor c = context.getContentResolver().query(
                DBContract.WidgetsEntry.buildWidgetWithAppWidgetId(appWidgetId),
                projection,
                null, null, null, null
        );

        // If the cursor is not null and there are entries
        if (c != null) {
            if (c.moveToFirst()) {

                long configId = c.getLong(0);
                String rawPath = c.getString(1);
                ArrayList<Integer> path = convertStringToPath(rawPath);
                c.close();

                // Create contentValues because the widget's path will need to be updated
                ContentValues contentValues = new ContentValues();

                // If the button pressed was the widget's launch button
                if (index < 0) {

                    // Load the configuration via ID, and generate the node located at the end of the pathLoad the node at the end of the path.
                    Node node = StorageManager.loadNodeForWidget(context, configId, path);
                    if (node.hasAction()) {
                        // If that node has an action, execute it.
                        node.getAction().execute();
                    }

                    // Clear the widgets path
                    contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, "");

                } else {

                    // If the launch button was not pressed, append a new path segment and update widget
                    contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, rawPath + " " + index);

                }

                // Update the database with the new path
                context.getContentResolver().update(
                        DBContract.WidgetsEntry.buildWidgetWithAppWidgetId(appWidgetId),
                        contentValues,
                        null, null
                );

                // Update the widget, now that the widget has a new path
                updateWidgets(context, appWidgetId);

            }
            c.close();
        }
    }

    private static RemoteViews generateRemoteViewsFromNode(Context context, int appWidgetId, int buttonCount, int launchButtonIndex, boolean vertical, Node node) {

        ArrayList<Integer> buttonIds = SupportedWidgetSizes.getWidgetButtonIds(buttonCount);
        ArrayList<Integer> backgroundIds = SupportedWidgetSizes.getWidgetButtonBackgroundIds(buttonCount);
        RemoteViews remoteViews = SupportedWidgetSizes.getWidgetRemoteView(context, buttonCount, vertical);

        ButtonIconSet buttonIconSet = ButtonIconSetManager.getButtonIconSet(context, buttonCount);

        boolean launchPlaced = false;
        for (int i = 0; i < buttonCount; i++) {

            int relativeChildIndex = i - (launchPlaced ? 1 : 0);

            if (i == launchButtonIndex) {

                launchPlaced = true;

                // Set icon
                if (node.hasAction()) {
                    remoteViews.setImageViewUri(buttonIds.get(i), node.getAction().getActionImageUri());
                } else {
                    remoteViews.setImageViewBitmap(buttonIds.get(i), null);
                }

                // Set select highlight
                remoteViews.setInt(buttonIds.get(i), "setBackgroundResource", buttonIconSet.getButtonHighlightStateDrawableResourceId());

                // Set background
                remoteViews.setImageViewUri(backgroundIds.get(i), buttonIconSet.getLauncherUri());

                remoteViews.setOnClickPendingIntent(buttonIds.get(i), generateButtonPendingIntent(context, appWidgetId, -1));

            } else {

                Node childNode = node.getChild(relativeChildIndex);

                // Set icon
                if (childNode.hasAction()) {
                    remoteViews.setImageViewUri(buttonIds.get(i), childNode.getAction().getActionImageUri());
                } else {
                    remoteViews.setImageViewBitmap(buttonIds.get(i), null);
                }

                // Set select highlight
                remoteViews.setInt(buttonIds.get(i), "setBackgroundResource", buttonIconSet.getButtonHighlightStateDrawableResourceId());

                // Set background
                remoteViews.setImageViewUri(backgroundIds.get(i), buttonIconSet.getIcon(relativeChildIndex));

                remoteViews.setOnClickPendingIntent(buttonIds.get(i), generateButtonPendingIntent(context, appWidgetId, relativeChildIndex));

            }

        }

        return remoteViews;

    }

    private static PendingIntent generateButtonPendingIntent(Context context, int appWidgetId, int index) {
        Intent intent = new Intent(WidgetReceiver.ACTION_BUTTON_CLICKED);
        intent.putExtra(WidgetReceiver.EXTRA_INDEX, index);
        intent.putExtra(WidgetReceiver.EXTRA_APP_WIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId * 10 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Convert raw widgetPath to int[]
    private static ArrayList<Integer> convertStringToPath(String path) {

        ArrayList<Integer> out = new ArrayList<>();

        if (TextUtils.isEmpty(path))
            return out;

        String[] pathIndexes = path.split(" ");
        for (String x : pathIndexes) {
            out.add(Integer.parseInt(x));
        }

        return out;

    }

    public static void addWidgetToDB(Context context, int appWidgetId, long configurationId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.WidgetsEntry.APP_WIDGET_ID_COL, appWidgetId);
        contentValues.put(DBContract.WidgetsEntry.CONFIG_KEY_COL, configurationId);
        contentValues.put(DBContract.WidgetsEntry.WIDGET_CURRENT_PATH_COL, "");
        context.getContentResolver().insert(
                DBContract.WidgetsEntry.CONTENT_URI,
                contentValues
        );

    }

    public static void removeWidgetFromDB(Context context, int appWidgetId) {
        context.getContentResolver().delete(
                DBContract.WidgetsEntry.buildWidgetWithAppWidgetId(appWidgetId),
                null,
                null
        );
    }

}
