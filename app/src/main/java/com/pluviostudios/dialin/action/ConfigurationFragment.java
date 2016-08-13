package com.pluviostudios.dialin.action;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by spectre on 7/29/16.
 */
public abstract class ConfigurationFragment extends Fragment {

    public static final String TAG = "ConfigurationFragment";

    public static final String EXTRA_ARGUMENTS_ARRAY = "extra_arguments";

    public void setActionArguments(ArrayList<String> actionArguments) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_ARGUMENTS_ARRAY, actionArguments);
        setArguments(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ArrayList<String> savedArguments = null;
        if (getArguments() != null) {
            Bundle extras = getArguments();
            if (extras.containsKey(EXTRA_ARGUMENTS_ARRAY)) {
                savedArguments = extras.getStringArrayList(EXTRA_ARGUMENTS_ARRAY);
            }
        }
        return onCreateView(inflater, container, savedArguments);
    }

    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable ArrayList<String> savedActionArguments);

    public abstract ArrayList<String> getActionArguments();

}
