package com.pluviostudios.dialin.buttonsActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.defaultActions.EmptyAction;
import com.pluviostudios.dialin.appearanceActivity.AppearanceActivity;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonFragmentEvents;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonsFragment;
import com.pluviostudios.dialin.buttonsActivity.fragments.EditActionFragment;
import com.pluviostudios.dialin.buttonsActivity.fragments.EditActionFragmentEvents;
import com.pluviostudios.dialin.data.JSONNodeConverter;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.settings.SettingsActivity;
import com.pluviostudios.dialin.utilities.HelpDialogFragment;
import com.pluviostudios.dialin.utilities.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.util.ArrayList;

import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.APPEARANCE_ACTIVITY_REQUEST_CODE;
import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.EXTRA_CHANGES_MADE;

public class ButtonsActivity extends AppCompatActivity {

    public static final String TAG = "ButtonsActivity";

    public static final int EDIT_CONFIG_RESULT_CODE = 101;

    public static final String EXTRA_CONFIG_ID = "extra_config_id";
    public static final String EXTRA_CONFIG_TITLE = "extra_config_title";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";
    public static final String EXTRA_LAUNCH_BUTTON_INDEX = "extra_launch_button_index";

    public static final String SAVED_PATH = "saved_path";
    public static final String SAVED_EDIT_NODE_INDEX = "saved_edit_node_index";
    public static final String SAVED_TEMP_JSON = "saved_temp_json";

    private Button mButtonOk;

    private long mConfigID;
    private String mConfigTitle;
    private int mWidgetButtonCount;
    private boolean mNewConfig;
    private boolean mLaunchOnLeft = false;
    private int mLaunchButtonIndex;

    private Node mRootNode;
    private Node mCurrentNode = mRootNode;
    private Integer mNodeBeingEditedIndex;
    private ArrayList<Integer> mCurrentPath;

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

    private void initialize() {
        mButtonOk = (Button) findViewById(R.id.activity_buttons_save_button);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons);
        initialize();

        // Get extras passed by ConfigManagerActivity
        Bundle extras = getIntent().getExtras();

        // Throw exceptions if we are missing expected extras
        Utilities.checkBundleForExpectedExtras(extras, EXTRA_CONFIG_TITLE, EXTRA_BUTTON_COUNT);
        mWidgetButtonCount = extras.getInt(EXTRA_BUTTON_COUNT);
        mConfigTitle = extras.getString(EXTRA_CONFIG_TITLE);

        setTitle(mConfigTitle);

        // If extra launch_button_index was passed, change the launch button index, otherwise assign to rightmost button
        if (extras.containsKey(EXTRA_LAUNCH_BUTTON_INDEX)) {
            mLaunchButtonIndex = extras.getInt(EXTRA_LAUNCH_BUTTON_INDEX);
            mLaunchOnLeft = mLaunchButtonIndex == 0;
        } else {
            mLaunchButtonIndex = mLaunchOnLeft ? 0 : (mWidgetButtonCount - 1);
        }

        // Check to see if this is a new configuration
        mNewConfig = !extras.containsKey(EXTRA_CONFIG_ID);
        if (savedInstanceState == null) {
            if (!mNewConfig) {
                mConfigID = extras.getLong(EXTRA_CONFIG_ID);
                mRootNode = StorageManager.loadNode(ButtonsActivity.this, mConfigID);
            } else {
                mRootNode = new Node();
            }
        } else {
            try {
                String jsonData = savedInstanceState.getString(SAVED_TEMP_JSON);
                mRootNode = JSONNodeConverter.convertJSONToNodeTree(jsonData);
            } catch (JSONException e) {

            }
        }

        // Return to previous path position if it was saved
        mCurrentNode = mRootNode;

        // If this activity is being restored
        if (savedInstanceState != null) {

            // Restore current path
            if (savedInstanceState.containsKey(SAVED_PATH)) {

                mCurrentPath = savedInstanceState.getIntegerArrayList(SAVED_PATH);
                for (Integer x : mCurrentPath) {
                    mCurrentNode = mCurrentNode.getChild(x);
                }

            }

            // Restore edit action fragment
            if (savedInstanceState.containsKey(SAVED_EDIT_NODE_INDEX)) {
                mNodeBeingEditedIndex = savedInstanceState.getInt(SAVED_EDIT_NODE_INDEX);
            }

        } else {

            mCurrentPath = new ArrayList<>();
            mNodeBeingEditedIndex = null;
            buildButtonsFragment();

        }

        // Set OK button to save changes to config file and send RESULT_OK to ConfigurationManagerActivity
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishButtonConfiguration();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putIntegerArrayList(SAVED_PATH, mCurrentPath);

        if (mNodeBeingEditedIndex != null)
            outState.putInt(SAVED_EDIT_NODE_INDEX, mNodeBeingEditedIndex);
        else {
            if (outState.containsKey(SAVED_EDIT_NODE_INDEX)) {
                outState.remove(SAVED_EDIT_NODE_INDEX);
            }
        }

