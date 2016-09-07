package com.pluviostudios.dialin.configManagerActivity.fragments;

/**
 * Created by spectre on 9/6/16.
 */
public class ConfigurationListFragmentEvents {

    public static class Outgoing {

        public static class EditEvent {

            public final long configurationId;

            public EditEvent(long configurationId) {
                this.configurationId = configurationId;
            }

        }

        public static class SelectedEvent {

            public final long configurationId;

            public SelectedEvent(long configurationId) {
                this.configurationId = configurationId;
            }

        }

        public static class NewConfiguration {

        }

    }

}
