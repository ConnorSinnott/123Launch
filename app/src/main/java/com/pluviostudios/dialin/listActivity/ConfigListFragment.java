package com.pluviostudios.dialin.listActivity;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.database.DBContract;
import com.pluviostudios.dialin.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/2/16.
 */
public class ConfigListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "ConfigListFragment";
    public static final String EXTRA_BUTTON_COUNT = "extra_button_count";

    @BindView(R.id.activity_list_listview) ListView mListView;
    @BindView(R.id.activity_list_fab) FloatingActionButton mNewConfigButton;

    CursorAdapter mCursorAdapter;

    private View mRoot;

    public static ConfigListFragment buildConfigListFragment(int buttonCount) {
        ConfigListFragment configListFragment = new ConfigListFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_BUTTON_COUNT, buttonCount);
        configListFragment.setArguments(extras);
        return configListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.activity_list, container, false);
        ButterKnife.bind(this, mRoot);

        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_BUTTON_COUNT);

        getLoaderManager().initLoader(0, extras, this);
        return mRoot;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        int buttonCount = args.getInt(EXTRA_BUTTON_COUNT);

        return new CursorLoader(getContext(),
                DBContract.ConfigEntry.buildConfigWithButtonCount(buttonCount),
                null,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter = new CursorAdapter(getContext(), data) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                // Get id and filename from cursor
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.ConfigEntry._ID));
                String filename = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.ConfigEntry.FILENAME_COL));

                // Place the id of the entry into the view so that the onItemClickListener can get it later
                view.setTag(id);
                ((TextView) view.findViewById(android.R.id.text1)).setText(filename);

            }

        };

        final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), String.valueOf(view.getTag()), Toast.LENGTH_SHORT).show();
            }
        };

        mListView.setAdapter(mCursorAdapter);
        mListView.setOnItemClickListener(onItemClickListener);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }
    }

}
