package com.pluviostudios.dialin.buttonsActivity.fragments;

import android.net.Uri;

/**
 * Created by spectre on 8/30/16.
 */
public class ButtonFragmentEvents {

    public static class Outgoing {

        public static class ClickEvent {

            public final int index;

            public ClickEvent(int index) {
                this.index = index;
            }
        }

        public static class LongClickEvent {

            public final int index;

            public LongClickEvent(int index) {
                this.index = index;
            }

        }

        public static class LaunchClickEvent {

        }

    }

    public static class Incoming {

        public static class ButtonsFragmentClearHoldEvent {
        }

        public static class ButtonsFragmentUpdateEvent {

            public final Uri launcherImage;
            public final Uri[] childImages;

            public ButtonsFragmentUpdateEvent(Uri launcherImage, Uri[] childImages) {
                this.launcherImage = launcherImage;
                this.childImages = childImages;
            }

        }

    }

}
