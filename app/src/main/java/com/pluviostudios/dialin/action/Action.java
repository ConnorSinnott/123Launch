package com.pluviostudios.dialin.action;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by spectre on 7/26/16.
 */
public abstract class Action {

    public static final String TAG = "Action";

    private ArrayList<String> mActionParameters;

    public abstract int getActionId();

    public abstract String getActionName();

    public abstract Uri getActionImageUri();

    public abstract boolean onExecute();

    public boolean execute() {
        try {
            return onExecute();
        } catch (Exception e) {
            Log.e(TAG, "execute: Execution Failed", e);
            return false;
        }
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

}
