package com.pluviostudios.dialin.action;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * Created by spectre on 7/29/16.
 */
public abstract class ConfigurationFragment extends Fragment {

    public static final String TAG = "ConfigurationFragment";

    public static final String EXTRA_PARAMETERS_ARRAY = "extra_parameters";

    public void setActionParameters(ArrayList<String> actionParameters) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_PARAMETERS_ARRAY, actionParameters);
        setArguments(bundle);
    }
    public abstract ArrayList<String> getActionParameters();

    public abstract int getParentActionId();

}
