package com.pluviostudios.dialin.action;

import android.util.Log;

/**
 * Created by spectre on 7/26/16.
 */
public abstract class Action {

    public static final String TAG = "Action";

    public String name;
    public int id;
    public DialinImage actionImage;

    public static final Action DefaultDialinAction = new Action("Default", 0) {
        @Override
        public boolean onExecute() {
            return false;
        }
    };

    public Action(String name, int id) {
        this(name, id, DialinImage.defaultActionImage);
    }

    public Action(String name, int id, DialinImage actionImage) {
        this.name = name;
        this.id = id;
        this.actionImage = actionImage;
    }

    public boolean execute() {
        try {
            return onExecute();
        } catch (Exception e) {
            Log.e(TAG, "execute: Execution Failed", e);
            return false;
        }
    }

    public abstract boolean onExecute();

}
