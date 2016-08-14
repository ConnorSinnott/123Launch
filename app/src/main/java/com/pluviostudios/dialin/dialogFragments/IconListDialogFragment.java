package com.pluviostudios.dialin.dialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.action.ActionManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/1/16.
 */
public class IconListDialogFragment<T> extends DialogFragment {

    public static final String TAG = "IconListDialogFragment";

    private View mRoot;
    @BindView(R.id.dialog_fragment_action_list_list_view) ListView mListView;

    private ArrayList<T> objectList;
    private Integer mSelected;
    private boolean isDialog = false;
    protected OnListItemSelected mOnListItemSelected;
    protected IconListDialogItemAdapter mItemAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    public void setItems(ArrayList<T> objectList) {
        this.objectList = objectList;
    }

    public void setSelected(int position) {
        mSelected = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.dialog_fragment_action_list, container, false);
        ButterKnife.bind(this, mRoot);

        mListView.setAdapter(new DialogListAdapter(ActionManager.getContext(), objectList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (mOnListItemSelected != null) {

                    mOnListItemSelected.onListItemSelected(objectList.get(i), i);
                    mSelected = i;
                    view.setSelected(true);

                }
                if (isDialog) {
                    dismiss();
                }
            }
        });
        return mRoot;

    }

    private class DialogListAdapter extends ArrayAdapter<T> {

        public DialogListAdapter(Context context, List<T> objects) {
            super(context, R.layout.list_item_action, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action, null);

            if (mItemAdapter == null)
                throw new RuntimeException("Missing ItemAdapter");

            T currObject = getItem(position);

            Uri displayUri = mItemAdapter.getImageUri(currObject);
            String displayName = mItemAdapter.getString(currObject);

            ((ImageView) convertView.findViewById(R.id.list_item_action_image)).setImageURI(displayUri);
            ((TextView) convertView.findViewById(R.id.list_item_action_text_view)).setText(displayName);

            return convertView;

        }
    }

    public interface OnListItemSelected<T> {
        void onListItemSelected(T object, int position);
    }

    public interface IconListDialogItemAdapter<T> {

        String getString(T object);

        Uri getImageUri(T object);

    }

    public void setOnListItemSelected(OnListItemSelected<T> onListItemSelected) {
        mOnListItemSelected = onListItemSelected;
    }

    public void setItemAdapter(IconListDialogItemAdapter<T> itemAdapter) {
        mItemAdapter = itemAdapter;
    }

}
