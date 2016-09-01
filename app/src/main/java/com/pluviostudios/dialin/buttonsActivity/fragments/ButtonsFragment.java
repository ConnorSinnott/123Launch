package com.pluviostudios.dialin.buttonsActivity.fragments;

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

import com.pluviostudios.dialin.buttonIconSet.ButtonIconSet;
import com.pluviostudios.dialin.buttonIconSet.ButtonIconSetManager;
import com.pluviostudios.dialin.utilities.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by spectre on 7/26/16.
 */
public class ButtonsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "ButtonsFragment";

    public static final String EXTRA_COUNT = "extra_count";
    public static final String EXTRA_LAUNCH_BUTTON_INDEX = "extra_launch_button_index";
    public static final String EXTRA_LAUNCH_ICON_URI = "extra_launch_icon_uri";
    public static final String EXTRA_CHILD_ICON_URIS = "extra_child_icon_uris";

    public static final String SAVED_HOLD_POSITION = "saved_hold_position";
    public static final String SAVED_ICONS_EXIST = "saved_icons_exit";

    private LinearLayout mRoot;
    private ImageButton mLauncherButton;
    private ImageButton[] mChildrenButtons;

    private ButtonIconSet mButtonIconSet;
    private Uri mLauncherIcon;
    private Uri[] mChildrenIcons;

    private Integer mHoldIndex;

    public static ButtonsFragment buildButtonsFragment(int count) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_COUNT, count);
        fragment.setArguments(extras);
        return fragment;
    }

    public static ButtonsFragment buildButtonsFragment(int count, Uri launcherIcon, Uri[] childrenIcons) {
        ButtonsFragment buttonsFragment = buildButtonsFragment(count);
        Bundle extras = buttonsFragment.getArguments();

        if (launcherIcon != null) {
            extras.putParcelable(EXTRA_LAUNCH_ICON_URI, launcherIcon);
        }

        if (childrenIcons != null) {
            extras.putParcelableArray(EXTRA_CHILD_ICON_URIS, childrenIcons);
        }

        return buttonsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Throw exceptions if we are missing expected extras
        Bundle extras = getArguments();

        Utilities.checkBundleForExpectedExtras(extras,
                EXTRA_COUNT
        );

        // Set button count and determine launch index
        int buttonCount = extras.getInt(EXTRA_COUNT);

        // Add support for launch on left
        int launchButtonIndex = extras.getInt(EXTRA_LAUNCH_BUTTON_INDEX, buttonCount - 1);

        // Get the passed ButtonIconSet
        mButtonIconSet = ButtonIconSetManager.getButtonIconSet(getContext(), buttonCount);

        // Generate the button view
        mRoot = new LinearLayout(getContext());
        mRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.setOrientation(LinearLayout.HORIZONTAL);
        mRoot.setGravity(Gravity.CENTER);

        mChildrenButtons = new ImageButton[buttonCount - 1];
        boolean launchPlaced = false;
        for (int i = 0; i < buttonCount; i++) {

            FrameLayout buttonFrameLayout;
            int relativeChildIndex = i - (launchPlaced ? 1 : 0);

            ImageButton newImageButton;

            // Determine which buttons get launcher icons vs standard icons
            if (i == launchButtonIndex) {

                launchPlaced = true;
                buttonFrameLayout = generateLaunchButton(getContext(), mButtonIconSet);

                newImageButton = (ImageButton) buttonFrameLayout.findViewWithTag("ImageButton");
                newImageButton.setId(i);
                newImageButton.setTag(-1);

                mLauncherButton = newImageButton;

            } else {

                buttonFrameLayout = generateChildButton(getContext(), mButtonIconSet, relativeChildIndex);

                newImageButton = (ImageButton) buttonFrameLayout.findViewWithTag("ImageButton");
                newImageButton.setTag(relativeChildIndex);
                newImageButton.setId(i);

                mChildrenButtons[relativeChildIndex] = newImageButton;

            }

            newImageButton.setOnClickListener(this);
            newImageButton.setOnLongClickListener(this);

            mRoot.addView(buttonFrameLayout);

        }

        // If savedInstanceState is not null, override the icons saved in init bundle with saved instance state
        if (savedInstanceState != null && savedInstanceState.getBoolean(SAVED_ICONS_EXIST)) {

            if (savedInstanceState.containsKey(EXTRA_LAUNCH_ICON_URI)) {
                mLauncherIcon = savedInstanceState.getParcelable(EXTRA_LAUNCH_ICON_URI);
            }

            if (savedInstanceState.containsKey(EXTRA_CHILD_ICON_URIS)) {
                mChildrenIcons = (Uri[]) savedInstanceState.getParcelableArray(EXTRA_CHILD_ICON_URIS);
            }

        } else {

            if (extras.containsKey(EXTRA_LAUNCH_ICON_URI)) {
                mLauncherIcon = extras.getParcelable(EXTRA_LAUNCH_ICON_URI);
            }

            if (extras.containsKey(EXTRA_CHILD_ICON_URIS)) {
                mChildrenIcons = (Uri[]) extras.getParcelableArray(EXTRA_CHILD_ICON_URIS);
            }

        }

        updateIcons();

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_HOLD_POSITION)) {
            setHoldOnButton(savedInstanceState.getInt(SAVED_HOLD_POSITION));
        }

        return mRoot;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mHoldIndex != null) {
            outState.putInt(SAVED_HOLD_POSITION, mHoldIndex);
        }

        outState.putBoolean(SAVED_ICONS_EXIST, mLauncherIcon != null || mChildrenIcons != null);

        if (mLauncherIcon != null) {
            outState.putParcelable(EXTRA_LAUNCH_ICON_URI, mLauncherIcon);
        }

        if (mChildrenIcons != null) {
            outState.putParcelableArray(EXTRA_CHILD_ICON_URIS, mChildrenIcons);
        }

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private static FrameLayout generateButtonFrameLayout(Context context, int buttonHighlightResourceId) {

        // Determine Button Size
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
        backgroundImageView.setImageURI(buttonIconSet.getLauncherUri());
        return generatedButton;
    }

    private void updateIcons() {

        if (mLauncherIcon != null) {
            mLauncherButton.setImageURI(mLauncherIcon);
        } else {
            mLauncherButton.setImageBitmap(null);
        }

        if (mChildrenIcons != null) {
            for (int i = 0; i < mChildrenIcons.length; i++) {
                if (mChildrenIcons[i] != null) {
                    mChildrenButtons[i].setImageURI(mChildrenIcons[i]);
                } else {
                    mChildrenButtons[i].setImageBitmap(null);
                }
            }
        }

    }

    public void setHoldOnButton(int index) {

        // Set hold image
        if (mHoldIndex != null) {
            clearHold();
        }

        mHoldIndex = index;
        mChildrenButtons[index].setBackgroundResource(mButtonIconSet.getButtonHighlightResourceId());

    }

    private void clearHold() {
        // Reset hold image
        if (mHoldIndex != null) {
            mChildrenButtons[mHoldIndex].setBackgroundResource(mButtonIconSet.getButtonHighlightStateDrawableResourceId());
            mHoldIndex = null;
        }
    }

    @Subscribe
    public void onButtonsFragmentUpdateEvent(ButtonFragmentEvents.Incoming.ButtonsFragmentUpdateEvent event) {

        mLauncherIcon = event.launcherImage;
        mChildrenIcons = event.childImages;
        updateIcons();

    }

    @Subscribe
    public void onButtonsFragmentClearHoldEvent(ButtonFragmentEvents.Incoming.ButtonsFragmentClearHoldEvent event) {
        clearHold();
    }

    @Override
    public void onClick(View view) {

        // Which button was clicked. -1 if Launch
        int buttonIndex = (int) view.getTag();

        if (buttonIndex >= 0) {

            // Standard button was clicked
            EventBus.getDefault().post(new ButtonFragmentEvents.Outgoing.ClickEvent(buttonIndex));

        } else {

            // Launch button was clicked
            EventBus.getDefault().post(new ButtonFragmentEvents.Outgoing.LaunchClickEvent());

        }

        clearHold();


    }

    @Override
    public boolean onLongClick(View view) {

        // Which button was clicked
        int buttonIndex = (int) view.getTag();


        // Alert the listener
        if (buttonIndex >= 0) {

            // Standard button was long clicked
            EventBus.getDefault().post(new ButtonFragmentEvents.Outgoing.LongClickEvent(buttonIndex));

            // Set hold image
            setHoldOnButton(buttonIndex);

        } else {

            // Consider this a launch button click
            EventBus.getDefault().post(new ButtonFragmentEvents.Outgoing.LaunchClickEvent());

        }

        return true; // Do not propagate up

    }

}
