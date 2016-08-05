package com.pluviostudios.dialin.mainActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.data.JSONNodeConverter;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ButtonsFragment.OnButtonsFragmentButtonClicked, EditFragment.OnActionConfigured, ListDialogFragment.OnListItemSelected {

    public static final String TAG = "MainActivity";

    public static final int EDIT_CONFIG_RESULT_CODE = 101;
    public static final String EXTRA_CONFIG_ID = "extra_config_id";
    public static final String EXTRA_CONFIG_TITLE = "extra_config_title";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";
    public static final String EXTRA_NEW_CONFIG = "extra_new_config";

    @BindView(R.id.main_save_button) Button buttonOk;

    private long mConfigID;
    private String mConfigTitle;
    private int mWidgetButtonCount;
    private boolean mLaunchOnLeft;
    private boolean mNewConfig;

    private Node mRootNode;
    private Node mCurrentNode = mRootNode;
    private Node mNodeBeingEdited;

    private ButtonsFragment mButtonsFragment;
    private EditFragment mEditFragment;

    public static Intent buildMainActivityForNewConfiguration(Context context, String configTitle, int buttonCount) {
        Intent startIntent = new Intent(context, MainActivity.class);
        startIntent.putExtra(EXTRA_CONFIG_TITLE, configTitle);
        startIntent.putExtra(EXTRA_BUTTON_COUNT, buttonCount);
        return startIntent;
    }

    public static Intent buildMainActivityForNewConfiguration(Context context, String configTitle, long configId, int buttonCount) {
        Intent startIntent = new Intent(context, MainActivity.class);
        startIntent.putExtra(EXTRA_CONFIG_ID, configId);
        startIntent.putExtra(EXTRA_CONFIG_TITLE, configTitle);
        startIntent.putExtra(EXTRA_BUTTON_COUNT, buttonCount);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialize ActionManager
        ActionManager.initialize(this);

        // Get extras passed by ConfigManagerActivity
        Bundle extras = getIntent().getExtras();

        // Throw exceptions if we are missing expected extras
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_CONFIG_ID,
                EXTRA_CONFIG_TITLE,
                EXTRA_BUTTON_COUNT);

        mConfigID = extras.getLong(EXTRA_CONFIG_ID);
        mConfigTitle = extras.getString(EXTRA_CONFIG_TITLE);
        mWidgetButtonCount = extras.getInt(EXTRA_BUTTON_COUNT);

        setTitle(mConfigTitle);

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
        mRootNode = StorageManager.loadNode(MainActivity.this, mConfigID);

        // If the root node is blank it means that this is a new configuration
        mNewConfig = mRootNode.isBlank;

        mCurrentNode = mRootNode;
        clearEditMenu();

        // Todo setup system to set images
        // Todo system should include reversing
        DialinImage[] defaultImages = new DialinImage[5];
        defaultImages[0] = new DialinImage(this, R.drawable.bblue);
        defaultImages[1] = new DialinImage(this, R.drawable.bgreen);
        defaultImages[2] = new DialinImage(this, R.drawable.bpurp);
        defaultImages[3] = new DialinImage(this, R.drawable.bblue);
        defaultImages[4] = new DialinImage(this, R.drawable.blaunch);

        // Todo setup system to set images
        DialinImage[] holdImages = new DialinImage[5];
        for (int i = 0; i < 4; i++) {
            holdImages[i] = new DialinImage(this, R.drawable.bpressed);
        }
        holdImages[4] = new DialinImage(this, R.drawable.bpressedlaunch);

        // Generate and place ButtonsFragment in top frame
        mButtonsFragment = ButtonsFragment.buildButtonsFragment(mWidgetButtonCount, defaultImages, holdImages);

        // Extra Params
        if (mLaunchOnLeft)
            mButtonsFragment.getArguments().putBoolean(ButtonsFragment.EXTRA_LAUNCH_ON_LEFT, true);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_top_frame, mButtonsFragment, ButtonsFragment.TAG)
                .commit();

        if (!mCurrentNode.isBlank) {
            updateButtonsFragment();
        }

    }

    @Override
    public void onButtonClicked(int index) {
        mCurrentNode = mCurrentNode.getChild(index);
        updateButtonsFragment();
        clearEditMenu();
    }

    @Override
    public void onButtonLongClicked(int index) {
        showEditMenu(mCurrentNode.getChild(index));
    }

    @Override
    public void onLaunchButtonClicked() {
        mCurrentNode.getAction().execute();
        mCurrentNode = mRootNode;
        updateButtonsFragment();
        clearEditMenu();
    }

    private void updateButtonsFragment() {

        DialinImage[] currentImages = new DialinImage[mWidgetButtonCount];
        for (int i = 0; i < mWidgetButtonCount - 1; i++) {

            // Add child images
            Node currentChild = mCurrentNode.getChild(i);
            currentImages[i] = currentChild.getAction().actionImage;

        }

        // Add current images
        currentImages[mWidgetButtonCount - 1] = mCurrentNode.getAction().actionImage;

        // Update button fragment
        mButtonsFragment.setIcons(currentImages);

    }

    private void showEditMenu(Node node) {

        mNodeBeingEdited = node;

        // Generate and setup the EditFragment
        if (mNodeBeingEdited.hasAction()) {
            mEditFragment = EditFragment.buildEditFragment(mNodeBeingEdited.getAction());
        } else {
            mEditFragment = EditFragment.buildEditFragment();
        }

        // Display EditFragment in the bottom frame
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_bottom_frame, mEditFragment, EditFragment.TAG)
                .commit();

    }

    private void clearEditMenu() {
        if (mEditFragment != null) {

            // Remove EditFragment
            getSupportFragmentManager().beginTransaction().remove(mEditFragment).commit();

            // Clear any holds
            mButtonsFragment.clearHold();

        }

        mNodeBeingEdited = null;
    }

    @Override
    public void onActionConfigured(Action action) {

        // Once action has been setup through EditFragment, assign the new action to this node
        mNodeBeingEdited.setAction(action);

        // Update Buttons Fragment to display the new action
        updateButtonsFragment();

        // Exit EditFragment
        clearEditMenu();

    }

    @Override
    public void onConfigurationCancelled() {

        // Exit EditFragment
        clearEditMenu();

    }

    // Used by EditFragment, re-route there
    @Override
    public void onListItemSelected(int position) {
        mEditFragment.onListItemSelected(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                            // On Cancel
                        }
                    });
                    builder.setCancelable(true);
                    builder.create().show();
                } else {
                    mLaunchOnLeft = !mLaunchOnLeft; // Set flag for launchOnLeft
                    buildButtonsFragment(); // Rebuild
                }
                break;
        }
        return true;
    }

    // Save the configuration file and finish activity
    private void finishButtonConfiguration() {

        if (!mRootNode.isBlank) {
            StorageManager.saveConfiguration(this, mConfigID, mConfigTitle, mRootNode);
            JSONNodeConverter.convertNodeToJSON();

            Intent data = new Intent();
            data.putExtra(EXTRA_CONFIG_ID, mConfigID);
            data.putExtra(EXTRA_NEW_CONFIG, mNewConfig);
            data.putExtra(EXTRA_CONFIG_TITLE, mConfigTitle);
            data.putExtra(EXTRA_BUTTON_COUNT, mWidgetButtonCount);
            setResult(RESULT_OK, data);

        } else {
            setResult(RESULT_CANCELED);
        }

        finish();

    }

    @Override
    public void onBackPressed() {

        setResult(RESULT_CANCELED);
        finish();

        super.onBackPressed();

    }

}
