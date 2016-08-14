package com.pluviostudios.dialin.buttonsActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.pluviostudios.dialin.appearanceActivity.AppearanceActivity;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSetManager;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonsFragment;
import com.pluviostudios.dialin.buttonsActivity.fragments.EditActionFragment;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.settings.SettingsActivity;
import com.pluviostudios.dialin.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.APPEARANCE_ACTIVITY_REQUEST_CODE;
import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.EXTRA_CHANGES_MADE;

public class ButtonsActivity extends AppCompatActivity implements ButtonsFragment.OnButtonsFragmentButtonClicked, EditActionFragment.OnActionConfigured {

    public static final String TAG = "ButtonsActivity";

    public static final int EDIT_CONFIG_RESULT_CODE = 101;

    public static final String EXTRA_CONFIG_ID = "extra_config_id";
    public static final String EXTRA_CONFIG_TITLE = "extra_config_title";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";
    public static final String EXTRA_LAUNCH_BUTTON_INDEX = "extra_launch_button_index";
    public

    @BindView(R.id.activity_buttons_save_button) Button buttonOk;

    private long mConfigID;
    private String mConfigTitle;
    private int mWidgetButtonCount;
    private boolean mNewConfig;
    private boolean mLaunchOnLeft = false;
    private int mLaunchButtonIndex;

    private Node mRootNode;
    private Node mCurrentNode = mRootNode;
    private Node mNodeBeingEdited;

    private ButtonsFragment mButtonsFragment;
    private EditActionFragment mEditFragment;

    public static Intent buildMainActivityForNewConfiguration(Context context, String configTitle, int buttonCount) {
        Intent startIntent = new Intent(context, ButtonsActivity.class);
        startIntent.putExtra(EXTRA_CONFIG_TITLE, configTitle);
        startIntent.putExtra(EXTRA_BUTTON_COUNT, buttonCount);
        return startIntent;
    }

    public static Intent buildMainActivity(Context context, long configId) {

        // Get configuration title, button count and launch index from database
        final String[] projection = new String[]{
                DBContract.ConfigEntry.TITLE_COL,
                DBContract.ConfigEntry.BUTTON_COUNT_COL,
                DBContract.ConfigEntry.LAUNCH_BUTTON_INDEX
        };

        Cursor c = context.getContentResolver().query(DBContract.ConfigEntry.buildConfigWithId(configId),
                projection,
                null, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {

                String title = c.getString(0);
                int buttonCount = c.getInt(1);
                int launchIndex = c.getInt(2);

                Intent startIntent = buildMainActivityForNewConfiguration(context, title, buttonCount);
                startIntent.putExtra(EXTRA_LAUNCH_BUTTON_INDEX, launchIndex);
                startIntent.putExtra(EXTRA_CONFIG_ID, configId);

                c.close();

                return startIntent;

            } else {
                throw new RuntimeException("ConfigID " + configId + " not found");
            }
        } else {
            throw new RuntimeException("ConfigID " + configId + " not found");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons);
        ButterKnife.bind(this);

        // Get extras passed by ConfigManagerActivity
        Bundle extras = getIntent().getExtras();

        // Throw exceptions if we are missing expected extras
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_CONFIG_TITLE,
                EXTRA_BUTTON_COUNT);

        mConfigTitle = extras.getString(EXTRA_CONFIG_TITLE);
        mWidgetButtonCount = extras.getInt(EXTRA_BUTTON_COUNT);

        if (extras.containsKey(EXTRA_LAUNCH_BUTTON_INDEX)) {
            mLaunchButtonIndex = extras.getInt(EXTRA_LAUNCH_BUTTON_INDEX);
            if (mLaunchButtonIndex > 0) {
                mLaunchOnLeft = false;
            }
        } else {
            mLaunchButtonIndex = mLaunchOnLeft ? 0 : (mWidgetButtonCount - 1);
        }

        // Set the title
        setTitle(mConfigTitle);

        // Check to see if this is a new configuration
        mNewConfig = !extras.containsKey(EXTRA_CONFIG_ID);
        if (!mNewConfig) {
            mConfigID = extras.getLong(EXTRA_CONFIG_ID);
            mRootNode = StorageManager.loadNode(ButtonsActivity.this, mConfigID);
        } else {
            // Show the rename dialog if this is a new configuration
            showRenameDialog();
            mRootNode = new Node();
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

        // Set current node to root
        mCurrentNode = mRootNode;

        // Clear edit menu if it is open
        clearEditMenu();

        // Get the current button icon set
        ButtonIconSet buttonIconSet = ButtonIconSetManager.getButtonIconSet(this, mWidgetButtonCount);

        // Generate and place ButtonsFragment in top frame
        mButtonsFragment = ButtonsFragment.buildButtonsFragment(mWidgetButtonCount, buttonIconSet);
        mButtonsFragment.getArguments().putInt(ButtonsFragment.EXTRA_LAUNCH_BUTTON_INDEX, mLaunchButtonIndex);
        mButtonsFragment.setOnButtonsFragmentButtonClicked(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_buttons_top_frame, mButtonsFragment, ButtonsFragment.TAG)
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
        newTitleEditText.setMaxLines(1);
        newTitleEditText.setSingleLine();
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

        DialinImage launcherIcon = null;
        DialinImage[] childrenIcons = new DialinImage[mWidgetButtonCount - 1];

        if (mCurrentNode.hasAction()) {
            launcherIcon = mCurrentNode.getAction().getActionImage();
        }

        for (int i = 0; i < mWidgetButtonCount - 1; i++) {
            Node childNode = mCurrentNode.getChild(i);
            if (childNode.hasAction()) {
                childrenIcons[i] = childNode.getAction().getActionImage();
            }
        }

        mButtonsFragment.setIcons(launcherIcon, childrenIcons);

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
                .replace(R.id.activity_buttons_bottom_frame, mEditFragment, EditActionFragment.TAG)
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
            case R.id.menu_main_switch_launch_side: {
                // Set flag for launchOnLeft
                mLaunchOnLeft = !mLaunchOnLeft;
                mLaunchButtonIndex = mLaunchOnLeft ? 0 : mWidgetButtonCount - 1;
                // Rebuild
                buildButtonsFragment();
                break;
            }
            case R.id.menu_activity_configuration_rename: {
                showRenameDialog();
                break;
            }
            case R.id.menu_appearance: {
                Intent intent = new Intent(this, AppearanceActivity.class);
                startActivityForResult(intent, APPEARANCE_ACTIVITY_REQUEST_CODE);
                break;
            }
            case R.id.menu_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
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
                saveResult = StorageManager.saveNewConfiguration(this, mConfigTitle, mWidgetButtonCount, mLaunchButtonIndex, mRootNode);
            } else {
                saveResult = StorageManager.saveConfiguration(this, mConfigID, mConfigTitle, mLaunchButtonIndex, mRootNode);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case AppearanceActivity.APPEARANCE_ACTIVITY_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {

                    // If changes were made to the widget's appearance, rebuild the fragment
                    Bundle resultExtras = data.getExtras();
                    if (resultExtras.containsKey(AppearanceActivity.EXTRA_CHANGES_MADE) && resultExtras.getBoolean(EXTRA_CHANGES_MADE)) {
                        buildButtonsFragment();
                    }

                }
                break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
