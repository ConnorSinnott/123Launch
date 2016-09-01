package com.pluviostudios.dialin.buttonIconSet;

import android.content.Context;
import android.net.Uri;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionTools;

/**
 * Created by spectre on 8/6/16.
 */
public class ButtonIconSetManager {

    public static final String TAG = "ButtonIconSetManager";

    public static ButtonIconSet getButtonIconSet(Context context, int buttonCount) {

        final Uri bBlue = ActionTools.convertResourceToUri(context, R.drawable.ic_bblue);
        final Uri bGreen = ActionTools.convertResourceToUri(context, R.drawable.ic_bgreen);
        final Uri bPurple = ActionTools.convertResourceToUri(context, R.drawable.ic_bpurp);
        final Uri bLaunch = ActionTools.convertResourceToUri(context, R.drawable.ic_blaunch);

        switch (buttonCount) {
            case 4:
                return new ButtonIconSet(R.drawable.button_selected_orange, R.drawable.ic_highlight_orange, bLaunch, bBlue, bGreen, bPurple);
            case 5:
                return new ButtonIconSet(R.drawable.button_selected_orange, R.drawable.ic_highlight_orange, bLaunch, bBlue, bGreen, bPurple, bBlue);
        }

        throw new RuntimeException("Unsupported button count");

    }

}

