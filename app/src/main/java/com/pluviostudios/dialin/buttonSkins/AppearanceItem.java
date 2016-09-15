package com.pluviostudios.dialin.buttonSkins;

/**
 * Created by spectre on 9/13/16.
 */
public class AppearanceItem {

    public static final String TAG = "AppearanceItem";

    public HighlightItem highlightItem;
    public SkinSetItem skinSetItem;

    public AppearanceItem(HighlightItem highlightItem, SkinSetItem skinSetItem) {
        this.highlightItem = highlightItem;
        this.skinSetItem = skinSetItem;
    }

}
