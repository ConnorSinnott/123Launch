package com.pluviostudios.dialin.action;


import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * Created by spectre on 7/29/16.
 */
public abstract class ConfigurationFragment extends Fragment {

    public static final String TAG = "ConfigurationFragment";

    public abstract ArrayList<String> getActionArguements();

}
