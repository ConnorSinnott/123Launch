package com.pluviostudios.dialin.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by spectre on 8/1/16.
 */
public class Utilities {

    public static final String TAG = "Utilities";

    public static void checkBundleForExpectedExtras(Bundle bundle, String... expectedExtras) {
        for (String expectedExtra : expectedExtras) {
            if (!bundle.containsKey(expectedExtra))
                throw new MissingExtraException(expectedExtra);
        }
    }

    public static BitmapDrawable[] generateBitmapDrawableArrayFromStringURI(Context context, String[] uris) {

        BitmapDrawable[] out = new BitmapDrawable[uris.length];

        try {
            for (int i = 0; i < uris.length; i++) {
                Uri bitmapUri = Uri.parse(uris[i]);
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), bitmapUri);
                out[i] = new BitmapDrawable(mBitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        return out;

    }
}
