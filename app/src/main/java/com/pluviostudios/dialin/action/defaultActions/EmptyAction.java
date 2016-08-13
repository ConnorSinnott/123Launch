package com.pluviostudios.dialin.action.defaultActions;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;

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
        return "No Action";
    }

    @Override
    public DialinImage getActionImage() {
        return new DialinImage(getContext(), R.mipmap.ic_launcher);
    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return null;
    }

    @Override
    public boolean onExecute() {
        return false;
    }
}
