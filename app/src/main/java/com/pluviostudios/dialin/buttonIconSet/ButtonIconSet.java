package com.pluviostudios.dialin.buttonIconSet;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.util.Pair;

import java.io.Serializable;

/**
 * Created by spectre on 8/4/16.
 */
public class ButtonIconSet implements Serializable {

    public static final String TAG = "ButtonIconSet";

    private Pair<Drawable, Drawable>[] mIconPairs;
    private Pair<Drawable, Drawable> mLauncherPairs;

    public ButtonIconSet(Pair<Drawable, Drawable> launcherIcon, Pair<Drawable, Drawable>... icons) {
        mLauncherPairs = launcherIcon;
        mIconPairs = icons;
    }

    public Drawable getLauncherIcon(boolean pressed) {
        return pressed ? mLauncherPairs.second : mLauncherPairs.first;
    }

    public Drawable getIcon(int index, boolean pressed) {
        Pair<Drawable, Drawable> iconPair = mIconPairs[index];
        return pressed ? iconPair.second : iconPair.first;
    }

    public StateListDrawable getLauncherIconStateDrawable() {
        return generateStateListDrawable(mLauncherPairs);
    }

    public StateListDrawable getButtonIconStateDrawable(int index) {
        Pair<Drawable, Drawable> iconPair = mIconPairs[index];
        return generateStateListDrawable(iconPair);
    }

    private static StateListDrawable generateStateListDrawable(Pair<Drawable, Drawable> iconPair) {
        StateListDrawable newStateListDrawable = new StateListDrawable();
        newStateListDrawable.addState(new int[]{android.R.attr.state_pressed}, iconPair.second); // Clicked
        newStateListDrawable.addState(new int[]{android.R.attr.state_enabled}, iconPair.first); // Released
        return newStateListDrawable;
    }

}
