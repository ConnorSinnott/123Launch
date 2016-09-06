package com.pluviostudios.dialin.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.widget.WidgetManager;

import java.util.ArrayList;

/**
 * Created by spectre on 9/1/16.
 */
public class OnPackageUninstall extends BroadcastReceiver {

    //Todo make a volatile column on config which will list items that are outside of the applications control

    public static final String TAG = "OnPackageUninstall";

    @Override
    public void onReceive(Context context, Intent intent) {

        String packageName = intent.getData().toString();

        // Remove "package:" from beginning of string
        packageName = packageName.substring(8, packageName.length());

        final String[] projection = new String[]{
                DBContract.ConfigEntry._ID,
                DBContract.ConfigEntry.TITLE_COL,
                DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX
        };

        Cursor c = context.getContentResolver().query(
                DBContract.ConfigEntry.CONTENT_URI,
                projection,
                null, null, null, null);

        ArrayList<Long> affectedConfigIds = new ArrayList<>();

        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    Node rootNode = StorageManager.loadNode(context, c.getLong(0));
                    if (recursivelyDeleteActionWithPackageName(rootNode, packageName)) {

                        StorageManager.saveConfiguration(context, c.getLong(0), c.getString(1), c.getInt(2), rootNode);
                        affectedConfigIds.add(c.getLong(0));

                    }

                } while (c.moveToNext());
            }
            c.close();
        }

        for (long x : affectedConfigIds) {
            ArrayList<Integer> affectedAppWidgetIds = WidgetManager.getWidgetsUsingConfig(context, x);
            WidgetManager.updateWidgets(context, affectedAppWidgetIds);
        }

    }

    private boolean recursivelyDeleteActionWithPackageName(Node node, String packageName) {

        boolean actionRemoved = false;

        if (node.hasAction()) {

            Action nodeAction = node.getAction();
            if (nodeAction.getActionParameters() != null) {

                ArrayList<String> actionParameters = nodeAction.getActionParameters();

                for (String x : actionParameters) {

                    if (x.equals(packageName)) {

                        node.setAction(null);
                        actionRemoved = true;
                        break;

                    }
                }

            }

        }

        for (Integer x : node.getChildIndexes()) {
            if (recursivelyDeleteActionWithPackageName(node.getChild(x), packageName)) {
                actionRemoved = true;
            }
        }

        return actionRemoved;

    }


}
