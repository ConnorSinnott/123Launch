package com.pluviostudios.dialin.buttonsActivity.fragments;

import com.pluviostudios.dialin.action.Action;

/**
 * Created by spectre on 8/19/16.
 */
public class EditActionFragmentEvents {

    public static class Outgoing {

        public static class OnConfigured {

            public final Action action;

            public OnConfigured(Action action) {
                this.action = action;
            }

        }

        public static class OnCancel {

        }

    }

}
