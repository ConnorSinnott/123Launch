package com.pluviostudios.dialin.buttonIconSet;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;

import com.pluviostudios.dialin.R;

/**
 * Created by spectre on 8/6/16.
 */
public class ButtonIconSetManager {

    public static final String TAG = "ButtonIconSetManager";

    public static ButtonIconSet getButtonIconSet(Context context, int buttonCount) {

        final Drawable drawableBBlue = context.getResources().getDrawable(R.drawable.bblue);
        final Drawable drawableBGreen = context.getResources().getDrawable(R.drawable.bgreen);
        final Drawable drawableBPurple = context.getResources().getDrawable(R.drawable.bpurp);
        final Drawable drawableBPressed = context.getResources().getDrawable(R.drawable.bpressed);

        final Drawable drawableBLaunch = context.getResources().getDrawable(R.drawable.blaunch);
        final Drawable drawableBLaunchPressed = context.getResources().getDrawable(R.drawable.bpressedlaunch);

        final Pair<Drawable, Drawable> launchIconPair = new Pair<>(drawableBLaunch, drawableBLaunchPressed);
        final Pair<Drawable, Drawable> bluePair = new Pair<>(drawableBBlue, drawableBPressed);
        final Pair<Drawable, Drawable> greenPair = new Pair<>(drawableBGreen, drawableBPressed);
        final Pair<Drawable, Drawable> purplePair = new Pair<>(drawableBPurple, drawableBPressed);

        switch (buttonCount) {
            case 4:
                return new ButtonIconSet(launchIconPair, bluePair, greenPair, purplePair);
            case 5:
                return new ButtonIconSet(launchIconPair, bluePair, greenPair, purplePair, bluePair);
        }

        throw new RuntimeException("Unsupported button count");

    }

}

