package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "Eskukap";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        XposedBridge.log(TAG + ": Loaded " + lpparam.packageName);

        try {
            Class<?> imageReaderClass = XposedHelpers.findClass(
                    "android.media.ImageReader",
                    lpparam.classLoader
            );

            XposedBridge.log(TAG + ": ImageReader hook OK");

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

        } catch (Throwable t) {
            XposedBridge.log(TAG + ": ImageReader hook FAIL: " + t);
        }
    }

    private void handleImage(Image img) {
        int format = img.getFormat();
        XposedBridge.log(TAG + ": Frame format=" + format +
                " size=" + img.getWidth() + "x" + img.getHeight());

        if (format == ImageFormat.YUV_420_888) {
            replaceYuv(img);
        } else if (format == ImageFormat.JPEG) {
            replaceJpeg(img);
        } else {
            XposedBridge.log(TAG + ": Unsupported format, skip");
        }
    }

    // ---------- подмена JPEG кадра ----------
    private void replaceJpeg(Image img) {
        File f = new File("/sdcard/Pictures/Eskukap/fake.jpg");
        if (!f.exists()) {
            XposedBridge.log(TAG + ": fake.jpg not found (JPEG)");
            return;
        }

        byte[] jpeg = readFile(f);
        if (jpeg == null || jpeg.length == 0) {
            XposedBridge.log(TAG + ": fake.jpg read error");
            return;
        }

        Image.Plane[] planes = img.getPlanes();
        if (planes == null || planes.length == 0) return;

        ByteBuffer buf = planes[0].getBuffer();
        buf.rewind();

        int len = Math.min(buf.remaining(), jpeg.length);
        buf.put(jpeg, 0, len);

        XposedBridge.log(TAG + ": JPEG frame replaced, bytes=" + len);
    }

    // ---------- подмена YUV кадра (как раньше) ----------
    private void replaceYuv(Image img) {
        if (img.getFormat() != ImageFormat.YUV_420_888) return;

        File f = new File("/sdcard/Pictures/Eskukap/fake.jpg");
        if (!f.exists()) {
            XposedBridge.log(TAG + ": fake.jpg not found (YUV)");
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        if (bmp == null) {
            XposedBridge.log(TAG + ": bitmap decode fail");
            return;
        }

        int w = img.getWidth(), h = img.getHeight();
        if (bmp.getWidth() != w || bmp.getHeight() != h) {
            bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
        }

        byte[] nv21 = bitmapToNV21(bmp, w, h);
        if (nv21 == null) {
            XposedBridge.log(TAG + ": bitmapToNV21 fail");
            return;
        }

        writeNV21(img, nv21, w, h);
        XposedBridge.log(TAG + ": YUV frame replaced");
    }

    // ---------- helpers ----------
    private byte[] readFile(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = fis.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            fis.close();
            return baos.toByteArray();
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": readFile error " + t);
            return null;
        }
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

                if ((j % 2 == 0) && (i % 2 == 0)) {
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
        ByteBuffer Y = planes[0].getBuffer();
        int rowStrideY = planes[0].getRowStride();
        for (int row = 0; row < h; row++) {
            Y.position(row * rowStrideY);
            Y.put(nv21, row * w, w);
        }

        // UV
        ByteBuffer U = planes[1].getBuffer();
        ByteBuffer V = planes[2].getBuffer();
        int rowStrideU = planes[1].getRowStride();
        int pixelStrideU = planes[1].getPixelStride();
        int rowStrideV = planes[2].getRowStride();
        int pixelStrideV = planes[2].getPixelStride();

        int uvOffset = w * h;

        for (int row = 0; row < h / 2; row++) {
            for (int col = 0; col < w / 2; col++) {
                int idx = uvOffset + row * w + col * 2;
                byte Vb = nv21[idx];
                byte Ub = nv21[idx + 1];

                int posU = row * rowStrideU + col * pixelStrideU;
                int posV = row * rowStrideV + col * pixelStrideV;

                if (posU < U.capacity()) U.put(posU, Ub);
                if (posV < V.capacity()) V.put(posV, Vb);
            }
        }
    }
}
