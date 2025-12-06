package com.gaba.eskukap;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class ImageStorage {

    private static final String TAG = "EskukapUI";
    private static final String OUT_PATH = "/data/local/tmp/eskukap_fake.jpg";

    public static boolean saveToSharedLocation(Context ctx, Uri uri) {
        if (uri == null) return false;

        InputStream in = null;
        OutputStream out = null;
        try {
            in = ctx.getContentResolver().openInputStream(uri);
            if (in == null) return false;

            out = new FileOutputStream(OUT_PATH);
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
            out.flush();

            Log.i(TAG, "Image saved to " + OUT_PATH);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "saveToSharedLocation error", t);
            return false;
        } finally {
            try { if (in != null) in.close(); } catch (Throwable ignored) {}
            try { if (out != null) out.close(); } catch (Throwable ignored) {}
        }
    }
}
