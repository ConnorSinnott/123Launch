package com.pluviostudios.dialin.configActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSetManager;
import com.pluviostudios.dialin.configActivity.fragments.ButtonsFragment;
import com.pluviostudios.dialin.configActivity.fragments.EditActionFragment;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfigurationActivity extends AppCompatActivity implements ButtonsFragment.OnButtonsFragmentButtonClicked, EditActionFragment.OnActionConfigured {

    public static final String TAG = "ConfigurationActivity";

    public static final int EDIT_CONFIG_RESULT_CODE = 101;

    public static final String EXTRA_CONFIG_ID = "extra_config_id";
    public static final String EXTRA_CONFIG_TITLE = "extra_config_title";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";

    @BindView(R.id.activity_configuration_save_button) Button buttonOk;

    private long mConfigID;
    private String mConfigTitle;
    private int mWidgetButtonCount;
    private boolean mNewConfig;
    private boolean mLaunchOnLeft;
    private int launchButtonIndex;

    private Node mRootNode;
    private Node mCurrentNode = mRootNode;
    private Node mNodeBeingEdited;

    private ButtonsFragment mButtonsFragment;
    private EditActionFragment mEditFragment;

    public static Intent buildMainActivityForNewConfiguration(Context context, String configTitle, int buttonCount) {
        Intent startIntent = new Intent(context, ConfigurationActivity.class);
        startIntent.putExtra(EXTRA_CONFIG_TITLE, configTitle);
        startIntent.putExtra(EXTRA_BUTTON_COUNT, buttonCount);
        return startIntent;
    }

    public static Intent buildMainActivity(Context context, String configTitle, long configId, int buttonCount) {
        Intent startIntent = new Intent(context, ConfigurationActivity.class);
        startIntent.putExtra(EXTRA_CONFIG_ID, configId);
        startIntent.putExtra(EXTRA_CONFIG_TITLE, configTitle);
        startIntent.putExtra(EXTRA_BUTTON_COUNT, buttonCount);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ButterKnife.bind(this);

        // Get extras passed by ConfigManagerActivity
        Bundle extras = getIntent().getExtras();

        // Throw exceptions if we are missing expected extras
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_CONFIG_TITLE,
                EXTRA_BUTTON_COUNT);

        mConfigTitle = extras.getString(EXTRA_CONFIG_TITLE);
        mWidgetButtonCount = extras.getInt(EXTRA_BUTTON_COUNT);
        launchButtonIndex = mLaunchOnLeft ? 0 : mWidgetButtonCount - 1;

        // Set the title
        setTitle(mConfigTitle);

        // Check to see if this is a new configuration
        mNewConfig = !extras.containsKey(EXTRA_CONFIG_ID);
        if (!mNewConfig) {
            mConfigID = extras.getLong(EXTRA_CONFIG_ID);
        } else {

            // Show the rename dialog if this is a new configuration
            showRenameDialog();
        }

        // Build buttons fragment
        buildButtonsFragment();

        // Set OK button to save changes to config file and send RESULT_OK to ConfigurationManagerActivity
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishButtonConfiguration();
            }
        });


    }

    private void buildButtonsFragment() {

        // Load the rootNode from file using mConfigId
        if (mNewConfig) {
            mRootNode = new Node();
        } else {
            mRootNode = StorageManager.loadNode(ConfigurationActivity.this, mConfigID);
        }

        // Set current node to root
        mCurrentNode = mRootNode;

        // Clear edit menu if it is open
        // Todo might not be needed
        clearEditMenu();

        // Get the current button icon set
        ButtonIconSet buttonIconSet = ButtonIconSetManager.getButtonIconSet(this, mWidgetButtonCount);

        // Generate and place ButtonsFragment in top frame
        mButtonsFragment = ButtonsFragment.buildButtonsFragment(mWidgetButtonCount, buttonIconSet);
        mButtonsFragment.getArguments().putInt(ButtonsFragment.EXTRA_LAUNCH_INDEX, mLaunchOnLeft ? 0 : mWidgetButtonCount - 1);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_configuration_top_frame, mButtonsFragment, ButtonsFragment.TAG)
                .commit();

        // If the current node has any children, we have to call updateButtonsFragment to display them
        if (!mCurrentNode.isBlank) {
            updateButtonsFragment();
        }

    }

    public void showRenameDialog() {
        // Hold the current title so it can be restored on cancel
        final String titleHold = mConfigTitle;

        // Create a new editText
        final EditText newTitleEditText = new EditText(this);
        newTitleEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        newTitleEditText.setText(mConfigTitle);
        newTitleEditText.selectAll();
        newTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // On text changed, update the title. I think this looks neat
                setTitle(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Create an AlertDialog housing the edit text
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(newTitleEditText)
                .setTitle("New Title")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Assign the new title here instead of the text watcher so its not constantly rewritten
                        mConfigTitle = newTitleEditText.getText().toString();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfigTitle = titleHold;
                        setTitle(mConfigTitle);
                    }
                })
                .setCancelable(true)
                .create().show();

    }


    // Called when one of the buttonFragment's buttons are clicked
    @Override
    public void onButtonClicked(int index) {
        mCurrentNode = mCurrentNode.getChild(index);
        updateButtonsFragment();
        clearEditMenu();
    }

    // Called when one of the buttonFragment's buttons are long clicked
    @Override
    public void onButtonLongClicked(int index) {
        showEditMenu(mCurrentNode.getChild(index));
    }

    // Called when buttonFragment's launch button is clicked
    @Override
    public void onLaunchButtonClicked() {

        // Execute the action
        if (mCurrentNode.hasAction()) {
            mCurrentNode.getAction().execute();
        }

        // Set current node back to rootNode
        mCurrentNode = mRootNode;

        // Update buttons fragment to display changes
        updateButtonsFragment();

        // Clear edit menu if open
        clearEditMenu();

    }

    private void updateButtonsFragment() {

        // Todo This system does not support LaunchOnLeft

        DialinImage[] currentImages = new DialinImage[mWidgetButtonCount];
        for (int i = 0; i < mWidgetButtonCount - 1; i++) {

            // Add child images
            Node currentChild = mCurrentNode.getChild(i);
            if (currentChild.hasAction()) {
                currentImages[i] = currentChild.getAction().getActionImage();
            }
        }

        // Add current (Launch button) image
        if (mCurrentNode.hasAction()) {
            currentImages[mWidgetButtonCount - 1] = mCurrentNode.getAction().getActionImage();
        }

        // Update button fragment
        mButtonsFragment.setIcons(currentImages);

    }

    private void showEditMenu(Node node) {

        mNodeBeingEdited = node;

        // Generate and setup the EditActionFragment
        if (mNodeBeingEdited.hasAction()) {

            // If the node currently has an action, pass it as a parameter
            mEditFragment = EditActionFragment.buildEditFragment(mNodeBeingEdited.getAction());

        } else {

            // Otherwise, don't pass anything and EditActionFragment will create one
            mEditFragment = EditActionFragment.buildEditFragment();

        }

        // Display EditActionFragment in the bottom frame
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_configuration_bottom_frame, mEditFragment, EditActionFragment.TAG)
                .commit();

    }

    private void clearEditMenu() {
        if (mEditFragment != null) {

            // Remove EditActionFragment
            getSupportFragmentManager().beginTransaction().remove(mEditFragment).commit();

            // Clear any holds
            mButtonsFragment.clearHold();

        }

        mNodeBeingEdited = null;
    }

    @Override
    public void onActionConfigured(Action action) {

        // Once action has been setup through EditActionFragment, assign the new action to this node
        mNodeBeingEdited.setAction(action);

        // Update Buttons Fragment to display the new action
        updateButtonsFragment();

        // Exit EditActionFragment
        clearEditMenu();

    }

    @Override
    public void onConfigurationCancelled() {

        // Exit EditActionFragment
        clearEditMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_switch_launch_side:
                // If the user has made progress, alert the user that progress will be lost
                if (!mRootNode.isBlank) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(android.R.drawable.stat_sys_warning);
                    builder.setMessage("To apply this change the widget will need to be reset.");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Set flag for launchOnLeft
                            mLaunchOnLeft = !mLaunchOnLeft;
                            // Rebuild
                            buildButtonsFragment();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // On cancel, dismiss
                            dialog.dismiss();

                        }
                    });
                    builder.setCancelable(true);
                    builder.create().show();
                } else {
                    mLaunchOnLeft = !mLaunchOnLeft; // Set flag for launchOnLeft
                    buildButtonsFragment(); // Rebuild
                }
                break;
            case R.id.menu_activity_configuration_rename:
                showRenameDialog();
                break;
        }
        return true;
    }

    // Save the configuration file and finish activity
    private void finishButtonConfiguration() {

        // If changes have been made
        if (!mRootNode.isBlank) {

            // Attempt to save the configuration and get saveResult
            Bundle saveResult;
            if (mNewConfig) {
                saveResult = StorageManager.saveNewConfiguration(this, mConfigTitle, mWidgetButtonCount, launchButtonIndex, mRootNode);
            } else {
                saveResult = StorageManager.saveConfiguration(this, mConfigID, mConfigTitle, mRootNode);
            }

            // Pass saveResult back to parent activity with RESULT_OK
            Intent data = new Intent();
            data.putExtras(saveResult);
            setResult(RESULT_OK, data);

        } else {

            // If changes have not been made, cancel
            setResult(RESULT_CANCELED);

        }

        finish();

    }

    @Override
    public void onBackPressed() {

        // If the back button is pressed, cancel without saving
        // Todo, is this default behavior?

        finish();

        super.onBackPressed();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(new Bundle());
    }
}
