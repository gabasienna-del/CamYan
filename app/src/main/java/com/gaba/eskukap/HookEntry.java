package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        // работаем только с Яндекс Таксометром
        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;
        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // ---------- ImageReader.acquireLatestImage ----------
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

                        // ===== если формат YUV_420_888 — делаем resize до 1280x720 =====
                        if (fmt == ImageFormat.YUV_420_888) {
                            byte[] nv21 = yuvToNV21(image);
                            if (nv21 != null) {
                                byte[] jpeg = resizeNV21To1280x720(nv21, w, h);
                                if (jpeg != null) {
                                    XposedBridge.log("Eskukap: YUV scaled to 1280x720, size=" + jpeg.length);
                                } else {
                                    XposedBridge.log("Eskukap: resizeNV21To1280x720 failed");
                                }
                            } else {
                                XposedBridge.log("Eskukap: yuvToNV21 failed");
                            }
                        }

                        // ===== PRIVATE (256) — просто логируем, размер уже 1280x720 =====
                        if (fmt == 256) {
                            XposedBridge.log("Eskukap: PRIVATE frame, keep as is (already " + w + "x" + h + ")");
                        }
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("Eskukap IMAGE HOOK ERR: " + e);
        }
    }

    // ------------------ YUV_420_888 -> NV21 ------------------
    private static byte[] yuvToNV21(Image img) {
        try {
            int w = img.getWidth();
            int h = img.getHeight();
            Image.Plane[] planes = img.getPlanes();

            ByteBuffer Y = planes[0].getBuffer();
            ByteBuffer U = planes[1].getBuffer();
            ByteBuffer V = planes[2].getBuffer();

            int yRow = planes[0].getRowStride();
            int uRow = planes[1].getRowStride();
            int vRow = planes[2].getRowStride();

            byte[] out = new byte[w * h * 3 / 2];
            int pos = 0;

            // копируем Y
            for (int i = 0; i < h; i++) {
                Y.position(i * yRow);
                Y.get(out, pos, w);
                pos += w;
            }

            // UV (VU для NV21)
            for (int i = 0; i < h / 2; i++) {
                for (int j = 0; j < w / 2; j++) {
                    U.position(i * uRow + j * 2);
                    V.position(i * vRow + j * 2);
                    out[pos++] = V.get();
                    out[pos++] = U.get();
                }
            }

            return out;
        } catch (Exception e) {
            XposedBridge.log("Eskukap: yuvToNV21 ERR " + e);
            return null;
        }
    }

    // ------------------ NV21 -> JPEG -> resize 1280x720 ------------------
    private static byte[] resizeNV21To1280x720(byte[] nv21, int srcW, int srcH) {
        try {
            android.graphics.YuvImage yuv =
                    new android.graphics.YuvImage(nv21, ImageFormat.NV21, srcW, srcH, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, srcW, srcH), 90, os);
            byte[] jpeg = os.toByteArray();

            Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
            if (bmp == null) return null;

            Bitmap scaled = Bitmap.createScaledBitmap(bmp, 1280, 720, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return out.toByteArray();
        } catch (Exception e) {
            XposedBridge.log("Eskukap: resizeNV21To1280x720 ERR " + e);
            return null;
        }
    }
}
