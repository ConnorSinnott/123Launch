package com.pluviostudios.dialin;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.utilities.ContextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ButtonsFragment.OnButtonsFragmentButtonClicked, EditFragment.OnActionConfigured {

    public static final String TAG = "MainActivity";

    @BindView(R.id.main_save_button) Button buttonOk;

    private int widgetId;
    private int widgetButtonCount;

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

        // ContextHelper is used globally and must be initialized first
        ContextHelper.setContext(this);

        // Find which widget started this activity
        if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {

            //Get widgetId
            widgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            //Determine button count
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            widgetButtonCount = (width + 30) / 70; // Homescreen tile = n * 70 - 30

        } else {

            // Non-developmental use should not get here. This is only a launcher for developmental purposes
            widgetId = 0;
            widgetButtonCount = 5;

        }

        // Load root node
        mRootNode = loadRootNode();
        mCurrentNode = mRootNode;

        // Todo setup system to set images
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
        mButtonsFragment = ButtonsFragment.buildButtonsFragment(widgetButtonCount, defaultImages, holdImages);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_top_frame, mButtonsFragment, ButtonsFragment.TAG)
                .commit();

        // Set OK button to finish app config
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishConfig();
            }
        });

    }

    private static Node loadRootNode() {
        return new Node();
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

        DialinImage[] currentImages = new DialinImage[widgetButtonCount];
        for (int i = 0; i < widgetButtonCount - 1; i++) {

            // Add child images
            Node currentChild = mCurrentNode.getChild(i);
            currentImages[i] = currentChild.getAction().actionImage;

        }

        // Add current images
        currentImages[widgetButtonCount - 1] = mCurrentNode.getAction().actionImage;

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

    private void finishConfig() {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }


    @Override
    public void onActionConfigured(Action action) {

        // Once action has been setup through EditFragment, assign the new action to this node
        mNodeBeingEdited.setAction(action);

        // Exit EditFragment
        clearEditMenu();

    }

    @Override
    public void onConfigurationCancelled() {

        // Exit EditFragment
        clearEditMenu();

    }

}
