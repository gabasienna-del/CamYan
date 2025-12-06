package com.gaba.eskukap;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // Camera2: openCamera log
        XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
        );

        // Camera2: ImageReader.acquireLatestImage hook — FRAME REPLACER
        XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "acquireLatestImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Image img = (Image) param.getResult();
                        if (img == null) return;

                        int w = img.getWidth();
                        int h = img.getHeight();

                        XposedBridge.log("EskukapHook: Frame " + w + "x" + h);

                        // LOAD JPEG
                        byte[] jpeg = FileHelper.loadJPEG();
                        if (jpeg == null) {
                            XposedBridge.log("EskukapHook: ❌ JPEG NOT FOUND");
                            return;
                        }

                        // Convert JPEG → YUV
                        int[] out = new int[2];
                        byte[] yuv = JpegYuvPipeline.jpegToYuv420(jpeg, out);
                        if (yuv == null) {
                            XposedBridge.log("EskukapHook: ❌ JPEG to YUV fail");
                            return;
                        }

                        // Push to virtual ImageReader surface (frame injection)
                        JpegYuvPipeline.pushYuvToImageReader(
                                (ImageReader) param.thisObject,
                                yuv,
                                out[0],
                                out[1]
                        );

                        XposedBridge.log("EskukapHook: ✅ FRAME REPLACED WITH JPEG");
                    }
                }
        );

        // CameraX: ImageAnalysis.Analyzer.analyze(ImageProxy) — YUV → JPEG capture
        try {
            Class<?> analyzerClass = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader
            );
            Class<?> imageProxyClass = XposedHelpers.findClass(
                    "androidx.camera.core.ImageProxy",
                    lpparam.classLoader
            );

            XposedBridge.log("EskukapHook: Analyzer & ImageProxy classes found");

            XposedHelpers.findAndHookMethod(
                    analyzerClass,
                    "analyze",
                    imageProxyClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object arg0 = param.args[0];
                                if (arg0 == null) return;

                                ImageProxy image = (ImageProxy) arg0;
                                ImageProxy.PlaneProxy[] planes = image.getPlanes();
                                if (planes == null || planes.length < 3) {
                                    XposedBridge.log("EskukapHook: Not enough planes");
                                    return;
                                }

                                ByteBuffer y = planes[0].getBuffer();
                                ByteBuffer u = planes[1].getBuffer();
                                ByteBuffer v = planes[2].getBuffer();

                                byte[] yBytes = new byte[y.remaining()];
                                byte[] uBytes = new byte[u.remaining()];
                                byte[] vBytes = new byte[v.remaining()];

                                y.get(yBytes);
                                u.get(uBytes);
                                v.get(vBytes);

                                XposedBridge.log("EskukapHook: YUV captured, converting...");

                                // упрощённый YUV -> JPEG: используем только Y-плоскость как NV21
                                YuvImage yuvImage = new YuvImage(
                                        yBytes,
                                        ImageFormat.NV21,
                                        image.getWidth(),
                                        image.getHeight(),
                                        null
                                );

                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                boolean ok = yuvImage.compressToJpeg(
                                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                                        90,
                                        out
                                );
                                if (!ok) {
                                    XposedBridge.log("EskukapHook: JPEG compress fail");
                                    return;
                                }

                                byte[] jpegBytes = out.toByteArray();
                                XposedBridge.log("EskukapHook: JPEG ready, size=" + jpegBytes.length);

                                // при желании можно сохранить в файл:
                                // FileHelper.saveCapturedJPEG(jpegBytes);

                            } catch (Throwable t) {
                                XposedBridge.log("EskukapHook: analyze hook error: " +
                                        Log.getStackTraceString(t));
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: Analyzer hook not installed: " +
                    Log.getStackTraceString(t));
        }
    }
}
