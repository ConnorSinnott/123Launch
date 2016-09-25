package com.pluviostudios.onetwothreelaunch.utilities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.pluviostudios.onetwothreelaunch.action.ActionTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by spectre on 9/16/16.
 */
public class AsyncGetApplicationInfo extends AsyncTask<Void, Void, ArrayList<ApplicationInfo>> {

    private final Context mContext;

    public AsyncGetApplicationInfo(Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<ApplicationInfo> doInBackground(Void... voids) {

        ArrayList<ApplicationInfo> applicationInfoList = new ArrayList<>(mContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));

        // Filter Application info list to remove system applications and mark previous selection to be selected on init
        ArrayList<ApplicationInfo> listItems = new ArrayList<>();
        while (applicationInfoList.size() > 0) {

            ApplicationInfo appInfo = applicationInfoList.remove(0);
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || appInfo.icon == 0) {
                continue;
            }

            listItems.add(appInfo);

        }

        // Get Application info list and sort it alphabetically
        Collections.sort(listItems, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo applicationInfo, ApplicationInfo t1) {
                String leftName = ActionTools.getForeignApplicationNameFromInfo(mContext, applicationInfo);
                String rightName = ActionTools.getForeignApplicationNameFromInfo(mContext, t1);
                return leftName.toLowerCase().charAt(0) < rightName.toLowerCase().charAt(0) ? -1 : 1;
            }
        });

        return listItems;

    }

}
