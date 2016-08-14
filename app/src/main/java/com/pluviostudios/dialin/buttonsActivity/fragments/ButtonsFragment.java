package com.pluviostudios.dialin.buttonsActivity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pluviostudios.dialin.action.DialinImage;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.utilities.Utilities;

/**
 * Created by spectre on 7/26/16.
 */
public class ButtonsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "ButtonsFragment";
    private static final String EXTRA_COUNT = "extra_count";
    private static final String EXTRA_BUTTON_SET = "extra_button_set";
    public static final String EXTRA_LAUNCH_BUTTON_INDEX = "extra_launch_button_index";

    private LinearLayout mRoot;
    private ImageButton mLauncherButton;
    private ImageButton[] mChildrenButtons;

    private ButtonIconSet mButtonIconSet;
    private DialinImage mLauncherIcon;
    private DialinImage[] mChildrenIcons;

    private int mButtonCount;
    private Integer mHoldIndex;
    private int mLaunchButtonIndex;
    private boolean mIconsPending = false;

    protected OnButtonsFragmentButtonClicked mOnButtonsFragmentButtonClicked;

    public static ButtonsFragment buildButtonsFragment(int count, ButtonIconSet buttonIconSet) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_COUNT, count);
        extras.putSerializable(EXTRA_BUTTON_SET, buttonIconSet);
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_COUNT,
                EXTRA_BUTTON_SET);

        // Set button count and determine launch index
        mButtonCount = extras.getInt(EXTRA_COUNT);

        // Add support for launch on left
        mLaunchButtonIndex = extras.getInt(EXTRA_LAUNCH_BUTTON_INDEX, mButtonCount - 1);

        // Get the passed ButtonIconSet
        mButtonIconSet = (ButtonIconSet) extras.getSerializable(EXTRA_BUTTON_SET);

        // Generate the button view
        mRoot = new LinearLayout(getContext());
        mRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setOrientation(LinearLayout.HORIZONTAL);
        mRoot.setGravity(Gravity.CENTER);

        mChildrenButtons = new ImageButton[mButtonCount - 1];
        boolean launchPlaced = false;
        for (int i = 0; i < mButtonCount; i++) {

            FrameLayout buttonFrameLayout;
            int relativeChildIndex = i - (launchPlaced ? 1 : 0);

            ImageButton newImageButton;

            // Determine which buttons get launcher icons vs standard icons
            if (i == mLaunchButtonIndex) {
                launchPlaced = true;
                buttonFrameLayout = generateLaunchButton(getContext(), mButtonIconSet);

                newImageButton = (ImageButton) buttonFrameLayout.findViewWithTag("ImageButton");
                newImageButton.setTag(-1);

                mLauncherButton = newImageButton;

            } else {

                buttonFrameLayout = generateChildButton(getContext(), mButtonIconSet, relativeChildIndex);

                newImageButton = (ImageButton) buttonFrameLayout.findViewWithTag("ImageButton");
                newImageButton.setTag(relativeChildIndex);

                mChildrenButtons[relativeChildIndex] = newImageButton;

            }

            newImageButton.setOnClickListener(this);
            newImageButton.setOnLongClickListener(this);

            mRoot.addView(buttonFrameLayout);

        }

        // If there are cached icons waiting to be displayed, display them
        if (mIconsPending) {
            mIconsPending = false;
            updateIcons();
        }

        return mRoot;

    }

    private static FrameLayout generateButtonFrameLayout(Context context, int buttonHighlightResourceId) {

        // Determine Button Size
        // Todo find a better way to determine button width
        final int buttonSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, context.getResources().getDisplayMetrics()));

        // For placing ImageView under ImageButton
        FrameLayout buttonFrameLayout = new FrameLayout(context);
        buttonFrameLayout.setTag("ButtonFrameLayout");
        buttonFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

        // For displaying the background
        ImageView backgroundImageView = new ImageView(context);
        backgroundImageView.setTag("BackgroundImageView");
        backgroundImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttonFrameLayout.addView(backgroundImageView);

        // When pressed, this button will display a highlight ring over the ImageView, it also houses the icons.
        // Unfortunately, this elaborate setup is the only way to allow customizable button press effects within the RemoteViews used for the AppWidget.
        // For simplicity and consistency, I used the same system here, rather than using a StateListDrawable
        ImageButton newImageButton = new ImageButton(context);
        newImageButton.setTag("ImageButton");
        newImageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newImageButton.setBackgroundResource(buttonHighlightResourceId);
        buttonFrameLayout.addView(newImageButton);

        return buttonFrameLayout;

    }

    private static FrameLayout generateChildButton(Context context, ButtonIconSet buttonIconSet, int index) {
        FrameLayout generatedButton = generateButtonFrameLayout(context, buttonIconSet.getButtonHighlightStateDrawableResourceId());
        ImageView backgroundImageView = (ImageView) generatedButton.findViewWithTag("BackgroundImageView");
        backgroundImageView.setImageURI(buttonIconSet.getIcon(index));
        return generatedButton;
    }

    private static FrameLayout generateLaunchButton(Context context, ButtonIconSet buttonIconSet) {
        FrameLayout generatedButton = generateButtonFrameLayout(context, buttonIconSet.getButtonHighlightStateDrawableResourceId());
        ImageView backgroundImageView = (ImageView) generatedButton.findViewWithTag("BackgroundImageView");
        backgroundImageView.setImageURI(buttonIconSet.getLauncher());
        return generatedButton;
    }

    public void setOnButtonsFragmentButtonClicked(OnButtonsFragmentButtonClicked onButtonsFragmentButtonClicked) {
        mOnButtonsFragmentButtonClicked = onButtonsFragmentButtonClicked;
    }

    private void updateIcons() {

        // If the fragment is visible to the user
        if (isAdded()) {

            if (mLauncherIcon != null) {
                mLauncherButton.setImageURI(mLauncherIcon.getImageUri());
            } else {
                mLauncherButton.setImageBitmap(null);
            }

            for (int i = 0; i < mChildrenIcons.length; i++) {
                if (mChildrenIcons[i] != null) {
                    mChildrenButtons[i].setImageURI(mChildrenIcons[i].getImageUri());
                } else {
                    mChildrenButtons[i].setImageBitmap(null);
                }
            }

        } else {
            // Otherwise flag this fragment to update widgets on attach
            mIconsPending = true;
        }

    }

    public void setIcons(DialinImage launcherIcon, DialinImage[] childrenIcons) {
        mLauncherIcon = launcherIcon;
        mChildrenIcons = childrenIcons;
        updateIcons();
    }

    @Override
    public void onClick(View view) {


        // Which button was clicked. -1 if Launch
        int buttonIndex = (int) view.getTag();

        Log.d(TAG, "onClick: " + buttonIndex);

        if (mOnButtonsFragmentButtonClicked != null) {

            if (buttonIndex >= 0) {

                // Standard button was clicked
                mOnButtonsFragmentButtonClicked.onButtonClicked(buttonIndex);

            } else {

                // Launch button was clicked
                mOnButtonsFragmentButtonClicked.onLaunchButtonClicked();

            }

            clearHold();

        }

    }

    @Override
    public boolean onLongClick(View view) {

        // Which button was clicked
        int buttonIndex = (int) view.getTag();

        if (mOnButtonsFragmentButtonClicked != null) {

            // Alert the listener
            if (buttonIndex >= 0) {

                // Standard button was long clicked
                mOnButtonsFragmentButtonClicked.onButtonLongClicked(buttonIndex);

                // Set hold image
                setHoldOnButton(buttonIndex);

            } else {

                // Consider this a launch button click
                mOnButtonsFragmentButtonClicked.onLaunchButtonClicked();

            }

        }

        return true; // Do not propagate up

    }

    public void setHoldOnButton(int index) {

        // Set hold image
        if (mHoldIndex != null) {
            clearHold();
        }

        mHoldIndex = index;
        mChildrenButtons[index].setBackgroundResource(mButtonIconSet.getButtonHighlightResourceId());

    }

    public void clearHold() {

        // Reset hold image
        if (mHoldIndex != null) {
            mChildrenButtons[mHoldIndex].setBackgroundResource(mButtonIconSet.getButtonHighlightStateDrawableResourceId());
            mHoldIndex = null;
        }

    }

    public interface OnButtonsFragmentButtonClicked {
        void onButtonClicked(int index);

        void onButtonLongClicked(int index);

        void onLaunchButtonClicked();
    }

}
