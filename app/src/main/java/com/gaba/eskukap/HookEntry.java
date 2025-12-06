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
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // 1) CameraManager.openCamera
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
                            XposedBridge.log("EskukapHook: Camera open -> " + param.args[0]);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera opened OK");
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook openCamera ERROR " + e);
        }

        // 2) Camera2 CaptureComplete
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
                            XposedBridge.log("EskukapHook: Capture completed");
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook CaptureCallback ERROR " + e);
        }

        // 3) CameraX Analyzer
        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: CameraX analyze frame");
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook Analyzer ERROR " + e);
        }

        // 4) MediaCodec queueInputBuffer
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.MediaCodec",
                    lpparam.classLoader,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: MediaCodec buffer idx=" + param.args[0]);
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook MediaCodec ERROR " + e);
        }

        // 5) ImageReader.acquireLatestImage — без image.close()
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image image = (Image) param.getResult();
                            if (image != null) {
                                int w = image.getWidth();
                                int h = image.getHeight();
                                XposedBridge.log("EskukapHook: Frame " + w + "x" + h);

                                // *** обработка кадра здесь ***
                                // image.close();  // убрано как просили
                            }
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook ImageReader ERROR " + e);
        }

        // 6) Dynamic load Analyzer
        try {
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[0];

                            if ("androidx.camera.core.ImageAnalysis$Analyzer".equals(name)) {
                                XposedBridge.log("EskukapHook: Analyzer loaded dynamically");
                            }
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log("EskukapHook loadClass ERROR " + e);
        }
    }
}
