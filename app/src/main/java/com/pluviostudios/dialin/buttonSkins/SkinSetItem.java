package com.pluviostudios.dialin.buttonSkins;

import android.net.Uri;

/**
 * Created by spectre on 9/13/16.
 */
public class SkinSetItem {

    public static final String TAG = "SkinSetItem";

    public final String title;
    public final int maxButtonCount;
    public final Uri[] buttonIconUris;
    public final Uri launcherIconUri;

    public SkinSetItem(String title, int maxButtonCount, Uri launcherIconUri, Uri... buttonIconUris) {
        this.title = title;
        this.maxButtonCount = maxButtonCount;
        this.buttonIconUris = buttonIconUris;
        this.launcherIconUri = launcherIconUri;
    }

}
