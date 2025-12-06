package com.gaba.eskukap;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileHelper {

    private static final String TAG = "FileHelper";

    // Простое чтение файла в byte[]
    public static byte[] readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "readFile: file not exists: " + path);
            return null;
        }

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] buf = new byte[4096];
            int r;
            while ((r = fis.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "readFile: error reading " + path, e);
            return null;
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ignored) {
            }
            try {
                if (bos != null) bos.close();
            } catch (IOException ignored) {
            }
        }
    }
}
