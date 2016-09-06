package com.pluviostudios.dialin.buttonsActivity;

/**
 * Created by spectre on 9/5/16.
 */
public class OnRequestPermissionResultEvent {

    public int requestCode;
    public String permissions[];
    public int[] grantResults;

    public OnRequestPermissionResultEvent(int requestCode, String[] permissions, int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
    }

}
