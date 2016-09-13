package com.pluviostudios.dialin.appearanceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.buttonIconSet.AppearanceItem;
import com.pluviostudios.dialin.buttonIconSet.AppearanceManager;
import com.pluviostudios.dialin.buttonIconSet.HighlightItem;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragment;
import com.pluviostudios.dialin.dialogFragments.IconListDialogFragmentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.pluviostudios.dialin.action.ActionManager.getContext;

/**
 * Created by spectre on 8/13/16.
 */
public class AppearanceActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "AppearanceActivity";

    public static final int APPEARANCE_ACTIVITY_REQUEST_CODE = 555;
    public static final String EXTRA_CHANGES_MADE = "extra_changes_made";

    private static final int HIGHLIGHT_REQUEST_CODE = 5610;
    private static final int BUTTON_SET_REQUEST_CODE = 9570;

    private Button mButtonSave;
    private View mListItemView;
    private ImageView mHighlightImageView;
    private TextView mHighlightTextView;

    private AppearanceItem mAppearanceItem;

    private boolean changesMade = false;

    private void init() {
        mButtonSave = (Button) findViewById(R.id.activity_appearance_button_save);
        mListItemView = findViewById(R.id.activity_appearance_list_item);
        mHighlightImageView = (ImageView) findViewById(R.id.list_item_action_image);
        mHighlightTextView = (TextView) findViewById(R.id.list_item_action_text_view);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Appearance");
        setContentView(R.layout.activity_appearance);
        init();

        mAppearanceItem = AppearanceManager.getAppearanceItem(getContext());

        updateHighlightItem();
        updateButtonSetItem();

        mListItemView.setOnClickListener(this);
        mButtonSave.setOnClickListener(this);

    }

    private void updateButtonSetItem() {

    }


    private void updateHighlightItem() {
        mHighlightTextView.setText(mAppearanceItem.highlightItem.title);
        mHighlightImageView.setImageResource(mAppearanceItem.highlightItem.previewResourceId);
    }

    private void showHighlightDialog() {

        IconListDialogFragment.Builder builder = new IconListDialogFragment.Builder(HIGHLIGHT_REQUEST_CODE);
        for (HighlightItem x : AppearanceManager.getHighlightItems(getContext())) {
            builder.addItem(x.title, getContext(), x.previewResourceId);
        }
        builder.build().show(getSupportFragmentManager(), IconListDialogFragment.TAG);

    }

    private void showSkinDialog() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.activity_appearance_button_save: {
                finishActivity();
                break;
            }

            case R.id.activity_appearance_list_item: {
                showHighlightDialog();
                break;
            }

        }
    }

    @Subscribe
    public void onIconListDialogFragmentEvent(IconListDialogFragmentEvent event) {

        switch (event.requestCode) {
            case HIGHLIGHT_REQUEST_CODE: {

                mAppearanceItem.highlightItem = AppearanceManager.getHighlightItems(getContext()).get(event.position);
                changesMade = true;
                updateHighlightItem();

                break;
            }

            case BUTTON_SET_REQUEST_CODE: {
                break;
            }

        }

    }

    private void finishActivity() {

        if (changesMade) {
            AppearanceManager.setAppearanceItem(this, mAppearanceItem);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CHANGES_MADE, changesMade);
        setResult(RESULT_OK, resultIntent);
        finish();

    }


}
