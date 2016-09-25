package com.pluviostudios.onetwothreelaunch.action.defaultActions;

import android.content.Intent;
import android.net.Uri;

import com.pluviostudios.onetwothreelaunch.R;
import com.pluviostudios.onetwothreelaunch.action.Action;
import com.pluviostudios.onetwothreelaunch.action.ActionTools;

import java.util.ArrayList;

/**
 * Created by spectre on 8/10/16.
 */
public class ActionLaunchApplication extends Action {

    public static final String TAG = "ActionLaunchApplication";

    private static final int INDEX_APPLICATION_PACKAGE_NAME = 0;
    private static final int INDEX_APPLICATION_NAME = 1;
    private static final int INDEX_APPLICATION_ICON_URI = 2;

    private String mApplicationName = null;
    private Uri mApplicationIcon = null;

    @Override
    public int getActionId() {
        return 0;
    }

    @Override
    public String getActionName() {
        if (mApplicationName == null) {
            return "Launch Application";
        } else {
            return getContext().getString(R.string.launch, mApplicationName);
        }
    }

    @Override
    public Uri getActionImageUri() {
        if (mApplicationIcon == null) {
            return ActionTools.convertResourceToUri(getContext(), R.drawable.ic_blaunch);
        } else {
            return mApplicationIcon;
        }
    }

    @Override
    public boolean onExecute() {

        Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(getActionParameters().get(INDEX_APPLICATION_PACKAGE_NAME));
        getContext().startActivity(launchIntent);
        return true;

    }

    @Override
    public void setParameters(ArrayList<String> actionParameters) {
        super.setParameters(actionParameters);
        if (actionParameters.size() > 0) {
            mApplicationName = getActionParameters().get(INDEX_APPLICATION_NAME);
            mApplicationIcon = Uri.parse(getActionParameters().get(INDEX_APPLICATION_ICON_URI));
        }
    }

}

