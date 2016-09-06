package com.pluviostudios.dialin.action.defaultActions;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.ConfigurationFragment;

/**
 * Created by spectre on 8/13/16.
 */
public class EmptyAction extends Action {

    public static final String TAG = "EmptyAction";

    @Override
    public int getActionId() {
        return -1;
    }

    @Override
    public String getActionName() {
        return "No Action (Click me!)";
    }

    @Override
    public Uri getActionImageUri() {
        return ActionTools.convertResourceToUri(getContext(), R.mipmap.ic_launcher);
    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return null;
    }

    @Override
    public boolean onExecute() {
        return false;
    }

    @Nullable
    @Override
    public String[] getRequiredPermissions() {
        return null;
    }
}
