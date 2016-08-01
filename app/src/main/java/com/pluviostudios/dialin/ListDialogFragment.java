package com.pluviostudios.dialin;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/1/16.
 */
public class ListDialogFragment extends DialogFragment {

    public static final String TAG = "ListDialogFragment";
    private static final String EXTRA_NAMES = "extra_names";
    private static final String EXTRA_URIS = "extra_uris";

    private View mRoot;
    @BindView(R.id.dialog_fragment_action_list_list_view) ListView mListView;

    private String[] mNames;
    private BitmapDrawable[] mDrawables;

    protected OnListItemSelected mOnListItemSelected;

    public static ListDialogFragment buildListDialogFragment(ArrayList<Pair<String, DialinImage>> items) {

        ListDialogFragment listDialogFragment = new ListDialogFragment();
        Bundle extras = new Bundle();

        String[] itemNames = new String[items.size()];
        String[] itemImageUris = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
            itemNames[i] = items.get(i).first;
            itemImageUris[i] = items.get(i).second.imageUri.toString();
        }

        extras.putStringArray(EXTRA_NAMES, itemNames);
        extras.putStringArray(EXTRA_URIS, itemImageUris);

        listDialogFragment.setArguments(extras);

        return listDialogFragment;

    }

    @Override
    public void onAttach(Context context) {

        if (!(context instanceof OnListItemSelected))
            throw new RuntimeException("Parent activity should implement " + OnListItemSelected.class.getCanonicalName());

        mOnListItemSelected = (OnListItemSelected) context;

        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.dialog_fragment_action_list, container, false);
        ButterKnife.bind(this, mRoot);

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_NAMES,
                EXTRA_URIS);

        // Get names and drawables from arguements
        mNames = extras.getStringArray(EXTRA_NAMES);
        mDrawables = Utilities.generateBitmapDrawableArrayFromStringURI(getContext(), extras.getStringArray(EXTRA_URIS));

        // Create list items
        ArrayList<DialogListItem> listItems = new ArrayList<>();
        for (int i = 0; i < mNames.length; i++) {
            DialogListItem newListItem = new DialogListItem();
            newListItem.title = mNames[i];
            newListItem.drawable = mDrawables[i];
            listItems.add(newListItem);
        }

        // Show list
        mListView.setAdapter(new DialogListAdapter(getContext(), listItems));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnListItemSelected.onListItemSelected(i);
                dismiss();
            }
        });

        return mRoot;
    }

    private class DialogListAdapter extends ArrayAdapter<DialogListItem> {

        public DialogListAdapter(Context context, List<DialogListItem> objects) {
            super(context, R.layout.list_item_action, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action, null);

            DialogListItem currentItem = getItem(position);

            String displayName = currentItem.title;
            Drawable displayIcon = currentItem.drawable;

            ((ImageView) convertView.findViewById(R.id.list_item_action_image)).setImageDrawable(displayIcon);
            ((TextView) convertView.findViewById(R.id.list_item_action_text_view)).setText(displayName);

            return convertView;

        }
    }

    private class DialogListItem {
        public String title;
        public Drawable drawable;
    }

    public interface OnListItemSelected {
        void onListItemSelected(int position);
    }

}
