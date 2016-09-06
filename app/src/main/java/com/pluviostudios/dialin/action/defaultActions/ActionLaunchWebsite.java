package com.pluviostudios.dialin.action.defaultActions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.ConfigurationFragment;

import java.util.ArrayList;

/**
 * Created by spectre on 7/31/16.
 */
public class ActionLaunchWebsite extends Action {

    public static final String TAG = "ActionLaunchWebsite";

    @Override
    public int getActionId() {
        return 1;
    }

    @Override
    public String getActionName() {
        return "Launch Website";
    }

    @Override
    public Uri getActionImageUri() {
        Intent i = (new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
        return ActionTools.getPreferredApplicationForIntent(getContext(), i);
    }

    @Override
    public boolean onExecute() {

        String address = getActionParameters().get(0);

        if (!address.startsWith("http://") && !address.startsWith("https://"))
            address = "http://" + address;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(browserIntent);

        return true;

    }

    @Nullable
    @Override
    public String[] getRequiredPermissions() {
        return null;
    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return new ActionLaunchWebsiteConfigFragment();
    }

    public static class ActionLaunchWebsiteConfigFragment extends ConfigurationFragment {

        EditText mEditText;

        @Override
        public int getParentActionId() {
            return 1;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            mEditText = new EditText(getContext());
            mEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mEditText.setHint("Website URL");

            Bundle data = null;
            if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PARAMETERS_ARRAY)) {
                data = savedInstanceState;
            } else if (getArguments() != null && getArguments().containsKey(EXTRA_PARAMETERS_ARRAY)) {
                data = getArguments();
            }

            if (data != null) {

                ArrayList<String> actionParameters = data.getStringArrayList(EXTRA_PARAMETERS_ARRAY);
                String currentWebsite = actionParameters.get(0);
                mEditText.setText(currentWebsite);

            }

            return mEditText;

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putStringArrayList(EXTRA_PARAMETERS_ARRAY, getActionParameters());
            super.onSaveInstanceState(outState);
        }

        @Override
        public ArrayList<String> getActionParameters() {

            ArrayList<String> out = new ArrayList<>();
            out.add(mEditText.getText().toString());
            return out;

        }

    }

}
