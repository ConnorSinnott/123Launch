package com.pluviostudios.dialin.action;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by spectre on 7/26/16.
 */
public class DialinImage {

    public static final String TAG = "DialinImage";

    public static DialinImage defaultActionImage = new DialinImage();

    public Uri imageUri;

    public DialinImage() {
        imageUri = Uri.EMPTY;
    }

    public DialinImage(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public DialinImage(Context context, int imageResource) {
        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(imageResource) + '/' +
                context.getResources().getResourceTypeName(imageResource) + '/' +
                context.getResources().getResourceEntryName(imageResource));
    }

}
