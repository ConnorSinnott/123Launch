package com.pluviostudios.dialin.dialogFragments;

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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionTools;
import com.pluviostudios.dialin.utilities.Utilities;

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
        boolean showTitle = getArguments().getBoolean(EXTRA_SHOW_TITLE);
        String dialogTitle = getArguments().getString(EXTRA_TITLE);

        if (showTitle) {
            getDialog().setTitle(dialogTitle);
        }

        listView.setAdapter(new IconListDialogAdapter());
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

    private class IconListDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action, parent, false);

            Uri displayUri = uriList.get(position);
            String displayName = titleList.get(position);

            ((ImageView) convertView.findViewById(R.id.list_item_action_image)).setImageURI(displayUri);
            ((TextView) convertView.findViewById(R.id.list_item_action_text_view)).setText(displayName);

            return convertView;

        }

    }

    /**
     * Created by spectre on 8/19/16.
     */
    public static class Builder {

        public static final String TAG = "IconListDataGenerator";

        final int requestCode;
        String dialogTitle;
        boolean showTitle = false;
        ArrayList<String> titlesList = new ArrayList<>();
        ArrayList<Uri> uriList = new ArrayList<>();
        ArrayList<Serializable> mSerializableArrayList = new ArrayList<>();

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

        public IconListDialogFragment build() {

            IconListDialogFragment iconListDialogFragment2 = new IconListDialogFragment();
            Bundle extras = new Bundle();
            extras.putInt(EXTRA_REQUEST_CODE, requestCode);
            extras.putStringArrayList(EXTRA_TITLE_LIST, titlesList);
            extras.putParcelableArrayList(EXTRA_URI_LIST, uriList);
            extras.putString(EXTRA_TITLE, dialogTitle);
            extras.putBoolean(EXTRA_SHOW_TITLE, showTitle);
            iconListDialogFragment2.setArguments(extras);
            return iconListDialogFragment2;

        }

    }
}
