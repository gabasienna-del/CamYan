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

// JPEG -> YUV helper'ы
import com.gaba.eskukap.JpegYuvPipeline;
import com.gaba.eskukap.FileHelper;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // 1. CameraManager.openCamera
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

        // 2. Camera2 CaptureCallback
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
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Capture completed — frame available (Camera2)");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 CaptureCallback HOOK ERROR: " + e);
        }

        // 3. CameraX Analyzer (может не существовать)
        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object imageProxy = param.args[0];
                            XposedBridge.log("EskukapHook: CameraX analyze() frame -> " + imageProxy);
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERAX Analyzer HOOK ERROR: " + e);
        }

        // 4. MediaCodec.queueInputBuffer — только лог
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.MediaCodec",
                    lpparam.classLoader,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int index = (int) param.args[0];
                            int offset = (int) param.args[1];
                            int size   = (int) param.args[2];
                            long pts   = (long) param.args[3];

                            XposedBridge.log(
                                    "EskukapHook: MediaCodec.queueInputBuffer idx=" +
                                            index + " off=" + offset + " size=" + size + " pts=" + pts
                            );
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook MEDIACODEC queueInputBuffer HOOK ERROR: " + e);
        }

        // 5. ImageReader.acquireLatestImage — ТУТ МЫ ПОДМЕНЯЕМ КАДР НА JPEG
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image image = (Image) param.getResult();
                            if (image == null) return;

                            int w = image.getWidth();
                            int h = image.getHeight();
                            XposedBridge.log("EskukapHook: ImageReader frame " + w + "x" + h);

                            try {
                                String path = "/sdcard/eskukap/frame.jpg";
                                byte[] jpegData = FileHelper.readFile(path);

                                if (jpegData == null) {
                                    XposedBridge.log("EskukapHook: JPEG not found: " + path);
                                    return;
                                }

                                int[] outWH = new int[2];
                                byte[] yuv = JpegYuvPipeline.jpegToYuv420(jpegData, outWH);

                                if (yuv == null) {
                                    XposedBridge.log("EskukapHook: jpegToYuv420 returned null");
                                    return;
                                }

                                if (outWH[0] != w || outWH[1] != h) {
                                    XposedBridge.log(
                                            "EskukapHook: JPEG size " + outWH[0] + "x" + outWH[1] +
                                                    " != camera " + w + "x" + h + " — skip replace"
                                    );
                                    return;
                                }

                                JpegYuvPipeline.overwriteImageWithI420(image, yuv, w, h);
                                XposedBridge.log("EskukapHook: FRAME REPLACED WITH JPEG " + w + "x" + h);

                            } catch (Throwable t) {
                                XposedBridge.log("EskukapHook: jpeg->yuv replace error: " + t);
                            }

                            // image.close(); // по-прежнему не закрываем
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGEREADER acquireLatestImage HOOK ERROR: " + e);
        }

        // 6. Динамический хук загрузки Analyzer (может не сработать — и это ок)
        try {
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
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
                                                protected void beforeHookedMethod(MethodHookParam param) {
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
