package com.pluviostudios.dialin.listActivity;

import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.pluviostudios.dialin.R;
import com.viewpagerindicator.TitlePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/2/16.
 */
public class ConfigurationManagerActivity extends AppCompatActivity {

    public static final String TAG = "ConfigurationManagerActivity";

    @BindView(R.id.activity_configuration_manager_viewpager) ViewPager mViewPager;
    @BindView(R.id.activity_configuration_manager_titles) TitlePageIndicator mTitlePageIndicator;

    private int mWidgetId = 0;
    private int mWidgetButtonCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_manager);
        ButterKnife.bind(this);

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

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public CharSequence getPageTitle(int position) {

                if (mWidgetButtonCount == 0) {

                    // Todo, get the unique button counts from DB to automate the below process
                    switch (position) {
                        case 0: {
                            // Todo Hard Coded!
                            return "4x1";
                        }
                        case 1: {
                            // Todo Hard Coded!
                            return "5x1";
                        }
                    }

                }

                return mWidgetButtonCount + "x1";

            }

            @Override
            public Fragment getItem(int position) {

                if (mWidgetButtonCount == 0) {

                    // Todo, get the unique button counts from DB to automate the below process
                    switch (position) {
                        case 0: {
                            // Todo Hard Coded!
                            return ConfigListFragment.buildConfigListFragment(4);
                        }
                        case 1: {
                            // Todo Hard Coded!
                            return ConfigListFragment.buildConfigListFragment(5);
                        }
                    }

                }

                return ConfigListFragment.buildConfigListFragment(mWidgetButtonCount);

            }

            @Override
            public int getCount() {
                // If the activity was started by a widget, only show configurations with the same button count
                // Todo Hard Coded
                return mWidgetButtonCount != 0 ? 1 : 2;
            }

        });

        mTitlePageIndicator.setViewPager(mViewPager);
        mTitlePageIndicator.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        mTitlePageIndicator.setSelectedColor(getResources().getColor(android.R.color.primary_text_light));
        mViewPager.addOnPageChangeListener(mTitlePageIndicator);

    }


}
