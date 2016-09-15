package com.pluviostudios.dialin.data;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.widget.WidgetManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by spectre on 8/4/16.
 */
public class StorageManager {

    public static final String TAG = "StorageManager";

    public static final String EXTRA_AFFECTED_APPWIDGETIDS = "extra_affected_appWidgetIds";
    public static final String EXTRA_SUCCESS = "extra_result";

    public static Bundle saveNewConfiguration(Context context, String configurationTitle, int buttonCount, int launchButtonIndex, Node node) {

        // Create a bundle which will hold information about how the save went
        Bundle data = new Bundle();

        try {

            // Create contentValues which will be used insert data into the DB
            ContentValues newValues = new ContentValues();
            newValues.put(DBContract.ConfigEntry.DATE_MODIFIED, Calendar.getInstance().getTimeInMillis());
            newValues.put(DBContract.ConfigEntry.TITLE_COL, configurationTitle);
            newValues.put(DBContract.ConfigEntry.BUTTON_COUNT_COL, buttonCount);
            newValues.put(DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX, launchButtonIndex);

            // Insert the new row into database and get the new configuration id
            Long sCurrentConfigId = DBContract.ConfigEntry.getIdFromUri(
                    context.getContentResolver().insert(
                            DBContract.ConfigEntry.CONTENT_URI,
                            newValues
                    )
            );

            // Convert node to JSON and save
            String generatedJSON = JSONNodeConverter.convertNodeToJSON(node);
            FileManager.writeToFile(context, String.valueOf(sCurrentConfigId), generatedJSON);

            data.putBoolean(EXTRA_SUCCESS, true);

        } catch (JSONException e) {
            Log.e(TAG, "saveConfiguration: Unable to save configuration due to problems converting node tree to JSON", e);
        } catch (IOException e) {
            Log.e(TAG, "saveConfiguration: Unable to save configurations due to problems loading config from storage", e);
        }

        data.putBoolean(EXTRA_SUCCESS, false);
        return data;


    }

    public static Bundle saveConfiguration(Context context, long configurationId, String configurationTitle, int launchButtonIndex, Node node) {

        // Create a bundle which will hold information about how the save went
        Bundle data = new Bundle();

        try {

            // Convert node to JSON and save
            String generatedJSON = JSONNodeConverter.convertNodeToJSON(node);
            FileManager.writeToFile(context, String.valueOf(configurationId), generatedJSON);

            // Get a list of appWidgetIds who are currently using this configuration and place into bundle
            ArrayList<Integer> affectedAppWidgetIds = WidgetManager.getWidgetsUsingConfig(context, configurationId);
            data.putIntegerArrayList(EXTRA_AFFECTED_APPWIDGETIDS, affectedAppWidgetIds);

            // Update the DB with DATE_MODIFIED
            final String selection = DBContract.ConfigEntry._ID + "=?";
            final String[] selectionArgs = new String[]{String.valueOf(configurationId)};

            // Create contentValues to update the database
            ContentValues newValues = new ContentValues();
            newValues.put(DBContract.ConfigEntry.DATE_MODIFIED, Calendar.getInstance().getTimeInMillis());
            newValues.put(DBContract.ConfigEntry.TITLE_COL, configurationTitle);
            newValues.put(DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX, launchButtonIndex);
            context.getContentResolver().update(
                    DBContract.ConfigEntry.CONTENT_URI,
                    newValues,
                    selection,
                    selectionArgs
            );


            data.putBoolean(EXTRA_SUCCESS, true);

        } catch (JSONException e) {
            Log.e(TAG, "saveConfiguration: Unable to save configuration due to problems converting node tree to JSON", e);
        } catch (IOException e) {
            Log.e(TAG, "saveConfiguration: Unable to save configurations due to problems loading config from storage", e);
        }

        data.putBoolean(EXTRA_SUCCESS, false);
        return data;

    }

    public static ArrayList<Integer> deleteConfiguration(Context context, long configurationId) {

        context.getContentResolver().delete(
                DBContract.ConfigEntry.buildConfigWithId(configurationId),
                null,
                null
        );

        return WidgetManager.getWidgetsUsingConfig(context, configurationId);

    }

    public static Node loadNode(Context context, long configurationId) {

        try {

            String savedJson = FileManager.readFromFile(context, String.valueOf(configurationId));
            return JSONNodeConverter.convertJSONToNodeTree(savedJson);

        } catch (JSONException e) {
            Log.e(TAG, "loadNode: Unable to load configuration due to problems converting JSON to node tree", e);
        } catch (IOException e) {
            Log.e(TAG, "loadNode: Configuration file exists but is unable to be read", e);
        }

        return null;

    }

    public static Node loadNodeForWidget(Context context, long configurationId, ArrayList<Integer> path) {

        try {

            String savedJson = FileManager.readFromFile(context, String.valueOf(configurationId));
            return JSONNodeConverter.loadNodeByPath(savedJson, path);

        } catch (JSONException e) {
            Log.e(TAG, "loadNode: Unable to load configuration due to problems converting JSON to node tree", e);
        } catch (IOException e) {
            Log.e(TAG, "loadNode: Configuration file exists but is unable to be read", e);
        }

        return null;
    }


}
