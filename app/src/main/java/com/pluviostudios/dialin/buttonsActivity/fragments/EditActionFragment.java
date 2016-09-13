package com.pluviostudios.dialin.buttonsActivity.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.defaultActions.EmptyAction;
import com.pluviostudios.dialin.buttonsActivity.OnRequestPermissionResultEvent;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragmentEvent;
import com.pluviostudios.dialin.utilities.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

/**
 * Created by spectre on 7/27/16.
 */
public class EditActionFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditActionFragment";
    public static final String EXTRA_ACTION_ID = "extra_action_id";
    public static final String EXTRA_ACTION_ARGUMENTS = "extra_action_arguments";

    public static final int DIALOG_REQUEST_CODE = 1987;

    private View mRoot;
    private TextView mNoConfigText;
    private View mEditActionListItem;
    private ImageView mListItemActionImageView;
    private TextView mListItemActionTextView;
    private Button mButtonOk;
    private Button mButtonCancel;

    protected Action mAction;

    public static EditActionFragment buildEditFragment() {
        EditActionFragment newFragment = new EditActionFragment();
        return newFragment;
    }

    public static EditActionFragment buildEditFragment(Action action) {

        EditActionFragment editFragment = buildEditFragment();

        int actionId = action.getActionId();
        ArrayList<String> actionArguments = action.getActionParameters();

        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ACTION_ID, actionId);
        extras.putStringArrayList(EXTRA_ACTION_ARGUMENTS, actionArguments);

        editFragment.setArguments(extras);

        return editFragment;

    }

    private void initialize() {
        mNoConfigText = (TextView) mRoot.findViewById(R.id.fragment_edit_action_no_config);
        mEditActionListItem = mRoot.findViewById(R.id.fragment_edit_appearance_list_item);
        mListItemActionImageView = (ImageView) mRoot.findViewById(R.id.list_item_action_image);
        mListItemActionTextView = (TextView) mRoot.findViewById(R.id.list_item_action_text_view);
        mButtonOk = (Button) mRoot.findViewById(R.id.fragment_edit_action_ok);
        mButtonCancel = (Button) mRoot.findViewById(R.id.fragment_edit_action_cancel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_edit_action, container, false);
        initialize();

        Bundle extras = null;
        if (savedInstanceState != null) {
            extras = savedInstanceState;
        } else {
            if (getArguments() != null) {
                extras = getArguments();
            }
        }

        if (extras != null) {

            // If there are extras it means that an Action info was passed
            Utilities.checkBundleForExpectedExtras(extras,
                    EXTRA_ACTION_ID
            );

            // Get the actionID and create an instance of the action
            int actionId = extras.getInt(EXTRA_ACTION_ID);
            mAction = ActionManager.getInstanceOfAction(actionId);

            // Add arguments if they exist
            if (extras.containsKey(EXTRA_ACTION_ARGUMENTS)) {
                ArrayList<String> actionArguments = extras.getStringArrayList(EXTRA_ACTION_ARGUMENTS);
                mAction.setParameters(actionArguments);
            }

        } else {

            //Otherwise start with default action
            mAction = new EmptyAction();

        }

        // Update the view that shows the action name and its icon
        updateCurrentAction();

        mEditActionListItem.setOnClickListener(this);
        mButtonOk.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        return mRoot;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAction != null) {
            outState.putInt(EXTRA_ACTION_ID, mAction.getActionId());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void updateCurrentAction() {

        // Update the header with action name and icon
        mListItemActionTextView.setText(mAction.getActionName());
        mListItemActionImageView.setImageURI(mAction.getActionImageUri());

        // If the action does not have a configuration fragment, than delete the currently displayed fragment, it does not belong there anymore
        if (!mAction.hasConfigurationFragment()) {
            clearConfigFragment();
        } else {

            // Otherwise, there should be a fragment being displayed, so clear up the frame
            mNoConfigText.setVisibility(View.GONE);

            // Get the currently displayed configuration fragment if it exists
            ConfigurationFragment currentlyDisplayedFragment = (ConfigurationFragment) getChildFragmentManager().findFragmentById(R.id.fragment_edit_action_configure_frame);

            // If there is currently a configuration fragment being displayed, determine if it belongs to the action
            if (currentlyDisplayedFragment != null) {

                // Check to see if the fragment being displayed belongs to the action, or if it exists because the user was looking at a different action
                if (currentlyDisplayedFragment.getParentActionId() != mAction.getActionId()) {
                    displayNewConfigFragment();
                } else {
                    mAction.setConfigurationFragment(currentlyDisplayedFragment);
                }

            } else {
                displayNewConfigFragment();
            }

        }

    }

    private void displayNewConfigFragment() {

        // If there is a different fragment being displayed, create the new configuration fragment
        ConfigurationFragment newConfigurationFragment = mAction.getConfigurationFragment();

        if (getArguments() != null) {
            if (getArguments().containsKey(EXTRA_ACTION_ID) && mAction.getActionId() == getArguments().getInt(EXTRA_ACTION_ID)) {
                if (getArguments().containsKey(EXTRA_ACTION_ARGUMENTS)) {
                    newConfigurationFragment.setActionParameters(getArguments().getStringArrayList(EXTRA_ACTION_ARGUMENTS));
                }
            }
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_edit_action_configure_frame, newConfigurationFragment)
                .commit();

    }

    private void clearConfigFragment() {

        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_edit_action_configure_frame);

        if (currentFragment != null) {
            mNoConfigText.setVisibility(View.VISIBLE);
            getChildFragmentManager().beginTransaction().remove(currentFragment).commit();
            getChildFragmentManager().popBackStack();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_edit_action_ok:

                checkPermissions();

                break;

            case R.id.fragment_edit_action_cancel:
                EventBus.getDefault().post(new EditActionFragmentEvents.Outgoing.OnCancel());
                break;

            case R.id.fragment_edit_appearance_list_item:

                IconListDialogFragment.Builder builder = new IconListDialogFragment.Builder(DIALOG_REQUEST_CODE);
                for (Action x : ActionManager.getActions()) {
                    builder.addItem(x.getActionName(), x.getActionImageUri());
                }
                builder.build().show(getFragmentManager(), IconListDialogFragment.TAG);

                break;

        }

    }

    private void checkPermissions() {

        if (mAction.getRequiredPermissions() != null && mAction.getRequiredPermissions().length > 0) {

            String[] requiredPermissions = mAction.getRequiredPermissions();

            for (int i = 0; i < requiredPermissions.length; i++) {

                String currRequiredPermission = requiredPermissions[i];

                if (ContextCompat.checkSelfPermission(getActivity(), currRequiredPermission) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{currRequiredPermission}, i);

                    return;

                }

            }

            finishConfigure();

        } else {

            finishConfigure();

        }


    }

    private void finishConfigure() {

        // On OK return the configured fragment using OnActionConfigured
        mAction.saveParameters();

        EventBus.getDefault().post(new EditActionFragmentEvents.Outgoing.OnConfigured(mAction));

    }

    @Subscribe
    public void onIconListDialogFragmentEvent(IconListDialogFragmentEvent event) {

        if (event.requestCode == DIALOG_REQUEST_CODE) {

            // On item selected, get a new instance of the action so the list objects remain in tact
            int actionId = ActionManager.getActions().get(event.position).getActionId();
            mAction = ActionManager.getInstanceOfAction(actionId);
            updateCurrentAction();

        }

    }

    @Subscribe
    public void onRequestPermissionResultEvent(OnRequestPermissionResultEvent event) {

        if (event.grantResults.length > 0
                && event.grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            checkPermissions();

            // permission was granted, yay! Do the
            // contacts-related task you need to do.

        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
        }

    }

}
