package com.pluviostudios.dialin.buttonsActivity.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.defaultActions.EmptyAction;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;
import com.pluviostudios.dialin.utilities.Utilities;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 7/27/16.
 */
public class EditActionFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditActionFragment";
    public static final String EXTRA_ACTION_ID = "extra_action_id";
    public static final String EXTRA_ACTION_ARGUMENTS = "extra_action_arguments";

    protected View mRoot;
    @BindView(R.id.fragment_edit_action_no_config) TextView mNoConfigText;
    @BindView(R.id.fragment_edit_action_configure_frame) FrameLayout mConfigFragmentFrame;
    @BindView(R.id.fragment_edit_action_list_item) View mEditActionListItem;
    @BindView(R.id.list_item_action_image) ImageView mListItemActionImageView;
    @BindView(R.id.list_item_action_text_view) TextView mListItemActionTextView;
    @BindView(R.id.fragment_edit_action_ok) Button mButtonOk;
    @BindView(R.id.fragment_edit_action_cancel) Button mButtonCancel;

    protected Action mAction;
    protected ConfigurationFragment mCurrentConfigFragment;
    protected OnActionConfigured mOnActionConfigured;

    public static EditActionFragment buildEditFragment() {
        EditActionFragment newFragment = new EditActionFragment();
        return newFragment;
    }

    public static EditActionFragment buildEditFragment(Action action) {

        EditActionFragment editFragment = buildEditFragment();

        int actionId = action.getActionId();
        ArrayList<String> actionArguments = action.getActionArguments();

        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ACTION_ID, actionId);
        extras.putStringArrayList(EXTRA_ACTION_ARGUMENTS, actionArguments);

        editFragment.setArguments(extras);

        return editFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_edit_action, container, false);
        ButterKnife.bind(this, mRoot);

        Bundle extras = getArguments();
        if (extras != null) {

            // If there are extras it means that an Action info was passed
            Utilities.checkBundleForExpectedExtras(extras,
                    EXTRA_ACTION_ID,
                    EXTRA_ACTION_ARGUMENTS);

            // Get the action information: id and arguments
            int actionId = extras.getInt(EXTRA_ACTION_ID);
            ArrayList<String> actionArguments = extras.getStringArrayList(EXTRA_ACTION_ARGUMENTS);

            // And recreate the action using these parameters
            mAction = ActionManager.getInstanceOfAction(actionId);
            mAction.setActionArguments(actionArguments);

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

    private void updateCurrentAction() {

        mListItemActionTextView.setText(mAction.getActionName());
        mListItemActionImageView.setImageURI(mAction.getActionImage().getImageUri());

        // If the action has a configuration fragment, now would be the time to show it
        if (mAction.hasConfigurationFragment()) {

            // Display the configuration frame
            mNoConfigText.setVisibility(View.GONE);

            // Place configuration fragment into frame
            mCurrentConfigFragment = mAction.getConfigurationFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_edit_action_configure_frame, mCurrentConfigFragment, mCurrentConfigFragment.TAG)
                    .commit();

        } else {

            // Otherwise clear the configuration fragment if one is visible
            if (mCurrentConfigFragment != null) {
                getFragmentManager().beginTransaction().remove(mCurrentConfigFragment).commit();
                mNoConfigText.setVisibility(View.VISIBLE);
                mCurrentConfigFragment = null;
            }

        }

    }

    @Override
    public void onAttach(Context context) {

        if (!(context instanceof OnActionConfigured))
            throw new RuntimeException("Parent activity should implement " + OnActionConfigured.class.getCanonicalName());

        mOnActionConfigured = (OnActionConfigured) context;

        super.onAttach(context);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_edit_action_ok:

                // If the currentAction has been changed from the default action with id -1
                if (mAction.getActionId() >= 0) {

                    mAction.saveArguments();

                    // On OK return the configured fragment using OnActionConfigured
                    if (mOnActionConfigured != null) {
                        mOnActionConfigured.onActionConfigured(mAction);
                    }

                } else {

                    // Otherwise consider this a cancellation

                    if (mOnActionConfigured != null) {
                        mOnActionConfigured.onConfigurationCancelled();
                    }

                }

                break;

            case R.id.fragment_edit_action_cancel:

                // On Cancel, notify mOnActionConfigured
                if (mOnActionConfigured != null) {
                    mOnActionConfigured.onConfigurationCancelled();
                }
                break;

            case R.id.fragment_edit_action_list_item:

                // Todo because we are not using onAttach anymore, save the restore the onClick manually in onStart

                // Get a list of all available actions and display them using a IconListDialogFragment
                IconListDialogFragment<Action> listDialogFragment = new IconListDialogFragment();

                listDialogFragment.setItems(ActionManager.getActions());

                listDialogFragment.setItemAdapter(new IconListDialogFragment.IconListDialogItemAdapter<Action>() {
                    @Override
                    public String getString(Action object) {
                        return object.getActionName();
                    }

                    @Override
                    public Uri getImageUri(Action object) {
                        return object.getActionImage().getImageUri();
                    }
                });

                listDialogFragment.setOnListItemSelected(new IconListDialogFragment.OnListItemSelected<Action>() {
                    @Override
                    public void onListItemSelected(Action object, int position) {

                        // On item selected, get a new instance of the action so the list objects remain in tact
                        mAction = ActionManager.getInstanceOfAction(object.getActionId());
                        updateCurrentAction();

                    }
                });

                listDialogFragment.show(getFragmentManager(), IconListDialogFragment.TAG);
                break;

        }

    }

    public interface OnActionConfigured {
        void onActionConfigured(Action action);

        void onConfigurationCancelled();
    }

}
