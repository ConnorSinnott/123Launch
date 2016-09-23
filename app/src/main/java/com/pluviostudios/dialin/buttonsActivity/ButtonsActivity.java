package com.pluviostudios.dialin.buttonsActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.action.defaultActions.ActionLaunchApplication;
import com.pluviostudios.dialin.appearanceActivity.AppearanceActivity;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonFragmentEvents;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonsFragment;
import com.pluviostudios.dialin.data.JSONNodeConverter;
import com.pluviostudios.dialin.data.Node;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.dialogFragments.IconListDialogAdapter;
import com.pluviostudios.dialin.utilities.AsyncGetApplicationInfo;
import com.pluviostudios.dialin.utilities.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.util.ArrayList;

import static com.pluviostudios.dialin.action.ActionManager.getContext;
import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.APPEARANCE_ACTIVITY_REQUEST_CODE;
import static com.pluviostudios.dialin.appearanceActivity.AppearanceActivity.EXTRA_CHANGES_MADE;

public class ButtonsActivity extends AppCompatActivity implements View.OnClickListener {

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
    private View mButtonInsert;
    private View mButtonDelete;
    private ImageView mButtonInsertImageView;
    private ListView mListView;
    private TextView mPathTextView;
    private TextView mInstructions;

    private long mConfigID;
    private String mConfigTitle;
    private int mWidgetButtonCount;
    private int mLaunchButtonIndex;
    private boolean mInsertMode;
    private boolean mLaunchOnLeft = false;
    private boolean mNewConfig;
    private boolean pendingAppearanceChange = false;

    private ArrayList<ApplicationInfo> mApplicationInfoList;
    private ApplicationInfo mCurrentApplicationInfo;
    private ActionLaunchApplication mGeneratedAction;

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

        mListView = (ListView) findViewById(R.id.activity_buttons_list_view);
        mButtonOk = (Button) findViewById(R.id.activity_buttons_save_button);
        mButtonDelete = findViewById(R.id.activity_buttons_delete_button);
        mButtonInsert = findViewById(R.id.activity_buttons_insert_button);
        mPathTextView = (TextView) findViewById(R.id.activity_buttons_textview_path);
        mInstructions = (TextView) findViewById(R.id.activity_buttons_instructions);

        mButtonInsertImageView = (ImageView) mButtonInsert.findViewById(R.id.activity_buttons_insert_button_image_view);

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
                Log.e(TAG, "onCreate: Could not load saved json", e);
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
        mButtonOk.setOnClickListener(this);
        mButtonInsert.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);

        updatePath();
        loadApplicationList();
        setInsertMode(true);

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

        if (pendingAppearanceChange) {
            pendingAppearanceChange = false;
            buildButtonsFragment();
        }

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void loadApplicationList() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading Applications");
        progressDialog.show();

        new AsyncGetApplicationInfo(this) {

            @Override
            protected void onPostExecute(ArrayList<ApplicationInfo> applicationInfoList) {

                mApplicationInfoList = applicationInfoList;
                mCurrentApplicationInfo = applicationInfoList.get(0);
                updateApplicationInfo();
                progressDialog.dismiss();

                IconListDialogAdapter.Builder builder = new IconListDialogAdapter.Builder();
                for (ApplicationInfo x : mApplicationInfoList) {

                    String applicationName = ActionTools.getForeignApplicationNameFromInfo(getContext(), x);
                    Uri applicationUri = ActionTools.getForeignApplicationImageUriFromInfo(x);
                    builder.addItem(applicationName, applicationUri);

                }
                mListView.setAdapter(builder.build());
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        mCurrentApplicationInfo = mApplicationInfoList.get(position);
                        updateApplicationInfo();

                    }
                });

            }

        }.execute();

    }

    private void updateApplicationInfo() {

        String applicationName = ActionTools.getForeignApplicationNameFromInfo(getContext(), mCurrentApplicationInfo);
        Uri applicationUri = ActionTools.getForeignApplicationImageUriFromInfo(mCurrentApplicationInfo);

        mButtonInsertImageView.setImageURI(applicationUri);

        mGeneratedAction = new ActionLaunchApplication();
        ArrayList<String> params = new ArrayList<>();
        params.add(mCurrentApplicationInfo.packageName);
        params.add(applicationName);
        params.add(applicationUri.toString());
        mGeneratedAction.setParameters(params);

    }

    private void buildButtonsFragment() {

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
        buttonsFragment.getArguments().putBoolean(ButtonsFragment.EXTRA_HOLD_ENABLED, false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_buttons_top_frame, buttonsFragment, ButtonsFragment.TAG)
                .commit();

    }

    private void updateButtonsFragment() {
        updatePath();
        EventBus.getDefault().post(new ButtonFragmentEvents.Incoming.ButtonsFragmentUpdateEvent(
                getCurrentLauncherIcon(),
                getCurrentChildIcons()));
    }

    private void updatePath() {
        String path = "";
        for (Integer x : mCurrentPath) {
            path += " -> " + (x + 1);
        }

        mPathTextView.setText(getResources().getString(R.string.path, path));
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

    private void setInsertMode(boolean insertModeEnabled) {
        mInsertMode = insertModeEnabled;

        View buttonToHighlight = insertModeEnabled ? mButtonInsert : mButtonDelete;
        View buttonToDeselect = insertModeEnabled ? mButtonDelete : mButtonInsert;

        buttonToHighlight.getBackground().setColorFilter(getResources().getColor(R.color.colorAccentTransparent), PorterDuff.Mode.MULTIPLY);
        buttonToDeselect.getBackground().clearColorFilter();

        mInstructions.setText(getResources().getString(R.string.activity_buttons_instructions,
                insertModeEnabled ?
                        getResources().getString(R.string.insert) :
                        getResources().getString(R.string.clear)
        ));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_buttons_save_button: {
                finishButtonConfiguration();
                break;
            }
            case R.id.activity_buttons_insert_button: {
                setInsertMode(true);
                break;
            }
            case R.id.activity_buttons_delete_button: {
                setInsertMode(false);
                break;
            }
        }
    }

    @Subscribe
    public void onButtonsFragmentClick(ButtonFragmentEvents.Outgoing.ClickEvent clickEvent) {

        Action newAction = null;

        if (mCurrentApplicationInfo != null && mInsertMode) {
            newAction = mGeneratedAction;
        }

        mCurrentNode.getChild(clickEvent.index).setAction(newAction);
        updateButtonsFragment();

    }

    @Subscribe
    public void onButtonsFragmentLongClick(ButtonFragmentEvents.Outgoing.LongClickEvent clickEvent) {

        mCurrentNode = mCurrentNode.getChild(clickEvent.index);
        mCurrentPath.add(clickEvent.index);
        updateButtonsFragment();

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
//        clearEditMenu();

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
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case AppearanceActivity.APPEARANCE_ACTIVITY_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {

                    // If changes were made to the widget's appearance, rebuild the fragment
                    Bundle resultExtras = data.getExtras();
                    if (resultExtras.containsKey(AppearanceActivity.EXTRA_CHANGES_MADE) && resultExtras.getBoolean(EXTRA_CHANGES_MADE)) {
                        pendingAppearanceChange = true;
                    }

                }
                break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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


}
