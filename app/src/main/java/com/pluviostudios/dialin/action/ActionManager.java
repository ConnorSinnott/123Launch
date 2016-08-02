package com.pluviostudios.dialin.action;

import android.content.Context;

import com.pluviostudios.dialin.action.defaultActions.ActionLaunchWebsite;
import com.pluviostudios.dialin.action.defaultActions.ActionToggleFlashlight;

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
                    add(new ActionToggleFlashlight());
                    add(new ActionLaunchWebsite());
                }
            };
        }
        return sActions;
    }

    public static Action getInstanceOfAction(int actionId) {

        if (sContext != null) {
            for (Action x : getActions()) {
                if (x.id == actionId)
                    return x;
            }
            throw new RuntimeException("ActionId " + actionId + " not found");
        } else {
            throw new RuntimeException("ActionManager has not been initialized, please call ActionManager.initialize() first");
        }
    }

}
