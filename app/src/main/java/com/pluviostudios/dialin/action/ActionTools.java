package com.pluviostudios.dialin.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;

/**
 * Created by spectre on 8/13/16.
 */
public class ActionTools {

    public static final String TAG = "ActionTools";

    public static final Uri getPrefferedApplicationIconUri(Context context, Intent launcherIntent) {

        // Setting Icon to default browser icon
        Intent i = (new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
        ResolveInfo mInfo = context.getPackageManager().resolveActivity(i, 0);
        return getForeignApplicationImageUriFromInfo(mInfo.activityInfo.applicationInfo);

    }


    public static String getForeignApplicationNameFromInfo(Context context, ApplicationInfo info) {
        return info.loadLabel(context.getPackageManager()).toString();
    }

    public static Uri getForeignApplicationImageUriFromInfo(ApplicationInfo info) {
        if (info.icon != 0) {
            return Uri.parse("android.resource://" + info.packageName + "/" + info.icon);
        } else
            return null;
    }
}
