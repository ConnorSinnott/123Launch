package com.pluviostudios.dialin.configManagerActivity.fragments;

/**
 * Created by spectre on 8/19/16.
 */
public class ConfigurationListFragmentEvent {

    public static final String TAG = "ConfigurationListFragmentEvent";

    public static final int TYPE_CONFIGURATION_EDIT = 0;
    public static final int TYPE_CONFIGURATION_SELECTED = 1;
    public static final int TYPE_NEW_CONFIGURATION = 2;

    public final int type;
    public final long configurationId;

    public ConfigurationListFragmentEvent(int type, long configurationId) {
        this.type = type;
        this.configurationId = configurationId;
    }

}
