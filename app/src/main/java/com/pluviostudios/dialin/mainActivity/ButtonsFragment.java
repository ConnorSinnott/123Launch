package com.pluviostudios.dialin.mainActivity;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    public static final String EXTRA_LAUNCH_ON_LEFT = "extra_launch_on_left";

    private static final String ID_BUTTON = "button_fragment_button";

    private LinearLayout mRoot;
    protected ButtonIconSet mButtonIconSet;
    private ImageButton mHoldButton;
    private StateListDrawable mHoldStateList;
    private ImageButton[] mButtons;
    private int mLaunchButtonIndex;

    private DialinImage[] pendingIconList;
    private boolean iconsPending = false;

    protected OnButtonsFragmentButtonClicked mOnButtonsFragmentButtonClicked;

    public static ButtonsFragment buildButtonsFragment(int count, ButtonIconSet buttonIconSet) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_COUNT, count);
        extras.putSerializable(EXTRA_BUTTON_SET, buttonIconSet);
        fragment.setArguments(extras);
        return fragment;
    }

    public static String generateButtonTag(int buttonIndex) {
        return ID_BUTTON + "_" + buttonIndex;
    }

    public static int getIndexFromButtonTag(String buttonTag) {
        return Integer.parseInt(buttonTag.substring(buttonTag.length() - 1, buttonTag.length()));
    }

    // To allow for an unlimited amount of buttons, this view must be made dynamically.
    public static LinearLayout generateButtons(Context context, int buttonCount, boolean launchOnLeft, ButtonIconSet buttonSet) {

        // Create parent view
        LinearLayout buttonsView = new LinearLayout(context);
        buttonsView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttonsView.setOrientation(LinearLayout.HORIZONTAL);
        buttonsView.setGravity(Gravity.CENTER);

        // Determine Button Size
        // Todo find a better way to determine button width
        int buttonSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, context.getResources().getDisplayMetrics()));

        int startingIndex = 0;
        if (launchOnLeft) {
            startingIndex = 1;
        }

        // Add buttons to parent view
        for (int i = startingIndex; i < buttonCount - (launchOnLeft ? 0 : 1); i++) {

            ImageButton newImageButton = new ImageButton(context);
            newImageButton.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

            // Set tag to identify button later
            newImageButton.setTag(generateButtonTag(i));

            // Set button background to default image
            newImageButton.setBackground(buttonSet.getButtonIconStateDrawable(i));

            buttonsView.addView(newImageButton);

        }

        // Add launch button to the parent view and set its unique icon
        ImageButton launchImageButton = new ImageButton(context);
        launchImageButton.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
        launchImageButton.setBackground(buttonSet.getLauncherIconStateDrawable());

        // If launchOnLeft, place the launch button at index 0, otherwise, add it normally
        // Set the identifying tag accordingly. We can identify which is the launcher with mLauncherIndex
        if (launchOnLeft) {
            launchImageButton.setTag(generateButtonTag(0));
            buttonsView.addView(launchImageButton, 0);
        } else {
            launchImageButton.setTag(generateButtonTag(buttonCount - 1));
            buttonsView.addView(launchImageButton);
        }

        return buttonsView;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_COUNT,
                EXTRA_BUTTON_SET);

        // Set button count and determine launch index
        int buttonCount = extras.getInt(EXTRA_COUNT);

        // Add support for launch on left
        if (extras.containsKey(EXTRA_LAUNCH_ON_LEFT) && extras.getBoolean(EXTRA_LAUNCH_ON_LEFT)) {
            mLaunchButtonIndex = 0;
        } else {
            mLaunchButtonIndex = buttonCount - 1;
        }

        // Get the passed ButtonIconSet
        mButtonIconSet = (ButtonIconSet) extras.getSerializable(EXTRA_BUTTON_SET);

        // Generate the button view
        mRoot = generateButtons(getContext(), buttonCount, false, mButtonIconSet);

        // The views exist but have no functionality. Find the buttons via tags and add listeners
        mButtons = new ImageButton[buttonCount];
        for (int i = 0; i < buttonCount; i++) {
            ImageButton imageButton = (ImageButton) mRoot.findViewWithTag(generateButtonTag(i));
            mButtons[i] = imageButton;
            imageButton.setOnClickListener(this);
            imageButton.setOnLongClickListener(this);
        }

        // If there are cached icons waiting to be displayed, display them
        if (iconsPending) {
            iconsPending = false;
            setIcons(pendingIconList);
        }

        return mRoot;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnButtonsFragmentButtonClicked))
            throw new RuntimeException("Parent activity should implement " + OnButtonsFragmentButtonClicked.class.getCanonicalName());

        mOnButtonsFragmentButtonClicked = (OnButtonsFragmentButtonClicked) context;

    }

    public void setIcons(DialinImage[] iconList) {

        // If the fragment is visible, update the button images
        if (isAdded()) {

            if (iconList.length != mButtons.length)
                throw new RuntimeException("Passed iconList length does not match button count");

            for (int i = 0; i < mButtons.length; i++) {

                ImageButton currentButton = mButtons[i];
                Uri imageUri = iconList[i].imageUri;

//              Apply new image to button
                currentButton.setImageURI(imageUri);

//              Invalidate button
                currentButton.requestLayout();

            }

        } else {

            //Otherwise, store the images and update the buttons as soon as the fragment is attached
            iconsPending = true;
            pendingIconList = iconList;

        }

    }

    @Override
    public void onClick(View view) {

        // Which button was clicked
        int buttonIndex = getIndexFromButtonTag((String) view.getTag());

        if (mOnButtonsFragmentButtonClicked != null) {

            // Alert the listener
            if (buttonIndex == mLaunchButtonIndex) {

                // Launch button was clicked
                mOnButtonsFragmentButtonClicked.onLaunchButtonClicked();

            } else {

                // Standard button was clicked
                mOnButtonsFragmentButtonClicked.onButtonClicked(buttonIndex);
            }

            clearHold();

        }

    }

    @Override
    public boolean onLongClick(View view) {

        // Which button was clicked
        int buttonIndex = getIndexFromButtonTag((String) view.getTag());

        if (mOnButtonsFragmentButtonClicked != null) {

            // Alert the listener
            if (buttonIndex != mLaunchButtonIndex) {

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
        if (mHoldButton != null)
            clearHold();

        mHoldButton = mButtons[index];
        mHoldStateList = (StateListDrawable) mHoldButton.getBackground();
        mHoldButton.setBackground(mButtonIconSet.getIcon(index, true));

    }

    public void clearHold() {

        // Reset hold image
        if (mHoldButton != null) {
            mHoldButton.setBackground(mHoldStateList);
            mHoldStateList = null;
            mHoldButton = null;
        }

    }

    public interface OnButtonsFragmentButtonClicked {
        void onButtonClicked(int index);

        void onButtonLongClicked(int index);

        void onLaunchButtonClicked();
    }

}
