package com.pluviostudios.dialin.action.defaultActions;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;

/**
 * Created by spectre on 7/31/16.
 */
public class ActionToggleFlashlight extends Action {

    public static final String TAG = "ActionToggleFlashlight";

    public ActionToggleFlashlight() {
        super("Toggle Flashlight", 2, null);
    }

    @Override
    public boolean onExecute() {
        return false;
    }

    @Override
    public boolean hasConfigurationFragment() {
        return false;
    }

    @Override
    public ConfigurationFragment getConfigurationFragment() {
        return null;
    }
}
