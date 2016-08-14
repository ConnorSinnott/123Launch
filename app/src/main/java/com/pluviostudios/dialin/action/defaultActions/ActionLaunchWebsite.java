package com.pluviostudios.dialin.action.defaultActions;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;

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
    public DialinImage getActionImage() {
        Intent i = (new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
        Uri preferredActionUri = ActionTools.getPrefferedApplicationIconUri(getContext(), i);
        return new DialinImage(preferredActionUri);
    }

    @Override
    public boolean onExecute() {

        String address = getActionArguments().get(0);

        if (!address.startsWith("http://") && !address.startsWith("https://"))
            address = "http://" + address;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(browserIntent);

        return true;

    }

    @Override
    public ConfigurationFragment buildConfigurationFragment() {
        return new ActionLaunchWebsiteConfigFragment();
    }

    public static class ActionLaunchWebsiteConfigFragment extends ConfigurationFragment {

        EditText mEditText;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable ArrayList<String> savedActionArguments) {

            mEditText = new EditText(getContext());
            mEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mEditText.setHint("Website URL");

            if (savedActionArguments != null) {
                String currentWebsite = savedActionArguments.get(0);
                mEditText.setText(currentWebsite);
            }

            return mEditText;

        }

        @Override
        public ArrayList<String> getActionArguments() {

            ArrayList<String> out = new ArrayList<>();
            out.add(mEditText.getText().toString());
            return out;

        }

    }

}
