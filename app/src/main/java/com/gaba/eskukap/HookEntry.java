package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.media.Image;

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
                    android.os.Handler.class,
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
                    android.hardware.camera2.CameraCaptureSession.class,
                    android.hardware.camera2.CaptureRequest.class,
                    android.hardware.camera2.TotalCaptureResult.class,
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

        // ---- 3. CameraX: статический хук Analyzer.analyze(ImageProxy) ----
        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object imageProxy = param.args[0];
                            XposedBridge.log("EskukapHook: CameraX analyze() frame -> " + imageProxy);
                            // TODO: подмена содержимого через ImageProxy, если нужно
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERAX Analyzer HOOK ERROR: " + e);
        }

        // ---- 4. MediaCodec.queueInputBuffer (видеопоток) ----
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.MediaCodec",
                    lpparam.classLoader,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int index = (int) param.args[0];
                            int offset = (int) param.args[1];
                            int size   = (int) param.args[2];
                            long pts   = (long) param.args[3];

                            XposedBridge.log(
                                    "EskukapHook: MediaCodec.queueInputBuffer idx=" +
                                            index + " off=" + offset + " size=" + size + " pts=" + pts
                            );
                            // TODO: здесь можно менять содержимое ByteBuffer перед кодеком
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook MEDIACODEC queueInputBuffer HOOK ERROR: " + e);
        }

        // ---- 5. ImageReader: хук acquireLatestImage() ----
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Image image = (Image) param.getResult();
                            if (image != null) {
                                int w = image.getWidth();
                                int h = image.getHeight();
                                XposedBridge.log("EskukapHook: ImageReader frame " + w + "x" + h);

                                // ↓↓↓ Сюда вставлять обработку кадра
                                // (JPG → YUV → подмена содержимого, запись в буфер и т.п.)

                                // image.close(); // НЕ закрываем — пусть приложение само закроет
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGEREADER acquireLatestImage HOOK ERROR: " + e);
        }

        // ---- 6. Динамический хук через ClassLoader.loadClass для Analyzer ----
        try {
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String name = (String) param.args[0];
                            Object result = param.getResult();

                            if (!(result instanceof Class<?>)) return;

                            if ("androidx.camera.core.ImageAnalysis$Analyzer".equals(name)) {
                                Class<?> cls = (Class<?>) result;
                                XposedBridge.log("EskukapHook: Analyzer LOADED -> Hooking analyze() dynamically");

                                try {
                                    XposedHelpers.findAndHookMethod(
                                            cls,
                                            "analyze",
                                            "androidx.camera.core.ImageProxy",
                                            new XC_MethodHook() {
                                                @Override
                                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                    XposedBridge.log("EskukapHook: Frame received! (dynamic ClassLoader hook)");
                                                }
                                            }
                                    );
                                } catch (Throwable e) {
                                    XposedBridge.log("EskukapHook: Error hooking Analyzer.analyze dynamically: " + e);
                                }
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook LOADCLASS HOOK ERROR: " + e);
        }
    }
}
