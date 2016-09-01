package com.pluviostudios.dialin.utilities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluviostudios.dialin.R;

/**
 * Created by spectre on 8/16/16.
 */
public class HelpDialogFragment extends DialogFragment {

    public static final String TAG = "HelpDialogFragment";

    private View mRoot;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.dialog_help, container, false);
        return mRoot;
    }

}
