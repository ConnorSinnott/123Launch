package com.pluviostudios.onetwothreelaunch.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;

import com.pluviostudios.onetwothreelaunch.action.ActionManager;

/**
 * Created by spectre on 7/26/16.
 */
public class BaseWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "BaseWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ActionManager.initialize(context);
        WidgetManager.updateWidgets(context, appWidgetIds);
    }

    // Pre-jellybean, options are created after widget is placed. So once options are created, the widget must be updated to ensure vertical works
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        WidgetManager.updateWidgets(context, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int x : appWidgetIds) {
            WidgetManager.removeWidgetFromDB(context, x);
        }
        super.onDeleted(context, appWidgetIds);
    }

}
