package com.pluviostudios.dialin.appearanceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pluviostudios.dialin.R;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSetManager;
import com.pluviostudios.dialin.buttonsActivity.fragments.ButtonsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by spectre on 8/13/16.
 */
public class AppearanceActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "AppearanceActivity";

    public static final int APPEARANCE_ACTIVITY_REQUEST_CODE = 555;
    public static final String EXTRA_CHANGES_MADE = "extra_changes_made";

    private static final int DEFAULT_BUTTON_COUNT = 5;

    @BindView(R.id.activity_buttons_save_button) Button mButtonSave;

    private boolean changesMade = false;
    private ButtonIconSet mButtonIconSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Appearance");
        setContentView(R.layout.activity_buttons);

        ButterKnife.bind(this);
        mButtonSave.setOnClickListener(this);

        mButtonIconSet = ButtonIconSetManager.getButtonIconSet(this, DEFAULT_BUTTON_COUNT);

        showButtonsFragment();

    }

    private void showButtonsFragment() {
        if (mButtonIconSet != null) {
            ButtonsFragment buttonsFragment = ButtonsFragment.buildButtonsFragment(DEFAULT_BUTTON_COUNT, mButtonIconSet);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_buttons_top_frame, buttonsFragment)
                    .commit();
        }
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

}
