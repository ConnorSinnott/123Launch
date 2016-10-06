package com.pluviostudios.onetwothreelaunch.dialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pluviostudios.onetwothreelaunch.action.ActionTools;
import com.pluviostudios.onetwothreelaunch.utilities.Utilities;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by spectre on 8/19/16.
 */
public class IconListDialogFragment extends DialogFragment {

    public static final String TAG = "IconListDialogFragment";

    public static final String EXTRA_REQUEST_CODE = "extra_request_code";
    public static final String EXTRA_TITLE_LIST = "extra_title_list";
    public static final String EXTRA_URI_LIST = "extra_uri_list";
    public static final String EXTRA_SHOW_TITLE = "extra_show_title";
    public static final String EXTRA_USE_PICASSO = "extra_use_picasso";
    public static final String EXTRA_TITLE = "extra_title";

    private int requestCode;
    private ArrayList<String> titleList;
    private ArrayList<Uri> uriList;

    private boolean isDialog = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ListView listView = new ListView(getContext());
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Utilities.checkBundleForExpectedExtras(getArguments(),
                EXTRA_REQUEST_CODE,
                EXTRA_TITLE_LIST,
                EXTRA_URI_LIST,
                EXTRA_SHOW_TITLE,
                EXTRA_TITLE);

        requestCode = getArguments().getInt(EXTRA_REQUEST_CODE);
        titleList = getArguments().getStringArrayList(EXTRA_TITLE_LIST);
        uriList = getArguments().getParcelableArrayList(EXTRA_URI_LIST);
        boolean usePicasso = getArguments().getBoolean(EXTRA_USE_PICASSO);
        boolean showTitle = getArguments().getBoolean(EXTRA_SHOW_TITLE);
        String dialogTitle = getArguments().getString(EXTRA_TITLE);

        if (showTitle) {
            getDialog().setTitle(dialogTitle);
        }

        IconListDialogAdapter.Builder iconListDialogAdapterBuilder = new IconListDialogAdapter.Builder();
        iconListDialogAdapterBuilder.usePicasso(usePicasso);
        for (int i = 0; i < titleList.size(); i++) {
            iconListDialogAdapterBuilder.addItem(titleList.get(i), uriList.get(i));
        }

        listView.setAdapter(iconListDialogAdapterBuilder.build());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EventBus.getDefault().post(new IconListDialogFragmentEvent(requestCode, i));
                if (isDialog) {
                    dismiss();
                }
            }
        });

        return listView;

    }

    /**
     * Created by spectre on 8/19/16.
     */
    public static class Builder {

        public static final String TAG = "IconListDataGenerator";

        final int requestCode;
        String dialogTitle;
        boolean showTitle = false;
        boolean usePicasso = false;
        final ArrayList<String> titlesList = new ArrayList<>();
        final ArrayList<Uri> uriList = new ArrayList<>();
        final ArrayList<Serializable> mSerializableArrayList = new ArrayList<>();

        public Builder(int requestCode) {
            this.requestCode = requestCode;

        }

        public Builder addItem(String title, Uri imageUri) {
            titlesList.add(title);
            uriList.add(imageUri);
            mSerializableArrayList.add(null);
            return this;
        }

        public Builder addItem(String title, Context context, int resourceId) {
            return addItem(title, ActionTools.convertResourceToUri(context, resourceId));
        }

        public Builder setTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        public Builder showDialogTitle(boolean showDialogTitle) {
            showTitle = showDialogTitle;
            return this;
        }

        public Builder usePicasso(boolean usePicasso) {
            this.usePicasso = usePicasso;
            return this;
        }

        public IconListDialogFragment build() {

            IconListDialogFragment iconListDialogFragment2 = new IconListDialogFragment();
            Bundle extras = new Bundle();
            extras.putInt(EXTRA_REQUEST_CODE, requestCode);
            extras.putStringArrayList(EXTRA_TITLE_LIST, titlesList);
            extras.putParcelableArrayList(EXTRA_URI_LIST, uriList);
            extras.putString(EXTRA_TITLE, dialogTitle);
            extras.putBoolean(EXTRA_SHOW_TITLE, showTitle);
            extras.putBoolean(EXTRA_USE_PICASSO, usePicasso);
            iconListDialogFragment2.setArguments(extras);
            return iconListDialogFragment2;

        }

    }
}
