package com.pluviostudios.dialin.configManagerActivity.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.database.DBContract;
import com.pluviostudios.dialin.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/2/16.
 */
public class ConfigurationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String TAG = "ConfigListFragment";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";

    @BindView(R.id.activity_list_listview) ListView mListView;
    @BindView(R.id.activity_list_fab) FloatingActionButton mNewConfigButton;
    @BindView(R.id.activity_list_no_configurations) TextView mNoConfigurations;

    protected OnConfigurationSelected mOnConfigurationSelected;
    protected CursorAdapter mCursorAdapter;
    private View mRoot;

    public static ConfigurationListFragment buildConfigListFragment(int buttonCount) {
        ConfigurationListFragment configurationListFragment = new ConfigurationListFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_BUTTON_COUNT, buttonCount);
        configurationListFragment.setArguments(extras);
        return configurationListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.activity_list, container, false);
        ButterKnife.bind(this, mRoot);

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_BUTTON_COUNT);

        // Init loader which will populate the listView with configuration files relevant to buttonCount
        getLoaderManager().initLoader(0, extras, this);
        return mRoot;

    }

    @Override
    public void onAttach(Context context) {

        if (!(context instanceof OnConfigurationSelected))
            throw new RuntimeException("Parent activity should implement " + OnConfigurationSelected.class.getCanonicalName());

        mOnConfigurationSelected = (OnConfigurationSelected) context;

        super.onAttach(context);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create the loader
        int buttonCount = args.getInt(EXTRA_BUTTON_COUNT);
        return new CursorLoader(getContext(),
                DBContract.ConfigEntry.buildConfigWithButtonCount(buttonCount),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // If there are no configurations, inform the user
        mNoConfigurations.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);

        // Create the cursor adapter, simply displays the filenames in a list
        mCursorAdapter = new CursorAdapter(getContext(), data) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.config_list_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                // Get id and configTitle from cursor
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.ConfigEntry._ID));
                String configTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.ConfigEntry.TITLE_COL));

                // Place the id of the entry into the view so that the onItemClickListener can get it later
                view.setTag(id);

                // Add a click listener to the list item
                view.setOnClickListener(ConfigurationListFragment.this);

                TextView title = (TextView) view.findViewById(R.id.config_list_item_textview);
                title.setText(configTitle);

                // Add a click listener to the edit configuration button
                ImageButton imageButton = (ImageButton) view.findViewById(R.id.config_list_item_button_edit);
                imageButton.setOnClickListener(ConfigurationListFragment.this);

            }

        };
        mListView.setAdapter(mCursorAdapter);

        // Add a click listener to the new configuration button
        mNewConfigButton.setOnClickListener(this);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }
    }


    @Override
    public void onClick(View view) {

        if (view instanceof FloatingActionButton) {
            // The user is attempting to make a new configuration | view = FAB

            mOnConfigurationSelected.onNewConfiguration();

        } else if (view instanceof ImageButton) {
            // The user is attempting to edit a configuration | view = edit configuration image button

            View parentView = ((ViewGroup) view.getParent());

            long configId = (long) parentView.getTag();
            String configTitle = ((TextView) parentView.findViewById(R.id.config_list_item_textview)).getText().toString();

            mOnConfigurationSelected.onConfigurationEdit(configTitle, configId);

        } else {
            // The user is selecting a configuration | view = configuration list item

            long id = (long) view.getTag();
            mOnConfigurationSelected.onConfigurationSelected(id);

        }

    }

    public interface OnConfigurationSelected {
        void onConfigurationEdit(String configurationTitle, long configurationId);

        void onConfigurationSelected(long configurationId);

        void onNewConfiguration();
    }

}
