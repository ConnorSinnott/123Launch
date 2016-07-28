package com.pluviostudios.dialin.utilities;

import android.content.Context;

/**
 * Created by spectre on 7/26/16.
 */
public class ContextHelper {

    public static final String TAG = "ContextHelper";
    private static Context sContext;

    public static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

}
