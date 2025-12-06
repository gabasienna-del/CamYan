package com.gaba.eskukap;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

public class FileHelper {

    private static final String TAG = "EskukapFile";

    public static byte[] readFile(String path) {

        String[] tryPaths = new String[]{
                path,
                "/storage/emulated/0/eskukap/frame.jpg",
                "/sdcard/eskukap/frame.jpg",
                Environment.getExternalStorageDirectory().getPath() + "/eskukap/frame.jpg"
        };

        for (String p : tryPaths) {
            try {
                File f = new File(p);
                if (f.exists()) {
                    FileInputStream fis = new FileInputStream(f);
                    byte[] data = new byte[(int) f.length()];
                    fis.read(data);
                    fis.close();
                    Log.i(TAG, "Loaded: " + p + " size=" + data.length);
                    return data;
                } else {
                    Log.w(TAG, "Try no file: " + p);
                }
            } catch (Throwable e) {
                Log.e(TAG, "readFile err: " + p + "  " + e);
            }
        }
        return null;
    }
}
