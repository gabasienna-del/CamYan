package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;

import java.io.File;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "Eskukap";
    private static final String FAKE_PATH = "/data/local/tmp/eskukap_fake.jpg";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        XposedBridge.log(TAG + ": Loaded " + lpparam.packageName);

        try {
            Class<?> imageReaderClass = XposedHelpers.findClass(
                    "android.media.ImageReader",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    imageReaderClass,
                    "acquireLatestImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image img = (Image) param.getResult();
                            if (img != null) handleImage(img);
                        }
                    });

            XposedHelpers.findAndHookMethod(
                    imageReaderClass,
                    "acquireNextImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image img = (Image) param.getResult();
                            if (img != null) handleImage(img);
                        }
                    });

            XposedBridge.log(TAG + ": ImageReader hook OK");

        } catch (Throwable t) {
            XposedBridge.log(TAG + ": ImageReader hook FAIL: " + t);
        }
    }

    private void handleImage(Image img) {
        int format = img.getFormat();
        int w = img.getWidth();
        int h = img.getHeight();

        XposedBridge.log(TAG + ": Frame format=" + format + " size=" + w + "x" + h);

        if (format != ImageFormat.YUV_420_888) {
            XposedBridge.log(TAG + ": Skip, not YUV_420_888");
            return;
        }

        File f = new File(FAKE_PATH);
        if (!f.exists()) {
            XposedBridge.log(TAG + ": fake file not found: " + FAKE_PATH);
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(FAKE_PATH);
        if (bmp == null) {
            XposedBridge.log(TAG + ": decode fake failed");
            return;
        }

        if (bmp.getWidth() != w || bmp.getHeight() != h) {
            bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
        }

        byte[] nv21 = bitmapToNV21(bmp, w, h);
        if (nv21 == null) {
            XposedBridge.log(TAG + ": bitmapToNV21 failed");
            return;
        }

        writeNV21(img, nv21, w, h);
        XposedBridge.log(TAG + ": YUV frame replaced");
    }

    private byte[] bitmapToNV21(Bitmap bmp, int w, int h) {
        int[] argb = new int[w * h];
        bmp.getPixels(argb, 0, w, 0, 0, w, h);

        byte[] yuv = new byte[w * h * 3 / 2];
        int frameSize = w * h;
        int yIndex = 0;
        int uvIndex = frameSize;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int c = argb[j * w + i];
                int R = (c >> 16) & 0xff;
                int G = (c >> 8) & 0xff;
                int B = c & 0xff;

                int Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                int U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                int V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                Y = clamp(Y);
                U = clamp(U);
                V = clamp(V);

                yuv[yIndex++] = (byte) Y;

                if (j % 2 == 0 && i % 2 == 0) {
                    yuv[uvIndex++] = (byte) V;
                    yuv[uvIndex++] = (byte) U;
                }
            }
        }
        return yuv;
    }

    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    private void writeNV21(Image img, byte[] nv21, int w, int h) {
        Image.Plane[] planes = img.getPlanes();

        // Y
        ByteBuffer yBuf = planes[0].getBuffer();
        int yRowStride = planes[0].getRowStride();
        for (int row = 0; row < h; row++) {
            yBuf.position(row * yRowStride);
            yBuf.put(nv21, row * w, w);
        }

        // UV
        ByteBuffer uBuf = planes[1].getBuffer();
        ByteBuffer vBuf = planes[2].getBuffer();
        int uRowStride = planes[1].getRowStride();
        int vRowStride = planes[2].getRowStride();
        int uPixelStride = planes[1].getPixelStride();
        int vPixelStride = planes[2].getPixelStride();

        int uvStart = w * h;

        for (int row = 0; row < h / 2; row++) {
            for (int col = 0; col < w / 2; col++) {
                int idx = uvStart + row * w + col * 2;
                byte V = nv21[idx];
                byte U = nv21[idx + 1];

                int uPos = row * uRowStride + col * uPixelStride;
                int vPos = row * vRowStride + col * vPixelStride;

                if (uPos < uBuf.capacity()) uBuf.put(uPos, U);
                if (vPos < vBuf.capacity()) vBuf.put(vPos, V);
            }
        }
    }
}
