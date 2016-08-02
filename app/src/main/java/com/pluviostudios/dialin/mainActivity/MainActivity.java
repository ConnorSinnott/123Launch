package com.pluviostudios.dialin.mainActivity;

import android.appwidget.AppWidgetManager;
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
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageMananger;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ButtonsFragment.OnButtonsFragmentButtonClicked, EditFragment.OnActionConfigured, ListDialogFragment.OnListItemSelected {

    public static final String TAG = "MainActivity";

    @BindView(R.id.main_save_button) Button buttonOk;

    private int mWidgetId;
    private int mWidgetButtonCount;
    private boolean mLaunchOnRight = true;

    private Node mRootNode;
    private Node mCurrentNode = mRootNode;
    private Node mNodeBeingEdited;

    private ButtonsFragment mButtonsFragment;
    private EditFragment mEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // ActionManager is used globally and must be initialized first
        ActionManager.initialize(this);

        // Find which widget started this activity
        if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {

            //Get mWidgetId (Will increment every time a widget is added to the home screen)
            mWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            //Determine button count
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            Bundle options = appWidgetManager.getAppWidgetOptions(mWidgetId);
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            mWidgetButtonCount = (width + 30) / 70; // Homescreen tile = n * 70 - 30

        } else {

            // Non-developmental use should not get here. This is only a launcher for developmental purposes
            mWidgetId = 0;
            mWidgetButtonCount = 5;

        }

        buildButtonsFragment();

        // Set OK button to finish app config
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishConfig();
            }
        });

    }

    private void buildButtonsFragment() {

        // Load root node
        mRootNode = loadRootNode();
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
        if (!mLaunchOnRight)
            mButtonsFragment.getArguments().putBoolean(ButtonsFragment.EXTRA_LAUNCH_ON_LEFT, true);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_top_frame, mButtonsFragment, ButtonsFragment.TAG)
                .commit();

        if (!mCurrentNode.isBlank) {
            updateButtonsFragment();
        }

    }

    private Node loadRootNode() {
        return StorageMananger.loadNodeTree(this, String.valueOf(mWidgetId));
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
                            mLaunchOnRight = !mLaunchOnRight;
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
                    mLaunchOnRight = !mLaunchOnRight; // Set flag for launchOnLeft
                    buildButtonsFragment(); // Rebuild
                }
                break;
        }
        return true;
    }

    // Call this when widget is ready
    private void finishConfig() {

        StorageMananger.saveNodeTree(this, String.valueOf(mWidgetId), mWidgetButtonCount, mRootNode);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

        // If the user has not added anything, consider this a cancellation
        if (!mRootNode.isBlank) {
            setResult(RESULT_OK, resultValue);
        } else {
            setResult(RESULT_CANCELED, resultValue);
        }

        finish();

    }

}
