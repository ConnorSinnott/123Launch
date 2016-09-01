package com.pluviostudios.dialin.appearanceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.appearanceActivity.fragments.AvailableSkinsDialog;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonsFragment;

/**
 * Created by spectre on 8/13/16.
 */
public class AppearanceActivity extends AppCompatActivity implements View.OnClickListener, AvailableSkinsDialog.OnSkinSelected {

    public static final String TAG = "AppearanceActivity";

    private static final int DEFAULT_BUTTON_COUNT = 5;

    public static final int APPEARANCE_ACTIVITY_REQUEST_CODE = 555;
    public static final String EXTRA_CHANGES_MADE = "extra_changes_made";

    private Button mButtonSave;

    private AvailableSkinsDialog mAvailableSkinsDialog;
    private boolean changesMade = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Appearance");
        setContentView(R.layout.activity_buttons);
        mButtonSave = (Button) findViewById(R.id.activity_buttons_save_button);

        mButtonSave.setOnClickListener(this);

        showButtonsFragment();

    }

    @Override
    protected void onStart() {
        // Reestablish onSkinSelected onStart

        if (mAvailableSkinsDialog == null) {
            mAvailableSkinsDialog = new AvailableSkinsDialog();
            mAvailableSkinsDialog.show(getSupportFragmentManager(), AvailableSkinsDialog.TAG);
        }

        mAvailableSkinsDialog.setOnSkinSelected(this);

        super.onStart();
    }

    private void showButtonsFragment() {
        ButtonsFragment buttonsFragment = ButtonsFragment.buildButtonsFragment(DEFAULT_BUTTON_COUNT);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_buttons_top_frame, buttonsFragment)
                .commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.activity_buttons_save_button: {
                finishActivity();
                break;
            }

        }
    }

    private void finishActivity() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CHANGES_MADE, changesMade);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onInternetSkinDownload() {

    }

    @Override
    public void onSkinSelected() {

    }
}
