package com.pluviostudios.dialin.action;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by spectre on 7/26/16.
 */
public abstract class Action {

    public static final String TAG = "Action";

    private ArrayList<String> mActionParameters;
    private ConfigurationFragment mConfigurationFragment;

    public abstract int getActionId();

    public abstract String getActionName();

    public abstract Uri getActionImageUri();

    public boolean hasConfigurationFragment() {
        return buildConfigurationFragment() != null;
    }

    public abstract ConfigurationFragment buildConfigurationFragment();

    public abstract boolean onExecute();

    public boolean execute() {
        try {
            return onExecute();
        } catch (Exception e) {
            Log.e(TAG, "execute: Execution Failed", e);
            return false;
        }
    }

    public void setConfigurationFragment(ConfigurationFragment configurationFragment) {
        mConfigurationFragment = configurationFragment;
    }

    public ConfigurationFragment getConfigurationFragment() {
        if (mConfigurationFragment == null) {
            mConfigurationFragment = buildConfigurationFragment();
            if (mActionParameters != null) {
                mConfigurationFragment.setActionParameters(mActionParameters);
            }
        }
        return mConfigurationFragment;
    }

    public boolean saveParameters() {
        if (hasConfigurationFragment() && mConfigurationFragment != null) {
            setParameters(getConfigurationFragment().getActionParameters());
        }
        return mActionParameters != null;
    }

    public void setParameters(ArrayList<String> actionArguments) {
        this.mActionParameters = actionArguments;
    }

    public ArrayList<String> getActionParameters() {
        return mActionParameters;
    }

    protected Context getContext() {
        return ActionManager.getContext();
    }

    @Nullable
    public abstract String[] getRequiredPermissions();

}
