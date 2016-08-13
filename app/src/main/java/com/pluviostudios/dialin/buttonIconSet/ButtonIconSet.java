package com.pluviostudios.dialin.buttonIconSet;

import android.net.Uri;

import com.pluviostudios.dialin.action.DialinImage;

import java.io.Serializable;

/**
 * Created by spectre on 8/4/16.
 */
public class ButtonIconSet implements Serializable {

    public static final String TAG = "ButtonIconSet";

    private DialinImage[] mIcons;
    private DialinImage mLauncher;
    private int mButtonHighlightStateDrawableResourceId;
    private int mButtonHighlightResourceId;

    public ButtonIconSet(int buttonHighlightStateDrawable, int buttonHighlightResource, DialinImage launcherIcon, DialinImage... icons) {
        mButtonHighlightStateDrawableResourceId = buttonHighlightStateDrawable;
        mButtonHighlightResourceId = buttonHighlightResource;
        mLauncher = launcherIcon;
        mIcons = icons;
    }

    public Uri getIcon(int index) {
        return mIcons[index].getImageUri();
    }

    public Uri getLauncher() {
        return mLauncher.getImageUri();
    }

    public int getButtonHighlightStateDrawableResourceId() {
        return mButtonHighlightStateDrawableResourceId;
    }

    public int getButtonHighlightResourceId() {
        return mButtonHighlightResourceId;
    }

}
