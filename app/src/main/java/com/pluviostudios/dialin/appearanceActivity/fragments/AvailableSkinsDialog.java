package com.pluviostudios.dialin.appearanceActivity.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluviostudios.dialin.R;

/**
 * Created by spectre on 8/15/16.
 */
public class AvailableSkinsDialog extends DialogFragment {

    public static final String TAG = "AvailableSkinsDialog";

    private View mRoot;
    boolean isDialog = false;
    private OnSkinSelected mOnSkinSelected;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_available_skins, container, false);

        return mRoot;
    }

    public void setOnSkinSelected(OnSkinSelected onSkinSelected) {
        mOnSkinSelected = onSkinSelected;
    }

    public interface OnSkinSelected {

        void onInternetSkinDownload();

        void onSkinSelected();

    }


}
