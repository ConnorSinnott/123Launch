package com.pluviostudios.dialin.buttonIconSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionTools;

import java.util.ArrayList;

/**
 * Created by spectre on 9/13/16.
 */
public class AppearanceManager {

    public static final String TAG = "AppearanceManager";

    private static final String PREF_CURRENT_HIGHLIGHT = "current_highlight_title";
    private static final String PREF_CURRENT_BUTTON_SET = "current_button_set_title";

    private static AppearanceItem sAppearanceItem;
    private static ArrayList<HighlightItem> mHighlights;
    private static ArrayList<ButtonSetItem> mButtonSets;

    public static AppearanceItem getAppearanceItem(Context context) {

        if (sAppearanceItem == null) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (sharedPreferences.contains(PREF_CURRENT_BUTTON_SET) && sharedPreferences.contains(PREF_CURRENT_HIGHLIGHT)) {

                String savedHighlightTitle = sharedPreferences.getString(PREF_CURRENT_HIGHLIGHT, "Orange");
                String savedButtonSetTitle = sharedPreferences.getString(PREF_CURRENT_BUTTON_SET, "Holo");

                HighlightItem currentHighlightItem = null;
                ButtonSetItem currentButtonSetItem = null;

                for (HighlightItem x : getHighlightItems(context)) {
                    if (x.title.equals(savedHighlightTitle)) {
                        currentHighlightItem = x;
                        break;
                    }
                }

                for (ButtonSetItem x : getButtonSets(context)) {
                    if (x.title.equals(savedButtonSetTitle)) {
                        currentButtonSetItem = x;
                        break;
                    }
                }

                if (currentButtonSetItem == null)
                    throw new RuntimeException("ButtonSetItem with title " + savedButtonSetTitle + " could not be loaded");

                if (currentHighlightItem == null) {
                    throw new RuntimeException("HighlightItem with title " + savedHighlightTitle + " could not be loaded");
                }

                sAppearanceItem = new AppearanceItem(currentHighlightItem, currentButtonSetItem);

            } else {

                sAppearanceItem = new AppearanceItem(getHighlightItems(context).get(0), getButtonSets(context).get(0));

            }

        }

        return sAppearanceItem;

    }

    public static void setAppearanceItem(Context context, AppearanceItem appearanceItem) {

        sAppearanceItem = appearanceItem;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .putString(PREF_CURRENT_HIGHLIGHT, appearanceItem.highlightItem.title)
                .putString(PREF_CURRENT_BUTTON_SET, appearanceItem.buttonSetItem.title)
                .apply();

    }

    public static ArrayList<HighlightItem> getHighlightItems(final Context context) {
        if (mHighlights == null) {
            mHighlights = new ArrayList<HighlightItem>() {{
                add(new HighlightItem("Orange", R.drawable.ic_select_orange, R.drawable.button_selected_orange));
                add(new HighlightItem("Blue", R.drawable.ic_select_blue, R.drawable.button_selected_blue));
                add(new HighlightItem("Green", R.drawable.ic_select_green, R.drawable.button_selected_green));
                add(new HighlightItem("Red", R.drawable.ic_select_red, R.drawable.button_selected_red));
                add(new HighlightItem("White", R.drawable.ic_select_white, R.drawable.button_selected_white));
            }};
        }
        return mHighlights;
    }

    public static ArrayList<ButtonSetItem> getButtonSets(final Context context) {
        if (mButtonSets == null) {
            mButtonSets = new ArrayList<ButtonSetItem>() {{

                Uri button1 = ActionTools.convertResourceToUri(context, R.drawable.ic_button_holo_1);
                Uri button2 = ActionTools.convertResourceToUri(context, R.drawable.ic_button_holo_2);
                Uri button3 = ActionTools.convertResourceToUri(context, R.drawable.ic_button_holo_3);
                Uri button4 = ActionTools.convertResourceToUri(context, R.drawable.ic_button_holo_4);
                Uri buttonLaunch = ActionTools.convertResourceToUri(context, R.drawable.ic_blaunch);

                add(new ButtonSetItem("Holo", 5, buttonLaunch, button1, button2, button3, button4));

            }};
        }
        return mButtonSets;
    }


}
