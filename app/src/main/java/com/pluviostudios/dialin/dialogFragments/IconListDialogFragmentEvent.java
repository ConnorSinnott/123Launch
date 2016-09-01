package com.pluviostudios.dialin.dialogFragments;

/**
 * Created by spectre on 8/19/16.
 */
public class IconListDialogFragmentEvent {

    public static final String TAG = "IconListDialogFragmentEvent";

    public final int position;
    public final int requestCode;

    public IconListDialogFragmentEvent(int requestCode, int position) {
        this.position = position;
        this.requestCode = requestCode;
    }

}
