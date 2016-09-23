package com.pluviostudios.dialin.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by spectre on 9/4/16.
 */
public class WidgetTools {

    public static final String TAG = "WidgetTools";

    public static boolean isWidgetVertical(Context context, int appWidgetId) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

    }

    public static int getTileCount(Context context, int appWidgetId) {

        //Determine button count
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxDimension = Math.max(width, height);
        return (maxDimension + 30) / 70; // Home Screen tile = n * 70 - 30

    }

}
