package com.pluviostudios.dialin.action;

import android.content.Context;
import android.util.Log;

import com.pluviostudios.dialin.action.defaultActions.ActionLaunchApplication;
import com.pluviostudios.dialin.action.defaultActions.ActionLaunchWebsite;
import com.pluviostudios.dialin.action.defaultActions.ActionToggleFlashlight;
import com.pluviostudios.dialin.action.defaultActions.EmptyAction;

import java.util.ArrayList;

/**
 * Created by spectre on 7/26/16.
 */
public class ActionManager {

    public static final String TAG = "ActionManager";

    public static Context sContext;

    public static void initialize(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

    public static ArrayList<Action> sActions = null;

    // Ensure that items are added to actions in incremental order for ids
    public static ArrayList<Action> getActions() {
        if (sActions == null) {
            sActions = new ArrayList<Action>() {
                {
                    add(new EmptyAction());
                    add(new ActionToggleFlashlight());
                    add(new ActionLaunchWebsite());
                    add(new ActionLaunchApplication());
                }
            };
        }
        return sActions;
    }

    public static Action getInstanceOfAction(int actionId, ArrayList<String> actionArguments) {

        if (sContext != null) {
            for (Action x : getActions()) {
                if (x.getActionId() == actionId)
                    try {
                        Action newAction = x.getClass().newInstance();
                        if (actionArguments != null) {
                            newAction.setParameters(actionArguments);
                        }
                        return newAction;
                    } catch (InstantiationException e) {
                        Log.e(TAG, "getInstanceOfAction: Error creating new instance of " + x.getActionName(), e);
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
            }
            throw new RuntimeException("ActionId " + actionId + " not found");
        } else {
            throw new RuntimeException("ActionManager has not been initialized, please call ActionManager.initialize() first");
        }

    }

    public static Action getInstanceOfAction(int actionId) {
        return getInstanceOfAction(actionId, null);
    }

}
