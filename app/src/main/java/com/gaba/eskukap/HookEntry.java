package com.gaba.eskukap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;

import java.io.InputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.AndroidAppHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;
        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Image image = (Image) param.getResult();
                            if (image == null) return;

                            int fmt = image.getFormat();
                            int w   = image.getWidth();
                            int h   = image.getHeight();

                            XposedBridge.log("Eskukap: Frame " + w + "x" + h + " fmt=" + fmt);

                            // --- PRIVATE пока не трогаем, только лог ---
                            if (fmt == 256) {
                                XposedBridge.log("Eskukap: PRIVATE frame, keep as is (already " + w + "x" + h + ")");
                                return;
                            }

                            if (fmt != ImageFormat.YUV_420_888) {
                                XposedBridge.log("Eskukap: Unsupported format for replace: " + fmt);
                                return;
                            }

                            try {
                                Context ctx = AndroidAppHelper.currentApplication();
                                if (ctx == null) {
                                    XposedBridge.log("Eskukap: no app context");
                                    return;
                                }

                                SharedPreferences sp = ctx.getSharedPreferences("eskukap", Context.MODE_PRIVATE);
                                String uriStr = sp.getString("img", null);
                                if (uriStr == null) {
                                    XposedBridge.log("Eskukap: no jpeg selected in settings");
                                    return;
                                }

                                Uri uri = Uri.parse(uriStr);
                                Bitmap src = loadBitmapFromUri(ctx, uri);
                                if (src == null) {
                                    XposedBridge.log("Eskukap: cannot load bitmap from uri");
                                    return;
                                }

                                Bitmap scaled = Bitmap.createScaledBitmap(src, w, h, true);
                                replaceImageWithBitmap(image, scaled);
                                XposedBridge.log("Eskukap: frame replaced from JPEG");
                            } catch (Throwable e) {
                                XposedBridge.log("Eskukap REPLACE ERR: " + e);
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGE HOOK ERR: " + e);
        }
    }

    // -------- загрузка JPEG по Uri из SettingsActivity --------
    private static Bitmap loadBitmapFromUri(Context ctx, Uri uri) {
        InputStream is = null;
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            return BitmapFactory.decodeStream(is);
        } catch (Throwable e) {
            XposedBridge.log("Eskukap loadBitmap ERR: " + e);
            return null;
        } finally {
            try { if (is != null) is.close(); } catch (Throwable ignored) {}
        }
    }

    // -------- подмена содержимого Image (YUV_420_888) из Bitmap --------
    private static void replaceImageWithBitmap(Image image, Bitmap bmp) {
        if (image.getFormat() != ImageFormat.YUV_420_888) return;

        int width  = image.getWidth();
        int height = image.getHeight();

        if (bmp.getWidth() != width || bmp.getHeight() != height) {
            bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        int[] argb = new int[width * height];
        bmp.getPixels(argb, 0, width, 0, 0, width, height);

        // YUV 4:2:0 planar
        byte[] y = new byte[width * height];
        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];

        int index = 0;
        int uvIndex;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int color = argb[index++];
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int U = (int) (-0.169 * r - 0.331 * g + 0.5 * b + 128);
                int V = (int) (0.5 * r - 0.419 * g - 0.081 * b + 128);

                if (Y < 0) Y = 0; if (Y > 255) Y = 255;
                if (U < 0) U = 0; if (U > 255) U = 255;
                if (V < 0) V = 0; if (V > 255) V = 255;

                y[j * width + i] = (byte) Y;

                // 4:2:0 subsampling: каждый 2x2 блок делит один U/V
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    uvIndex = (j / 2) * (width / 2) + (i / 2);
                    u[uvIndex] = (byte) U;
                    v[uvIndex] = (byte) V;
                }
            }
        }

        Image.Plane[] planes = image.getPlanes();

        // ----- пишем Y -----
        ByteBuffer yBuf = planes[0].getBuffer();
        int yRowStride = planes[0].getRowStride();
        int yPixelStride = planes[0].getPixelStride(); // обычно 1

        yBuf.position(0);
        for (int row = 0; row < height; row++) {
            int rowOffset = row * width;
            for (int col = 0; col < width; col++) {
                int bufferIndex = row * yRowStride + col * yPixelStride;
                yBuf.position(bufferIndex);
                yBuf.put(y[rowOffset + col]);
            }
        }

        // ----- пишем U -----
        ByteBuffer uBuf = planes[1].getBuffer();
        int uRowStride = planes[1].getRowStride();
        int uPixelStride = planes[1].getPixelStride();

        uBuf.position(0);
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int bufferIndex = row * uRowStride + col * uPixelStride;
                int srcIndex = row * (width / 2) + col;
                uBuf.position(bufferIndex);
                uBuf.put(u[srcIndex]);
            }
        }

        // ----- пишем V -----
        ByteBuffer vBuf = planes[2].getBuffer();
        int vRowStride = planes[2].getRowStride();
        int vPixelStride = planes[2].getPixelStride();

        vBuf.position(0);
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int bufferIndex = row * vRowStride + col * vPixelStride;
                int srcIndex = row * (width / 2) + col;
                vBuf.position(bufferIndex);
                vBuf.put(v[srcIndex]);
            }
        }
    }
}
