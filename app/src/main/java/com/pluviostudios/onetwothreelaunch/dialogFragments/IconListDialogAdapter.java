package com.pluviostudios.onetwothreelaunch.dialogFragments;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.onetwothreelaunch.R;
import com.pluviostudios.onetwothreelaunch.action.ActionTools;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by spectre on 9/16/16.
 */
public class IconListDialogAdapter extends BaseAdapter {

    private final ArrayList<String> mTitleList;
    private final ArrayList<Uri> mImageList;
    private final boolean mUsePicasso;

    public IconListDialogAdapter(ArrayList<String> titleList, ArrayList<Uri> imageList, boolean usePicasso) {
        mTitleList = titleList;
        mImageList = imageList;
        mUsePicasso = usePicasso;
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

        if (mUsePicasso) {
            Picasso.with(parent.getContext()).load(displayUri).into(((ImageView) convertView.findViewById(R.id.list_item_action_image)));
        } else {
            ((ImageView) convertView.findViewById(R.id.list_item_action_image)).setImageURI(displayUri);
        }
        ((TextView) convertView.findViewById(R.id.list_item_action_text_view)).setText(displayName);

        return convertView;

    }

    public static class Builder {

        private boolean usePicasso = false;
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

        public Builder usePicasso(boolean usePicasso) {
            this.usePicasso = usePicasso;
            return this;
        }

        public IconListDialogAdapter build() {
            return new IconListDialogAdapter(titlesList, uriList, usePicasso);
        }

    }

}
