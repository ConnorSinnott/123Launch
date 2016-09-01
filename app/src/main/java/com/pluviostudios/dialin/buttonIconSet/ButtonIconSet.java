package com.pluviostudios.dialin.buttonIconSet;

import android.net.Uri;

/**
 * Created by spectre on 8/4/16.
 */
public class ButtonIconSet {

    public static final String TAG = "ButtonIconSet";

    private Uri[] mIconsUri;
    private Uri mLauncherUri;
    private int mButtonHighlightStateDrawableResourceId;
    private int mButtonHighlightResourceId;

    public ButtonIconSet(int buttonHighlightStateDrawable, int buttonHighlightResource, Uri launcherIcon, Uri... iconsUri) {
        mButtonHighlightStateDrawableResourceId = buttonHighlightStateDrawable;
        mButtonHighlightResourceId = buttonHighlightResource;
        mLauncherUri = launcherIcon;
        mIconsUri = iconsUri;
    }

    public Uri getIcon(int index) {
        return mIconsUri[index];
    }

    public Uri getLauncherUri() {
        return mLauncherUri;
    }

    public int getButtonHighlightStateDrawableResourceId() {
        return mButtonHighlightStateDrawableResourceId;
    }

    public int getButtonHighlightResourceId() {
        return mButtonHighlightResourceId;
    }

}
