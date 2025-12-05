package com.gaba.eskukap.hook;

import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final String TAG = "CamYan";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Целевые приложения
        if (!"com.vkontakte.android".equals(lpparam.packageName)
                && !"ru.yandex.taximeter".equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + ": " + lpparam.packageName + " detected, applying camera hook...");

        hookLegacyCamera(lpparam);
        hookCamera2Open(lpparam);
        hookCamera2Capture(lpparam);
    }

    /** Хук старого API android.hardware.Camera */
    private void hookLegacyCamera(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.Camera",
                    lpparam.classLoader,
                    "takePicture",
                    Camera.ShutterCallback.class,
                    Camera.PictureCallback.class,
                    Camera.PictureCallback.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Camera.takePicture BEFORE in " + lpparam.packageName);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Camera.takePicture AFTER in " + lpparam.packageName);
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": no legacy Camera in " + lpparam.packageName + " : " + t.getMessage());
        }
    }

    /** Хук открытия камеры Camera2: CameraManager.openCamera */
    private void hookCamera2Open(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraManager",
                    lpparam.classLoader,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> CameraManager.openCamera BEFORE");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> CameraManager.openCamera AFTER");
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": no CameraManager.openCamera in "
                    + lpparam.packageName + " : " + t.getMessage());
        }
    }

    /** Хук съёмки в Camera2: CameraCaptureSession.capture */
    private void hookCamera2Capture(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraCaptureSession",
                    lpparam.classLoader,
                    "capture",
                    CaptureRequest.class,
                    CameraCaptureSession.CaptureCallback.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Camera2 capture BEFORE in " + lpparam.packageName);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Camera2 capture AFTER in " + lpparam.packageName);
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": no CameraCaptureSession.capture in "
                    + lpparam.packageName + " : " + t.getMessage());
        }
    }
}
