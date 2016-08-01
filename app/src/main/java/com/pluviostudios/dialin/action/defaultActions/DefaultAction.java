package com.pluviostudios.dialin.action.defaultActions;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;

/**
 * Created by spectre on 7/31/16.
 */
public class DefaultAction extends Action {

    public static final String TAG = "DefaultAction";

    public DefaultAction() {
        super("No Action", 0);
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
