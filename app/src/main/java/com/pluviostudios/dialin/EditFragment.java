package com.pluviostudios.dialin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ConfigurationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 7/27/16.
 */
public class EditFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditFragment";
    public static final String EXTRA_ACTION = "extra_action";

    protected View mRoot;
    @BindView(R.id.fragment_edit_action_no_config_flipper) protected ViewFlipper mNoConfigFlipper;
    @BindView(R.id.fragment_edit_action_configure_frame) protected FrameLayout mConfigFragmentFrame;
    @BindView(R.id.fragment_edit_action_ok) protected Button mButtonOk;
    @BindView(R.id.fragment_edit_action_cancel) protected Button mButtonCancel;

    protected Action mAction;
    protected OnActionConfigured mOnActionConfigured;

    public static EditFragment buildEditFragment(Action action, OnActionConfigured onActionConfigured) {
        EditFragment newFragment = buildEditFragment(onActionConfigured);
        newFragment.mAction = action;
        return newFragment;
    }

    public static EditFragment buildEditFragment(OnActionConfigured onActionConfigured) {
        EditFragment newFragment = new EditFragment();
        newFragment.mOnActionConfigured = onActionConfigured;
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_edit_action, container, false);
        ButterKnife.bind(this, mRoot);

        // If Action is not specified, throw exception
        Bundle extras = getArguments();
        if (extras != null && extras.containsKey(EXTRA_ACTION)) {
            // Action was passed

            Action passedAction = (Action) getArguments().getSerializable(EXTRA_ACTION);

            // If the action uses a configuration fragment, display it
            if (passedAction != null && passedAction.hasConfigurationFragment()) {

                // Display the configuration frame
                mNoConfigFlipper.showNext();

                // Place configuration fragment into frame
                ConfigurationFragment configurationFragment = passedAction.getConfigurationFragment();

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_edit_action_configure_frame, configurationFragment, configurationFragment.TAG)
                        .commit();

            }

        }

        mButtonOk.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        return mRoot;

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_edit_action_ok:

                mAction.saveArguements();

                // On OK return the configured fragment using OnActionConfigured
                if (mOnActionConfigured != null) {
                    mOnActionConfigured.onActionConfigured(mAction);
                }

                break;

            case R.id.fragment_edit_action_cancel:

                // On Cancel, notify mOnActionConfigured
                if (mOnActionConfigured != null) {
                    mOnActionConfigured.onConfigurationCancelled();
                }
                break;

        }

    }

    public interface OnActionConfigured {
        void onActionConfigured(Action action);

        void onConfigurationCancelled();
    }

}
