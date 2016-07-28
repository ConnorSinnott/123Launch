package com.pluviostudios.dialin.data;

import android.content.Context;
import android.util.Log;

import com.pluviostudios.dialin.utilities.ContextHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by spectre on 7/26/16.
 */
public class FileManager {

    public static final String TAG = "FileManager";

    public static void writeToFile(String filename, String data) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ContextHelper.getContext().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

    public static String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = ContextHelper.getContext().openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }


}
