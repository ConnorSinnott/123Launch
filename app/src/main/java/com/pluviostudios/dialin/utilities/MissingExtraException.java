package com.pluviostudios.dialin.utilities;

/**
 * Created by spectre on 7/27/16.
 */
public class MissingExtraException extends RuntimeException {

    public MissingExtraException(String expectedExtra) {
        super("Expected extra is missing: " + expectedExtra);
    }

}