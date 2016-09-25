package com.pluviostudios.onetwothreelaunch.widget;

import android.content.Context;
import android.widget.RemoteViews;

import com.pluviostudios.onetwothreelaunch.R;

import java.util.ArrayList;

/**
 * Created by spectre on 8/6/16.
 */
public class SupportedWidgetSizes {

    /**
     * To support an additional widget size, follow the steps below.
     * <p/>
     * 1. Make a copy of an existing widget_{count}x1_info xml document and set android:minWidth to {count} * 70 - 30
     * <p/>
     * 2. Create a new BaseWidgetProvider subclass class such as the ones found at the bottom of this file
     * <p/>
     * 3. Add the new BaseWidgetProvider to the manifest and have android:resource point to the xml document created in step 1
     * <p/>
     * 4. Make a copy of an existing widget_{count}x1 xml layout and add new image buttons with incrementing ids using the format "widget_button_{index}"
     * <p/>
     * 5. Update the function getWidgetButtonIds below, adding the new ids to a case statement relevant to the new size
     * <p/>
     * 6. Update the function getWidgetRemoteView below, adding the newly created xml layout in a case statement relevant to the new size
     */
    public final static int[] SUPPORTED_WIDGET_SIZES = new int[]{4, 5};

    public static ArrayList<Integer> getWidgetButtonIds(int buttonCount) {

        ArrayList<Integer> buttonIds = new ArrayList<>();

        if (buttonCount >= 4) {
            buttonIds.add(R.id.widget_button_1);
            buttonIds.add(R.id.widget_button_2);
            buttonIds.add(R.id.widget_button_3);
            buttonIds.add(R.id.widget_button_4);
        }

        if (buttonCount >= 5) {
            buttonIds.add(R.id.widget_button_5);
        }

        return buttonIds;

    }

    public static ArrayList<Integer> getWidgetButtonBackgroundIds(int buttonCount) {

        ArrayList<Integer> buttonIds = new ArrayList<>();

        if (buttonCount >= 4) {
            buttonIds.add(R.id.widget_background_1);
            buttonIds.add(R.id.widget_background_2);
            buttonIds.add(R.id.widget_background_3);
            buttonIds.add(R.id.widget_background_4);
        }

        if (buttonCount >= 5) {
            buttonIds.add(R.id.widget_background_5);
        }

        return buttonIds;

    }

    public static RemoteViews getWidgetRemoteView(Context context, int buttonCount) {

        RemoteViews remoteViews;
        switch (buttonCount) {

            case 4:
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);
                break;

            case 5:
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_5x1);
                break;

            // Ex.
            // case 6:
            //  remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_6x1);
            //  break;

            default:
                throw new RuntimeException("Layout for buttonCount " + buttonCount + " missing ");
        }

        return remoteViews;

    }

    /**
     * Created by spectre on 7/26/16.
     */
    public static class Widget4x1Provider extends BaseWidgetProvider {
    }

    /**
     * Created by spectre on 7/26/16.
     */
    public static class Widget5x1Provider extends BaseWidgetProvider {
    }

}
