package com.pluviostudios.dialin.mainActivity;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;

import java.io.Serializable;

/**
 * Created by spectre on 8/4/16.
 */
public class ButtonIconSet implements Serializable {

    public static final String TAG = "ButtonIconSet";

    private StateListDrawable launcherIcon;
    private StateListDrawable[] icons;

    public ButtonIconSet(StateListDrawable launcherIcon, StateListDrawable... icons) {
        this.launcherIcon = launcherIcon;
        this.icons = icons;
    }

    public ButtonIconSet(BitmapDrawable launcherDefault, BitmapDrawable launcherPressed, BitmapDrawable[] iconsDefault, BitmapDrawable[] iconsPressed) {

        if (iconsDefault.length != iconsPressed.length)
            throw new IllegalArgumentException("Default icon count differs from pressed icon count");

        icons = new StateListDrawable[iconsDefault.length];

        launcherIcon = generateStateListDrawable(launcherDefault, launcherPressed);
        for (int i = 0; i < iconsDefault.length; i++) {
            icons[i] = generateStateListDrawable(iconsDefault[i], iconsPressed[i]);
        }

    }

    private static StateListDrawable generateStateListDrawable(BitmapDrawable defaultIcon, BitmapDrawable holdIcon) {
        StateListDrawable newStateListDrawable = new StateListDrawable();
        newStateListDrawable.addState(new int[]{android.R.attr.state_enabled}, defaultIcon); // Released
        newStateListDrawable.addState(new int[]{android.R.attr.state_pressed}, holdIcon); // Clicked
        return newStateListDrawable;
    }

    public StateListDrawable getLauncherIcon() {
        return launcherIcon;
    }

    public StateListDrawable getButtonIcon(int index) {
        return icons[index];
    }

}
