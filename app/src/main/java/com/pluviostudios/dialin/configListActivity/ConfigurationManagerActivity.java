package com.pluviostudios.dialin.configListActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.primitives.Ints;
import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionManager;
import com.pluviostudios.dialin.appearanceActivity.AppearanceActivity;
import com.pluviostudios.dialin.buttonsActivity.ButtonsActivity;
import com.pluviostudios.dialin.configListActivity.fragments.ConfigurationListFragment;
import com.pluviostudios.dialin.data.StorageManager;
import com.pluviostudios.dialin.settings.SettingsActivity;
import com.pluviostudios.dialin.widget.SupportedWidgetSizes;
import com.pluviostudios.dialin.widget.WidgetManager;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/2/16.
 */
public class ConfigurationManagerActivity extends AppCompatActivity implements ConfigurationListFragment.OnConfigurationSelected {

    public static final String TAG = "ConfigManagerActivity";

    @BindView(R.id.activity_configuration_manager_viewpager) ViewPager mViewPager;
    @BindView(R.id.activity_configuration_manager_titles) TitlePageIndicator mTitlePageIndicator;

    private int mWidgetId = 0;
    private int mWidgetButtonCount = 0;

    // If this activity has been launched as a configuration activity, I only want to show configurations with the same button count
    // Otherwise, I would want to display all the options.
    // Due to this you'll see some annoying code snippits such as within getPageTitle() where I figure out whether to show 2 pages or 1. 5x1 or 4x1 or both.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_manager);
        ButterKnife.bind(this);
        ActionManager.initialize(this);

        // Check to see if this activity was started by a widget
        if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {

            //Get mWidgetId (Will increment every time a widget is added to the home screen)
            mWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            //Determine button count
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            Bundle options = appWidgetManager.getAppWidgetOptions(mWidgetId);
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            mWidgetButtonCount = (width + 30) / 70; // Homescreen tile = n * 70 - 30

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
        mTitlePageIndicator.setTextColor(getResources().getColor(android.R.color.primary_text_light)); // TODO find out how to do this in XML
        mTitlePageIndicator.setSelectedColor(getResources().getColor(android.R.color.primary_text_light)); // TODO find out how to do this in XML
        mViewPager.addOnPageChangeListener(mTitlePageIndicator);

    }

    // Called when the user is attempting to edit a configuration
    @Override
    public void onConfigurationEdit(long configurationId) {

        // Launch the configuration activity
        Intent intent = ButtonsActivity.buildMainActivity(this, configurationId);
        startActivityForResult(intent, ButtonsActivity.EDIT_CONFIG_RESULT_CODE);

    }

    // Called then the user is attempting to create a new configuration
    @Override
    public void onNewConfiguration() {

        int buttonCount = (mWidgetButtonCount != 0) ? mWidgetButtonCount : SupportedWidgetSizes.SUPPORTED_WIDGET_SIZES[mViewPager.getCurrentItem()];

        Intent intent = ButtonsActivity.buildMainActivityForNewConfiguration(this,
                buttonCount + "x1 New Configuration",
                buttonCount);
        startActivityForResult(intent, ButtonsActivity.EDIT_CONFIG_RESULT_CODE);

    }

    // Called when the user has selected a configuration for their widget
    @Override
    public void onConfigurationSelected(long configurationId) {

        // If the application was started by a widget
        if (mWidgetId != 0) {

            // Add this widget to the database and attach it to this configuration
            WidgetManager.addWidgetToDB(this, mWidgetId, configurationId);

            // Update the new widget
            WidgetManager.updateWidgets(this, mWidgetId);

            Intent data = new Intent();
            data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
            setResult(RESULT_OK, data);
            finish();
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
                WidgetManager.updateWidgets(this, Ints.toArray(affectedWidgetIds));
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_configuration_list, menu);
        return true;
    }

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
}
