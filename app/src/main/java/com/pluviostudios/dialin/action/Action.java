package com.pluviostudios.dialin.action;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by spectre on 7/26/16.
 */
public abstract class Action {

    public static final String TAG = "Action";

    private ArrayList<String> mActionArguments;
    private ConfigurationFragment mConfigurationFragment;

    public abstract int getActionId();

    public abstract String getActionName();

    public abstract DialinImage getActionImage();

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

    public ConfigurationFragment getConfigurationFragment() {
        if (mConfigurationFragment == null) {
            mConfigurationFragment = buildConfigurationFragment();
            if (mActionArguments != null) {
                mConfigurationFragment.setActionArguments(mActionArguments);
            }
        }
        return mConfigurationFragment;
    }

    public void saveArguments() {
        if (hasConfigurationFragment() && mConfigurationFragment != null) {
            setActionArguments(getConfigurationFragment().getActionArguments());
        }
    }

    public void setActionArguments(ArrayList<String> actionArguments) {
        this.mActionArguments = actionArguments;
    }

    public ArrayList<String> getActionArguments() {
        return mActionArguments;
    }

    protected Context getContext() {
        return ActionManager.getContext();
    }

}
