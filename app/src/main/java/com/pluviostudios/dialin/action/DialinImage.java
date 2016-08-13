package com.pluviostudios.dialin.action;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.Serializable;

/**
 * Created by spectre on 7/26/16.
 */
public class DialinImage implements Serializable {

    public static final String TAG = "DialinImage";

    private Uri imageUri;

    public DialinImage(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public DialinImage(Context context, int imageResource) {
        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(imageResource) + '/' +
                context.getResources().getResourceTypeName(imageResource) + '/' +
                context.getResources().getResourceEntryName(imageResource));
    }

    public Uri getImageUri() {
        return imageUri;
    }

}
