package com.pluviostudios.dialin;

import android.support.v4.app.Fragment;

import com.pluviostudios.dialin.data.Node;

/**
 * Created by spectre on 7/27/16.
 */
public class EditFragment extends Fragment {

    public static final String TAG = "EditFragment";

    public static EditFragment buildEditFragment(Node node) {
        return new EditFragment();
    }

}
