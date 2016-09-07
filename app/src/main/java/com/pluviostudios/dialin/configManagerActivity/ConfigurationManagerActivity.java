package com.pluviostudios.dialin.configManagerActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.appearanceActivity.AppearanceActivity;
import com.pluviostudios.dialin.buttonsActivity.ButtonsActivity;
import com.pluviostudios.dialin.configManagerActivity.fragments.ConfigurationListFragment;
import com.pluviostudios.dialin.configManagerActivity.fragments.ConfigurationListFragmentEvents;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragmentEvent;
import com.pluviostudios.dialin.settings.SettingsActivity;
import com.pluviostudios.dialin.widget.SupportedWidgetSizes;
import com.pluviostudios.dialin.widget.WidgetManager;
import com.pluviostudios.dialin.widget.WidgetTools;
import com.viewpagerindicator.TitlePageIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

/**
 * Created by spectre on 8/2/16.
 */
public class ConfigurationManagerActivity extends AppCompatActivity {

    public static final String TAG = "ConfigManagerActivity";

    public static final String SAVED_CURRENTLY_SELECTED_CONFIGURATION_ID = "saved_configId";

    public static final int DIALOG_REQUEST_CODE = 2521;

    private ViewPager mViewPager;
    private TitlePageIndicator mTitlePageIndicator;

    private int mWidgetId = 0;
    private int mWidgetButtonCount = 0;
    private Long currentlySelectedConfigurationId;

    // If this activity has been launched as a configuration activity, I only want to show configurations with the same button count
    // Otherwise, I would want to display all the options.
    // Due to this you'll see some annoying code snippits such as within getPageTitle() where I figure out whether to show 2 pages or 1. 5x1 or 4x1 or both.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_manager);

        mTitlePageIndicator = (TitlePageIndicator) findViewById(R.id.activity_configuration_manager_titles);
        mViewPager = (ViewPager) findViewById(R.id.activity_configuration_manager_viewpager);

        Toast.makeText(this, "123Go Debug Copy", Toast.LENGTH_SHORT).show();

        // Initialize action manager with this context
        ActionManager.initialize(this);

        // Check to see if this activity was started by a widget
        if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {

            //Get mWidgetId (Will increment every time a widget is added to the home screen)
            mWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            mWidgetButtonCount = WidgetTools.getTileCount(this, mWidgetId);

        }

        // Set the pager adapter. Each page will contain a list of Configurations of a specific button count
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public CharSequence getPageTitle(int position) {
                return ((mWidgetButtonCount != 0) ? mWidgetButtonCount : SupportedWidgetSizes.SUPPORTED_WIDGET_SIZES[position]) + "x1";
            }

            @Override
            public Fragment getItem(int position) {
                return ConfigurationListFragment.buildConfigListFragment((mWidgetButtonCount == 0) ? SupportedWidgetSizes.SUPPORTED_WIDGET_SIZES[position] : mWidgetButtonCount);
            }

            @Override
            public int getCount() {
                return mWidgetButtonCount != 0 ? 1 : SupportedWidgetSizes.SUPPORTED_WIDGET_SIZES.length;
            }

        });

        mTitlePageIndicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(mTitlePageIndicator);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_CURRENTLY_SELECTED_CONFIGURATION_ID)) {
            currentlySelectedConfigurationId = savedInstanceState.getLong(SAVED_CURRENTLY_SELECTED_CONFIGURATION_ID);
        }

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

    @Subscribe
    public void onConfigurationListFragmentEditEvent(ConfigurationListFragmentEvents.Outgoing.EditEvent event) {

        currentlySelectedConfigurationId = event.configurationId;

        new IconListDialogFragment.Builder(DIALOG_REQUEST_CODE)
                .addItem("Edit", this, R.drawable.ic_mode_edit_black_24px)
                .addItem("Delete", this, R.drawable.ic_delete_forever_black_24px)
                .build().show(getSupportFragmentManager(), IconListDialogFragment.TAG);


    }

    @Subscribe
    public void onConfigurationListFragmentSelectedEvent(ConfigurationListFragmentEvents.Outgoing.SelectedEvent event) {

        currentlySelectedConfigurationId = event.configurationId;

        // If the application was started by a widget
        if (mWidgetId != 0) {

            // Add this widget to the database and attach it to this configuration
            WidgetManager.addWidgetToDB(this, mWidgetId, event.configurationId);

            // Update the new widget
            WidgetManager.updateWidgets(this, mWidgetId);

            Intent data = new Intent();
            data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
            setResult(RESULT_OK, data);
            finish();
        }

    }

    @Subscribe
    public void onConfigurationListFragmentNewConfigurationEvent(ConfigurationListFragmentEvents.Outgoing.NewConfiguration event) {

        currentlySelectedConfigurationId = null;

        int buttonCount = (mWidgetButtonCount != 0) ? mWidgetButtonCount : SupportedWidgetSizes.SUPPORTED_WIDGET_SIZES[mViewPager.getCurrentItem()];

        Intent intent = ButtonsActivity.buildMainActivityForNewConfiguration(this,
                buttonCount + "x1 New Configuration",
                buttonCount);
        startActivityForResult(intent, ButtonsActivity.EDIT_CONFIG_RESULT_CODE);

    }

    @Subscribe
    public void onIconListDialogFragmentEvent(IconListDialogFragmentEvent event) {

        if (event.requestCode == DIALOG_REQUEST_CODE) {

            switch (event.position) {
                case 0: {
                    // Launch the configuration activity
                    Intent intent = ButtonsActivity.buildMainActivity(ConfigurationManagerActivity.this, currentlySelectedConfigurationId);
                    startActivityForResult(intent, ButtonsActivity.EDIT_CONFIG_RESULT_CODE);
                    break;
                }
                case 1: {

                    // Delete configuration from database
                    ArrayList<Integer> affectedAppWidgetIds = StorageManager.deleteConfiguration(ConfigurationManagerActivity.this, currentlySelectedConfigurationId);

                    // Notify widgets of change
                    WidgetManager.updateWidgets(ConfigurationManagerActivity.this, affectedAppWidgetIds);

                    break;
                }
            }

        }

    }


    // Called when the user is done configuring a layout
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If RESULT_OK
        if (requestCode == ButtonsActivity.EDIT_CONFIG_RESULT_CODE && resultCode == RESULT_OK) {

            ArrayList<Integer> affectedWidgetIds = data.getExtras().getIntegerArrayList(StorageManager.EXTRA_AFFECTED_APPWIDGETIDS);
            if (affectedWidgetIds != null) {
                WidgetManager.updateWidgets(this, affectedWidgetIds);
            }

        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_activity_configuration_list, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_appearance: {
                Intent intent = new Intent(this, AppearanceActivity.class);
                startActivity(intent);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (currentlySelectedConfigurationId != null) {
            outState.putLong(SAVED_CURRENTLY_SELECTED_CONFIGURATION_ID, currentlySelectedConfigurationId);
        }
        super.onSaveInstanceState(outState);
    }
}
