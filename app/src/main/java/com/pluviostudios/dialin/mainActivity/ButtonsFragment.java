package com.pluviostudios.dialin.mainActivity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
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
import com.pluviostudios.dialin.utilities.Utilities;

/**
 * Created by spectre on 7/26/16.
 */
public class ButtonsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "ButtonsFragment";
    private static final String EXTRA_COUNT = "extra_count";
    private static final String EXTRA_DEFAULT_IMAGE_LIST_URI = "extra_default_image_list_uri";
    private static final String EXTRA_HOLD_IMAGE_LIST_URI = "extra_hold_image_list_uri";

    public static final String EXTRA_LAUNCH_ON_LEFT = "extra_launch_on_right";

    private LinearLayout mRoot;
    private ImageButton[] mButtons;
    private ImageButton mHoldButton;
    private int mLaunchButtonIndex;

    DialinImage[] pendingIconList;
    private boolean iconsPending = false;

    protected BitmapDrawable[] defaultImageList; // How the buttons will look when not clicked. From left to right
    protected BitmapDrawable[] clickedImageList; // How the buttons will look when clicked or held. From left to right
    protected StateListDrawable[] mButtonStates; // The generated states of the button using the above images

    protected OnButtonsFragmentButtonClicked mOnButtonsFragmentButtonClicked;

    public static ButtonsFragment buildButtonsFragment(int count, DialinImage[] defaultImageList, DialinImage[] clickedImageList) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_COUNT, count);
        extras.putStringArray(EXTRA_DEFAULT_IMAGE_LIST_URI, convertDialinImagesToStringArray(defaultImageList));
        extras.putStringArray(EXTRA_HOLD_IMAGE_LIST_URI, convertDialinImagesToStringArray(clickedImageList));
        fragment.setArguments(extras);
        return fragment;
    }

    private static String[] convertDialinImagesToStringArray(DialinImage[] dialinImages) {
        String[] out = new String[dialinImages.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = dialinImages[i].imageUri.toString();
        }
        return out;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // To allow for an unlimited amount of buttons, this view must be made dynamically.

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();
        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_COUNT,
                EXTRA_DEFAULT_IMAGE_LIST_URI,
                EXTRA_HOLD_IMAGE_LIST_URI);

        // Set button count and determine launch index
        int buttonCount = extras.getInt(EXTRA_COUNT);

        // Add support for launch on left
        if (extras.containsKey(EXTRA_LAUNCH_ON_LEFT) && extras.getBoolean(EXTRA_LAUNCH_ON_LEFT)) {
            mLaunchButtonIndex = 0;
        } else {
            mLaunchButtonIndex = buttonCount - 1;
        }

        // Convert passed URIs to BitmapDrawables
        String[] defaultImageStringArray = extras.getStringArray(EXTRA_DEFAULT_IMAGE_LIST_URI);
        String[] holdImageStringArray = extras.getStringArray(EXTRA_HOLD_IMAGE_LIST_URI);
        defaultImageList = Utilities.generateBitmapDrawableArrayFromStringURI(getContext(), defaultImageStringArray);
        clickedImageList = Utilities.generateBitmapDrawableArrayFromStringURI(getContext(), holdImageStringArray);

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

                // Apply new image to button
                currentButton.setImageURI(imageUri);

                // Invalidate button
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
