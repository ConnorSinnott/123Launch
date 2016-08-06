package com.pluviostudios.dialin.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pluviostudios.dialin.action.ActionManager;

/**
 * Created by spectre on 7/26/16.
 */
public class WidgetReceiver extends BroadcastReceiver {

    public static final String TAG = "WidgetReceiver";

    public static final String INTENT_TYPE = "123GoIntent"; // Must also be updated in manifest
    public static final String EXTRA_APPWIDGETID = "extra_appWidgetId";
    public static final String EXTRA_INDEX = "extra_index";

    // Called when the user clicks a widget's button
    @Override
    public void onReceive(Context context, Intent intent) {

        ActionManager.initialize(context);

        Log.d(TAG, "onReceive: On Button Clicked!");

        // Get the button index and appWidgetId
        int pathIndex = intent.getExtras().getInt(EXTRA_INDEX);
        int appWidgetId = intent.getExtras().getInt(EXTRA_APPWIDGETID);

        // Notify widget manager
        WidgetManager.onWidgetButtonClicked(context, appWidgetId, pathIndex);

    }

}
