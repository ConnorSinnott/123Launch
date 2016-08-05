package com.pluviostudios.dialin.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by spectre on 7/26/16.
 */
public class FileManager {

    public static final String TAG = "FileManager";
    public static final String FILE_PREFIX = "OneTwoThree_";

    public static void writeToFile(Context context, String filename, String data) throws IOException {

        filename = FILE_PREFIX + filename;
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
        outputStreamWriter.write(data);
        outputStreamWriter.close();

    }

    public static String readFromFile(Context context, String filename) throws IOException {

        filename = FILE_PREFIX + filename;

        String ret = "";

        InputStream inputStream = context.openFileInput(filename);

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

        return ret;
    }


}
