package com.pluviostudios.dialin.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/**
 * Created by spectre on 7/26/16.
 */
public class BaseWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "BaseWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        WidgetManager.updateWidgets(context, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int x : appWidgetIds) {
            WidgetManager.removeWidgetFromDB(context, x);
        }
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * Created by spectre on 7/26/16.
     */
    public static class Widget4x1Provider extends BaseWidgetProvider {

        public static final String TAG = "Widget4x1Provider";

    }

    /**
     * Created by spectre on 7/26/16.
     */
    public static class Widget5x1Provider extends BaseWidgetProvider {

        public static final String TAG = "Widget5x1Provider";

    }

}
