package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.Image;
import android.media.MediaCodec;
import android.os.Handler;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Хукаем только таксометр
        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // ---- 1. Хук открытия камеры (CameraManager.openCamera) ----
        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera open request -> " + param.args[0]);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera opened");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 openCamera HOOK ERROR: " + e);
        }

        // ---- 2. Camera2: перехват кадров через CaptureCallback.onCaptureCompleted ----
        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraCaptureSession$CaptureCallback",
                    lpparam.classLoader,
                    "onCaptureCompleted",
                    CameraCaptureSession.class,
                    CaptureRequest.class,
                    TotalCaptureResult.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("EskukapHook: Capture completed — frame available (Camera2)");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 CaptureCallback HOOK ERROR: " + e);
        }

        // ---- 3. CameraX: Analyzer.analyze(ImageProxy) через findClass ----
        try {
            Class<?> analyzerClass = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader
            );

            XposedBridge.log("EskukapHook: CameraX Analyzer found!");

            XposedHelpers.findAndHookMethod(
                    analyzerClass,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("EskukapHook", "CameraX Analyzer frame");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook: CameraX Analyzer not found — skip");
        }

        // ---- 4. MediaCodec.queueInputBuffer (видеопоток) ----
        try {
            XposedHelpers.findAndHookMethod(
                    MediaCodec.class,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int index  = (int)  param.args[0];
                            int offset = (int)  param.args[1];
                            int size   = (int)  param.args[2];
                            long pts   = (long) param.args[3];

                            XposedBridge.log("EskukapHook: MediaCodec.queueInputBuffer idx=" +
                                    index + " off=" + offset + " size=" + size + " pts=" + pts);
                            // TODO: тут можно менять содержимое ByteBuffer перед кодеком
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook MEDIACODEC queueInputBuffer HOOK ERROR: " + e);
        }

        // ---- 5. ImageReader: acquireLatestImage() ----
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Image image = (Image) param.getResult();
                            if (image == null) return;

                            int w = image.getWidth();
                            int h = image.getHeight();

                            XposedBridge.log("EskukapHook: ImageReader acquireLatestImage " + w + "x" + h);

                            // TODO: здесь позже подмена кадра (JPG → YUV → обратно)
                            image.close();
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGEREADER acquireLatestImage HOOK ERROR: " + e);
        }

        // ---- 6. ImageReader: acquireNextImage() (твой новый код) ----
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.ImageReader",
                    lpparam.classLoader,
                    "acquireNextImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Image img = (Image) param.getResult();
                            if (img == null) return;

                            XposedBridge.log("EskukapHook: Frame intercepted: " +
                                    img.getWidth() + "x" + img.getHeight());

                            // TODO: тут уже можно менять YUV -> JPG -> YUV или подменять Buffer
                            img.close();
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook: ImageReader acquireNextImage hook error " + e);
        }
    }
}
