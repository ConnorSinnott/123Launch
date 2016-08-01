package com.pluviostudios.dialin.action.defaultActions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;

import java.util.ArrayList;

/**
 * Created by spectre on 7/31/16.
 */
public class ActionLaunchWebsite extends Action {

    public static final String TAG = "ActionLaunchWebsite";

    public ActionLaunchWebsite(String name, int id, DialinImage actionImage) {
        super("Launch Website", 1, null);
    }

    @Override
    public boolean onExecute() {
        return false;
    }

    @Override
    public boolean hasConfigurationFragment() {
        return true;
    }

    @Override
    public ConfigurationFragment getConfigurationFragment() {
        return new ActionLaunchWebsiteConfigFragment();
    }

    public static class ActionLaunchWebsiteConfigFragment extends ConfigurationFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public ArrayList<String> getActionArguements() {
            return null;
        }

    }

}
