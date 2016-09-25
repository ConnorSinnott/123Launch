package com.pluviostudios.onetwothreelaunch.buttonsActivity;

/**
 * Created by spectre on 9/5/16.
 */
public class OnRequestPermissionResultEvent {

    final public int requestCode;
    final public String permissions[];
    final public int[] grantResults;

    public OnRequestPermissionResultEvent(int requestCode, String[] permissions, int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
    }

}
