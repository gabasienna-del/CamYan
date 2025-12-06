package com.gaba.eskukap;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final int TARGET_W = 1280;
    private static final int TARGET_H = 720;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // Лог открытия камеры (Camera2)
        XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
        );

        // ---- CameraX: Analyzer.analyze(ImageProxy) → YUV resize до 1280x720 (чистый YUV, без JPEG/Bitmap) ----
        try {
            Class<?> analyzerClass = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader
            );
            Class<?> imageProxyClass = XposedHelpers.findClass(
                    "androidx.camera.core.ImageProxy",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    analyzerClass,
                    "analyze",
                    imageProxyClass,
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                ImageProxy image = (ImageProxy) param.args[0];
                                if (image == null) return;

                                int srcW = image.getWidth();
                                int srcH = image.getHeight();

                                ImageProxy.PlaneProxy[] planes = image.getPlanes();
                                if (planes == null || planes.length < 3) {
                                    XposedBridge.log("EskukapHook: Not enough planes");
                                    return;
                                }

                                // YUV_420_888 → NV21
                                byte[] nv21 = yuv420ImageProxyToNv21(image);
                                if (nv21 == null) {
                                    XposedBridge.log("EskukapHook: yuv420ImageProxyToNv21 failed");
                                    return;
                                }

                                // NV21 → resize до 1280x720 (nearest-neighbor, максимально быстрый)
                                byte[] nv21Resized = resizeNv21(
                                        nv21,
                                        srcW,
                                        srcH,
                                        TARGET_W,
                                        TARGET_H
                                );

                                XposedBridge.log(
                                        "EskukapHook: NV21 resized " +
                                                srcW + "x" + srcH + " -> " +
                                                TARGET_W + "x" + TARGET_H +
                                                ", bytes=" + nv21Resized.length
                                );

                                // Здесь можно:
                                // 1) сохранить nv21Resized в файл
                                // 2) скормить в свой JpegYuvPipeline / MediaCodec / ImageReader
                                //
                                // Пример:
                                // JpegYuvPipeline.pushYuvToImageReader(
                                //         someImageReader,
                                //         nv21Resized,
                                //         TARGET_W,
                                //         TARGET_H
                                // );

                            } catch (Throwable t) {
                                XposedBridge.log("EskukapHook: analyze resize error: " + t);
                            }
                        }
                    }
            );

            XposedBridge.log("EskukapHook: Analyzer hook with fast YUV resize installed");

        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: Analyzer hook not installed: " + t);
        }
    }

    // ====================== YUV_420_888 → NV21 ======================
    // Берём ImageProxy (Y, U, V отдельные plane’ы) и собираем в NV21:
    // [YYYYYY][VUVU...] без Bitmap/JPEG.
    private static byte[] yuv420ImageProxyToNv21(ImageProxy image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuf = planes[0].getBuffer();
        ByteBuffer uBuf = planes[1].getBuffer();
        ByteBuffer vBuf = planes[2].getBuffer();

        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();

        byte[] nv21 = new byte[width * height * 3 / 2];

        // ---- копируем Y-плоскость с учётом rowStride ----
        int pos = 0;
        byte[] row = new byte[yRowStride];
        for (int y = 0; y < height; y++) {
            int rowStart = y * yRowStride;
            yBuf.position(rowStart);
            yBuf.get(row, 0, yRowStride);
            System.arraycopy(row, 0, nv21, pos, width);
            pos += width;
        }

        // ---- копируем UV как VU в NV21 ----
        int uvHeight = height / 2;
        int uvWidth = width / 2;

        int ySize = width * height;
        int uvPos = ySize;

        byte[] uRow = new byte[uvRowStride];
        byte[] vRow = new byte[uvRowStride];

        for (int y = 0; y < uvHeight; y++) {
            int rowStart = y * uvRowStride;

            uBuf.position(rowStart);
            vBuf.position(rowStart);
            uBuf.get(uRow, 0, uvRowStride);
            vBuf.get(vRow, 0, uvRowStride);

            for (int x = 0; x < uvWidth; x++) {
                int idx = x * uvPixelStride;
                byte v = vRow[idx];
                byte u = uRow[idx];

                // NV21: V потом U
                nv21[uvPos++] = v;
                nv21[uvPos++] = u;
            }
        }

        return nv21;
    }

    // ====================== Быстрый resize NV21 → NV21 ======================
    // Nearest-neighbor, без float-alloc, максимум скорости.
    // Подходит для realtime 30/60fps.
    private static byte[] resizeNv21(byte[] src, int srcW, int srcH, int dstW, int dstH) {
        int srcYSize = srcW * srcH;
        int dstYSize = dstW * dstH;

        byte[] dst = new byte[dstYSize + dstW * dstH / 2];

        // --- масштабирование Y (полное разрешение) ---
        float scaleX = (float) srcW / dstW;
        float scaleY = (float) srcH / dstH;

        for (int y = 0; y < dstH; y++) {
            int srcY = (int) (y * scaleY);
            int srcYOff = srcY * srcW;
            int dstYOff = y * dstW;

            for (int x = 0; x < dstW; x++) {
                int srcX = (int) (x * scaleX);
                dst[dstYOff + x] = src[srcYOff + srcX];
            }
        }

        // --- масштабирование UV (половинное разрешение, VU interleaved) ---
        int srcUvW = srcW;
        int srcUvH = srcH / 2;
        int dstUvW = dstW;
        int dstUvH = dstH / 2;

        int srcUvStart = srcYSize;
        int dstUvStart = dstYSize;

        float scaleXuv = (float) srcUvW / dstUvW;
        float scaleYuv = (float) srcUvH / dstUvH;

        for (int y = 0; y < dstUvH; y++) {
            int srcY = (int) (y * scaleYuv);
            int srcRowOff = srcUvStart + srcY * srcUvW;
            int dstRowOff = dstUvStart + y * dstUvW;

            for (int x = 0; x < dstUvW; x += 2) {
                int srcX = (int) (x * scaleXuv);

                int srcIdx = srcRowOff + srcX;
                int dstIdx = dstRowOff + x;

                // копируем VU как группу из 2 байт
                dst[dstIdx] = src[srcIdx];       // V
                if (srcIdx + 1 < src.length) {
                    dst[dstIdx + 1] = src[srcIdx + 1]; // U
                }
            }
        }

        return dst;
    }
}
