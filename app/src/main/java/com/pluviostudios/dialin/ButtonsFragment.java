package com.pluviostudios.dialin;

import android.graphics.drawable.Drawable;
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
import com.pluviostudios.dialin.utilities.MissingExtraException;

/**
 * Created by spectre on 7/26/16.
 */
public class ButtonsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "ButtonsFragment";
    public static final String EXTRA_COUNT = "extra_count";

    private LinearLayout mRoot;
    private ImageButton[] mButtons;
    private ImageButton mHoldButton;
    private int mLaunchButtonIndex;

    protected Drawable[] defaultImageList; // How the buttons will look when not clicked. From left to right
    protected Drawable[] clickedImageList; // How the buttons will look when clicked or held. From left to right
    protected StateListDrawable[] mButtonStates; // The generated states of the button using the above images

    protected OnButtonsFragmentButtonClicked mOnButtonsFragmentButtonClicked;

    //Todo Setup Default Image List
    //Todo Should defaults be setup here? or should drawable files always be specified?
    public static ButtonsFragment buildButtonsFragment(int count, OnButtonsFragmentButtonClicked onButtonsFragmentButtonClicked) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_COUNT, count);
        fragment.setArguments(extras);
        fragment.mOnButtonsFragmentButtonClicked = onButtonsFragmentButtonClicked;
        return fragment;
    }

    public static ButtonsFragment buildButtonsFragment(int count, OnButtonsFragmentButtonClicked onButtonsFragmentButtonClicked, Drawable[] defaultImageList, Drawable[] clickedImageList) {
        ButtonsFragment fragment = buildButtonsFragment(count, onButtonsFragmentButtonClicked);
        fragment.defaultImageList = defaultImageList;
        fragment.clickedImageList = clickedImageList;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // To allow for an unlimited amount of buttons, this view must be made dynamically.

        // If button count is not specified, throw exception
        Bundle extras = getArguments();
        if (!extras.containsKey(EXTRA_COUNT))
            throw new MissingExtraException(EXTRA_COUNT);

        int buttonCount = extras.getInt(EXTRA_COUNT);
        mLaunchButtonIndex = buttonCount - 1;

        // Create parent view
        mRoot = new LinearLayout(getContext());
        mRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setOrientation(LinearLayout.HORIZONTAL);
        mRoot.setGravity(Gravity.CENTER);

        // Determine Button Size
        // Todo find a better way to determine button width
        int buttonSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getContext().getResources().getDisplayMetrics()));

        // Add buttons to parent view
        mButtons = new ImageButton[buttonCount];
        mButtonStates = new StateListDrawable[buttonCount];
        for (int i = 0; i < buttonCount; i++) {

            ImageButton newImageButton = new ImageButton(getContext());
            newImageButton.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

            // Set tag to identify button later
            newImageButton.setTag(i);

            // Add listeners
            newImageButton.setOnClickListener(this);
            newImageButton.setOnLongClickListener(this);

            // Generate button states
            mButtonStates[i] = new StateListDrawable();
            mButtonStates[i].addState(new int[]{android.R.attr.state_pressed}, clickedImageList[i]); // Clicked
            mButtonStates[i].addState(new int[]{android.R.attr.state_enabled}, defaultImageList[i]); // Released

            // Set button background to default image
            newImageButton.setBackground(mButtonStates[i]);

            mButtons[i] = newImageButton;
            mRoot.addView(newImageButton);

        }

        return mRoot;

    }

    public void setIcons(DialinImage[] iconList) {

        if (iconList.length != mButtons.length)
            throw new RuntimeException("Passed iconList length does not match button count");

        for (int i = 0; i < mButtons.length; i++) {

            ImageButton currentButton = mButtons[i];
            Uri imageUri = iconList[i].imageUri;

            // Apply new image to button
            currentButton.setImageURI(imageUri);

            // Invalidate button
            currentButton.requestLayout();

        }

    }

    @Override
    public void onClick(View view) {

        // Which button was clicked
        int buttonIndex = (int) view.getTag();

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
        int buttonIndex = (int) view.getTag();

        if (mOnButtonsFragmentButtonClicked != null) {

            // Alert the listener
            if (buttonIndex != mLaunchButtonIndex) {

                // Launch button should never be held

                // Todo setup hold feature

                mOnButtonsFragmentButtonClicked.onButtonLongClicked(buttonIndex);

                // Set hold image
                setHoldOnButton(buttonIndex);

            }
        }

        return true; // Do not propagate up

    }

    public void setHoldOnButton(int index) {

        // Set hold image
        if (mHoldButton != null)
            clearHold();

        mHoldButton = mButtons[index];
        mHoldButton.setBackground(clickedImageList[index]);

    }

    public void clearHold() {

        // Reset hold image
        if (mHoldButton != null) {
            int holdIndex = (int) mHoldButton.getTag();
            mHoldButton.setBackground(mButtonStates[holdIndex]);
        }

    }

    public interface OnButtonsFragmentButtonClicked {
        void onButtonClicked(int index);

        void onButtonLongClicked(int index);

        void onLaunchButtonClicked();
    }

}