        try {
            String tempNodeJson = JSONNodeConverter.convertNodeToJSON(mRootNode);
            outState.putString(SAVED_TEMP_JSON, tempNodeJson);
        } catch (JSONException e) {
            Log.e(TAG, "onSaveInstanceState: Unable to save tempNodeTree", e);
        }

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void buildButtonsFragment() {

        // Clear edit menu if it is open
        clearEditMenu();

        // Generate and place ButtonsFragment in top frame
        ButtonsFragment buttonsFragment;
        if (!mCurrentNode.isBlank) {
            buttonsFragment = ButtonsFragment.buildButtonsFragment(mWidgetButtonCount,
                    getCurrentLauncherIcon(),
                    getCurrentChildIcons());
        } else {
            buttonsFragment = ButtonsFragment.buildButtonsFragment(mWidgetButtonCount);
        }

        buttonsFragment.getArguments().putInt(ButtonsFragment.EXTRA_LAUNCH_BUTTON_INDEX, mLaunchButtonIndex);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_buttons_top_frame, buttonsFragment, ButtonsFragment.TAG)
                .commit();

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
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // On text changed, update the title. I think this looks neat
                setTitle(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed
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

    private Uri getCurrentLauncherIcon() {

        Uri launcherIcon = null;

        if (mCurrentNode.hasAction()) {
            launcherIcon = mCurrentNode.getAction().getActionImageUri();
        }

        return launcherIcon;

    }

    private Uri[] getCurrentChildIcons() {

        Uri[] childrenIcons = new Uri[mWidgetButtonCount - 1];

        for (int i = 0; i < mWidgetButtonCount - 1; i++) {
            Node childNode = mCurrentNode.getChild(i);
            if (childNode.hasAction()) {
                childrenIcons[i] = childNode.getAction().getActionImageUri();
            }
        }

        return childrenIcons;

    }


    private void updateButtonsFragment() {
        EventBus.getDefault().post(new ButtonFragmentEvents.Incoming.ButtonsFragmentUpdateEvent(
                getCurrentLauncherIcon(),
                getCurrentChildIcons()));
    }

    private void showEditMenu(int childIndex) {

        mNodeBeingEditedIndex = childIndex;
        Node editNode = mCurrentNode.getChild(childIndex);

        EditActionFragment editActionFragment;

        // Generate and setup the EditActionFragment
        if (editNode.hasAction()) {

            // If the node currently has an action, pass it as a parameter
            editActionFragment = EditActionFragment.buildEditFragment(editNode.getAction());

        } else {

            // Otherwise, don't pass anything and EditActionFragment will create one
            editActionFragment = EditActionFragment.buildEditFragment();

        }

        // Display EditActionFragment in the bottom frame
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_buttons_bottom_frame, editActionFragment, EditActionFragment.TAG)
                .commit();

    }

    private void clearEditMenu() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_buttons_bottom_frame);

        if (fragment != null) {
            // Remove EditActionFragment
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

            EventBus.getDefault().post(new ButtonFragmentEvents.Incoming.ButtonsFragmentClearHoldEvent());

        }
        mNodeBeingEditedIndex = null;

    }

    @Subscribe
    public void onButtonsFragmentClick(ButtonFragmentEvents.Outgoing.ClickEvent clickEvent) {

        mCurrentNode = mCurrentNode.getChild(clickEvent.index);
        mCurrentPath.add(clickEvent.index);
        updateButtonsFragment();
        clearEditMenu();

    }

    @Subscribe
    public void onButtonsFragmentLongClick(ButtonFragmentEvents.Outgoing.LongClickEvent clickEvent) {
        showEditMenu(clickEvent.index);
    }

    @Subscribe
    public void onButtonsFragmentLaunchClick(ButtonFragmentEvents.Outgoing.LaunchClickEvent clickEvent) {

        // Execute the action
        if (mCurrentNode.hasAction()) {
            mCurrentNode.getAction().execute();
        }

        // Set current node back to rootNode
        mCurrentNode = mRootNode;
        mCurrentPath = new ArrayList<>();

        // Update buttons fragment to display changes
        updateButtonsFragment();

        // Clear edit menu if open
        clearEditMenu();

    }

    @Subscribe
    public void onEditActionFragmentConfigured(EditActionFragmentEvents.Outgoing.OnConfigured event) {

        // Once action has been setup through EditActionFragment, assign the new action to this node
        Node editNode = mCurrentNode.getChild(mNodeBeingEditedIndex);
        if (event.action instanceof EmptyAction) {
            editNode.setAction(null);
        } else {
            editNode.setAction(event.action);
        }

        // Update Buttons Fragment to display the new action
        updateButtonsFragment();

        // Exit EditActionFragment
        clearEditMenu();


    }

    @Subscribe
    public void onEditActionFragmentCanceled(EditActionFragmentEvents.Outgoing.OnCancel event) {

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
            case R.id.menu_activity_configuration_help: {
                HelpDialogFragment helpDialogFragment = new HelpDialogFragment();
                helpDialogFragment.show(getSupportFragmentManager(), HelpDialogFragment.TAG);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        EventBus.getDefault().post(new OnRequestPermissionResultEvent(requestCode, permissions, grantResults));
    }

}
