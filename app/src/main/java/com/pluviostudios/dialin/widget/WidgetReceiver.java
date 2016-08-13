package com.pluviostudios.dialin.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pluviostudios.dialin.action.ActionManager;

/**
 * Created by spectre on 7/26/16.
 */
public class WidgetReceiver extends BroadcastReceiver {

    public static final String TAG = "WidgetReceiver";

    public static final String ACTION_BUTTON_CLICKED = "123GoIntent"; // Must also be updated in manifest

    public static final String EXTRA_APP_WIDGET_ID = "extra_appWidgetId";
    public static final String EXTRA_INDEX = "extra_index";

    // Called when the user clicks a widget's button
    @Override
    public void onReceive(Context context, Intent intent) {

        ActionManager.initialize(context);

        switch (intent.getAction()) {
            case ACTION_BUTTON_CLICKED:

                // Get the button index and appWidgetId
                int pathIndex = intent.getExtras().getInt(EXTRA_INDEX);
                int appWidgetId = intent.getExtras().getInt(EXTRA_APP_WIDGET_ID);

                // Notify widget manager
                WidgetManager.onWidgetButtonClicked(context, appWidgetId, pathIndex);

                break;

        }

    }

}
