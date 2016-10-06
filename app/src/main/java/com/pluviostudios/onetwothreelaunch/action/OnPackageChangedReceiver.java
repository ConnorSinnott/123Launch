package com.pluviostudios.onetwothreelaunch.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.pluviostudios.onetwothreelaunch.action.defaultActions.ActionLaunchApplication;
import com.pluviostudios.onetwothreelaunch.data.Node;
import com.pluviostudios.onetwothreelaunch.data.StorageManager;
import com.pluviostudios.onetwothreelaunch.database.DBContract;
import com.pluviostudios.onetwothreelaunch.widget.WidgetManager;

import java.util.ArrayList;

/**
 * Created by spectre on 9/30/16.
 */
public class OnPackageChangedReceiver extends BroadcastReceiver {

    public static final String TAG = "PackageChangedReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {

        ActionManager.initialize(context);

        // Get package name from the intent
        String packageName = intent.getData().toString();

        // Remove "package:" from beginning of string
        packageName = packageName.substring(8, packageName.length());

        // Load all node trees and traverse through them for instances of the package
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

                    // Load root node
                    Node rootNode = StorageManager.loadNode(context, c.getLong(0));

                    NodeModifier nodeModifier;
                    switch (intent.getAction()) {

                        case Intent.ACTION_PACKAGE_FULLY_REMOVED: {

                            // Delete the action
                            nodeModifier = new NodeModifier() {
                                @Override
                                public void modifyNodeDependentOnPackage(Node nodeDependentOnPackage) {
                                    nodeDependentOnPackage.setAction(null);
                                }
                            };

                            break;
                        }

                        case Intent.ACTION_PACKAGE_REPLACED: {

                            // Update the action's icon
                            nodeModifier = new NodeModifier() {
                                @Override
                                public void modifyNodeDependentOnPackage(Node nodeDependentOnPackage) {

                                    ActionLaunchApplication actionLaunchApplication = (ActionLaunchApplication) nodeDependentOnPackage.getAction();
                                    actionLaunchApplication.invalidateApplicationData(context);
                                    nodeDependentOnPackage.setAction(actionLaunchApplication);

                                }
                            };

                            break;
                        }
                        default: {
                            throw new UnsupportedOperationException("OnPackageChangedReceiver received an action which it was not intended to received");
                        }

                    }

                    if (recursivelyApplyActionToNodeWithPackage(rootNode, packageName, nodeModifier)) {

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

    public static boolean recursivelyApplyActionToNodeWithPackage(Node node, String packageName, NodeModifier nodeModifier) {

        boolean packageFound = false;

        if (node.hasAction()) {

            Action nodeAction = node.getAction();
            if (nodeAction.getActionParameters() != null) {

                ArrayList<String> actionParameters = nodeAction.getActionParameters();

                for (String x : actionParameters) {

                    if (x.equals(packageName)) {

                        nodeModifier.modifyNodeDependentOnPackage(node);
                        packageFound = true;
                        break;

                    }
                }

            }

        }

        for (Integer x : node.getChildIndexes()) {
            if (recursivelyApplyActionToNodeWithPackage(node.getChild(x), packageName, nodeModifier)) {
                packageFound = true;
            }
        }

        return packageFound;


    }

    protected interface NodeModifier {

        void modifyNodeDependentOnPackage(Node nodeDependentOnPackage);

    }


}
