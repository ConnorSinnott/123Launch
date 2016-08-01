package com.pluviostudios.dialin.action;

import com.pluviostudios.dialin.action.defaultActions.ActionLaunchWebsite;
import com.pluviostudios.dialin.action.defaultActions.ActionToggleFlashlight;

import java.util.ArrayList;

/**
 * Created by spectre on 7/26/16.
 */
public class ActionManager {

    public static final String TAG = "ActionManager";

    // Ensure that items are added to actions in incremental order for ids
    public static ArrayList<Action> actions = new ArrayList<Action>() {
        {
            add(new ActionToggleFlashlight());
            add(new ActionLaunchWebsite());
        }
    };

    public static Action getInstanceOfAction(int actionId) {
        for (Action x : actions) {
            if (x.id == actionId)
                return x;
        }
        throw new RuntimeException("ActionId " + actionId + " not found");
    }

}
