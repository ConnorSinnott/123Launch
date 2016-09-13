package com.pluviostudios.dialin.buttonIconSet;

import android.net.Uri;

/**
 * Created by spectre on 9/13/16.
 */
public class ButtonSetItem {

    public static final String TAG = "ButtonSetItem";

    public final String title;
    public final int maxButtonCount;
    public final Uri[] buttonIconUris;
    public final Uri launcherIconUri;

    public ButtonSetItem(String title, int maxButtonCount, Uri launcherIconUri, Uri... buttonIconUris) {
        this.title = title;
        this.maxButtonCount = maxButtonCount;
        this.buttonIconUris = buttonIconUris;
        this.launcherIconUri = launcherIconUri;
    }

}
