package com.pluviostudios.dialin.dialogFragments;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionTools;

import java.util.ArrayList;

/**
 * Created by spectre on 9/16/16.
 */
public class IconListDialogAdapter extends BaseAdapter {

    private final ArrayList<String> mTitleList;
    private final ArrayList<Uri> mImageList;

    public IconListDialogAdapter(ArrayList<String> titleList, ArrayList<Uri> imageList) {
        mTitleList = titleList;
        mImageList = imageList;
    }

    @Override
    public int getCount() {
        return mTitleList.size();
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

        Uri displayUri = mImageList.get(position);
        String displayName = mTitleList.get(position);

        ((ImageView) convertView.findViewById(R.id.list_item_action_image)).setImageURI(displayUri);
        ((TextView) convertView.findViewById(R.id.list_item_action_text_view)).setText(displayName);

        return convertView;

    }

    public static class Builder {

        final ArrayList<String> titlesList = new ArrayList<>();
        final ArrayList<Uri> uriList = new ArrayList<>();

        public Builder addItem(String title, Uri imageUri) {
            titlesList.add(title);
            uriList.add(imageUri);
            return this;
        }

        public Builder addItem(String title, Context context, int resourceId) {
            return addItem(title, ActionTools.convertResourceToUri(context, resourceId));
        }

        public IconListDialogAdapter build() {
            return new IconListDialogAdapter(titlesList, uriList);
        }

    }

}
