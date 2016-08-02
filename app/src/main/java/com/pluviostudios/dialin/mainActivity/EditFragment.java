package com.pluviostudios.dialin.mainActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.action.ConfigurationFragment;
import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.utilities.Utilities;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 7/27/16.
 */
public class EditFragment extends Fragment implements View.OnClickListener, ListDialogFragment.OnListItemSelected {

    public static final String TAG = "EditFragment";
    public static final String EXTRA_ACTION_ID = "extra_action_id";
    public static final String EXTRA_ACTION_ARGUMENTS = "extra_action_arguments";

    protected View mRoot;
    @BindView(R.id.fragment_edit_action_no_config_flipper) protected ViewFlipper mNoConfigFlipper;
    @BindView(R.id.fragment_edit_action_configure_frame) protected FrameLayout mConfigFragmentFrame;
    @BindView(R.id.fragment_edit_action_list_item) protected View mEditActionListItem;
    @BindView(R.id.list_item_action_image) ImageView mListItemActionImageView;
    @BindView(R.id.list_item_action_text_view) TextView mListItemActionTextView;
    @BindView(R.id.fragment_edit_action_ok) protected Button mButtonOk;
    @BindView(R.id.fragment_edit_action_cancel) protected Button mButtonCancel;

    protected Action mAction;
    protected OnActionConfigured mOnActionConfigured;

    public static EditFragment buildEditFragment() {
        EditFragment newFragment = new EditFragment();
        return newFragment;
    }

    public static EditFragment buildEditFragment(Action action) {

        EditFragment editFragment = buildEditFragment();

        int actionId = action.id;
        ArrayList<String> actionArguments = action.actionArguments;

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

        // If Action is not specified, throw exception
        Bundle extras = getArguments();
        if (extras != null) {

            Utilities.checkBundleForExpectedExtras(extras,
                    EXTRA_ACTION_ID,
                    EXTRA_ACTION_ARGUMENTS);

            int actionId = extras.getInt(EXTRA_ACTION_ID);
            ArrayList<String> actionArguments = extras.getStringArrayList(EXTRA_ACTION_ARGUMENTS);

            // Restore action
            mAction = ActionManager.getInstanceOfAction(actionId);
            mAction.actionArguments = actionArguments;

            if (mAction.hasConfigurationFragment()) {

                // Display the configuration frame
                mNoConfigFlipper.showNext();

                // Place configuration fragment into frame
                ConfigurationFragment configurationFragment = mAction.getConfigurationFragment();

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_edit_action_configure_frame, configurationFragment, configurationFragment.TAG)
                        .commit();

            }

        } else {

            //Otherwise start with default action
            mAction = Action.DefaultDialinAction;

        }

        updateActionListItem();

        mEditActionListItem.setOnClickListener(this);
        mButtonOk.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        return mRoot;

    }

    private void updateActionListItem() {
        mListItemActionTextView.setText(mAction.name);
        mListItemActionImageView.setImageURI(mAction.actionImage.imageUri);
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
                if (mAction.id >= 0) {

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

                ArrayList<Pair<String, DialinImage>> list = new ArrayList<>();

                for (Action x : ActionManager.getActions()) {
                    list.add(new Pair<>(x.name, x.actionImage));
                }
                ListDialogFragment listDialogFragment = ListDialogFragment.buildListDialogFragment(list);
                listDialogFragment.show(getFragmentManager(), ListDialogFragment.TAG);

                break;

        }

    }

    // Used by ListDialogFragment
    public void onListItemSelected(int position) {

        mAction = ActionManager.getInstanceOfAction(position);
        updateActionListItem();

    }

    public interface OnActionConfigured {
        void onActionConfigured(Action action);

        void onConfigurationCancelled();
    }

}
