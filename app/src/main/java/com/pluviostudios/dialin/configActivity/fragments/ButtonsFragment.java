package com.pluviostudios.dialin.configActivity.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    public static final String EXTRA_LAUNCH_INDEX = "extra_launch_on_left";

    private LinearLayout mRoot;
    protected ButtonIconSet mButtonIconSet;
    private ImageButton[] mButtons;
    private Integer mHoldIndex;
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
        mLaunchButtonIndex = extras.getInt(EXTRA_LAUNCH_INDEX, buttonCount - 1);

        // Get the passed ButtonIconSet
        mButtonIconSet = (ButtonIconSet) extras.getSerializable(EXTRA_BUTTON_SET);

        // Generate the button view
        mRoot = new LinearLayout(getContext());
        mRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setOrientation(LinearLayout.HORIZONTAL);
        mRoot.setGravity(Gravity.CENTER);

        // Determine Button Size
        // Todo find a better way to determine button width
        int buttonSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getContext().getResources().getDisplayMetrics()));

        // Add buttons to parent view
        mButtons = new ImageButton[buttonCount];
        boolean launchPlaced = false;
        for (int i = 0; i < buttonCount; i++) {

            // For placing ImageView under ImageButton
            FrameLayout buttonFrameLayout = new FrameLayout(getContext());
            buttonFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

            // For displaying the background
            ImageView backgroundImageView = new ImageView(getContext());
            backgroundImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            buttonFrameLayout.addView(backgroundImageView);

            // Determine which buttons get launcher icons vs standard icons
            if (i == mLaunchButtonIndex && !launchPlaced) {
                launchPlaced = true;
                backgroundImageView.setImageURI(mButtonIconSet.getLauncher());
            } else {
                backgroundImageView.setImageURI(mButtonIconSet.getIcon(i - (launchPlaced ? 1 : 0)));
            }

            // When pressed, this button will display a highlight ring over the ImageView, it also houses the icons.
            // Unfortunately, this elaborate setup is the only way to allow customizable button press effects within the RemoteViews used for the AppWidget.
            // For simplicity and consistency, I used the same system here, rather than using a StateListDrawable
            ImageButton newImageButton = new ImageButton(getContext());
            newImageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            newImageButton.setBackgroundResource(mButtonIconSet.getButtonHighlightStateDrawableResourceId());
            buttonFrameLayout.addView(newImageButton);
            mButtons[i] = newImageButton;

            newImageButton.setTag(i);
            newImageButton.setOnClickListener(this);
            newImageButton.setOnLongClickListener(this);

            mRoot.addView(buttonFrameLayout);

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

            // Go through each button
            for (int i = 0; i < mButtons.length; i++) {

                ImageButton currentButton = mButtons[i];

                // If this button has an icon to display
                if (iconList[i] != null) {

                    // Display the uri
                    Uri imageUri = iconList[i].getImageUri();

                    currentButton.setImageURI(imageUri);

                } else {

                    // Otherwise set the bitmap to null
                    currentButton.setImageBitmap(null);

                }

//              Invalidate button so it is refreshed
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
        mButtons[index].setBackgroundResource(mButtonIconSet.getButtonHighlightResourceId());

    }

    public void clearHold() {

        // Reset hold image
        if (mHoldIndex != null) {
            mButtons[mHoldIndex].setBackgroundResource(mButtonIconSet.getButtonHighlightStateDrawableResourceId());
            mHoldIndex = null;
        }

    }

    public interface OnButtonsFragmentButtonClicked {
        void onButtonClicked(int index);

        void onButtonLongClicked(int index);

        void onLaunchButtonClicked();
    }

}
