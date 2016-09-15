package com.pluviostudios.dialin.buttonSkins;

/**
 * Created by spectre on 9/13/16.
 */
public class HighlightItem {

    public static final String TAG = "HighlightItem";

    public final String title;
    public final int previewResourceId;
    public final int drawableResourceId;

    public HighlightItem(String title, int previewResourceId, int drawableResourceId) {
        this.title = title;
        this.previewResourceId = previewResourceId;
        this.drawableResourceId = drawableResourceId;
    }

}
