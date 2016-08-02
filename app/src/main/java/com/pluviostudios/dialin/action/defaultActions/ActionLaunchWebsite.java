package com.pluviostudios.dialin.action.defaultActions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;

import java.util.ArrayList;

/**
 * Created by spectre on 7/31/16.
 */
public class ActionLaunchWebsite extends Action {

    public static final String TAG = "ActionLaunchWebsite";

    public ActionLaunchWebsite() {
        super("Launch Website", 1, new DialinImage(ActionManager.getContext(), R.drawable.chrome_icon));
    }

    @Override
    public boolean onExecute() {

        String address = actionArguments.get(0);

        if (!address.startsWith("http://") && !address.startsWith("https://"))
            address = "http://" + address;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActionManager.getContext().startActivity(browserIntent);

        return true;

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
