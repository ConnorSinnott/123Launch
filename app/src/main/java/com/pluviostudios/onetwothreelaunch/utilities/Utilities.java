package com.pluviostudios.onetwothreelaunch.utilities;

import android.os.Bundle;

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

}
