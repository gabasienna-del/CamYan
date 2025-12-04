package com.gaba.eskukap.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CameraScan implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Фильтр — можно добавлять нужные приложения
        if (!lpparam.packageName.contains("vk") &&
            !lpparam.packageName.contains("google") &&
            !lpparam.packageName.contains("camera") &&
            !lpparam.packageName.contains("photo")) return;

        XposedBridge.log("CamYan CameraScan: Start in " + lpparam.packageName);

        try {
            // API1 Camera
            Class<?> cam = XposedHelpers.findClass("android.hardware.Camera", lpparam.classLoader);
            XposedBridge.log("CamYan CameraScan: Camera API1 found");

            XposedHelpers.findAndHookMethod(cam, "takePicture",
                    android.hardware.Camera.ShutterCallback.class,
                    android.hardware.Camera.PictureCallback.class,
                    android.hardware.Camera.PictureCallback.class,
                    new CameraHook()
            );

        } catch (Throwable e) {
            XposedBridge.log("CamYan CameraScan: No Camera API1");
        }

        try {
            // API2 Camera2
            Class<?> cam2 = XposedHelpers.findClass("android.hardware.camera2.CameraDevice", lpparam.classLoader);
            XposedBridge.log("CamYan CameraScan: Camera API2 FOUND");

            XposedHelpers.findAndHookMethod(cam2, "createCaptureSession",
                    java.util.List.class,
                    android.hardware.camera2.CameraCaptureSession.StateCallback.class,
                    java.util.concurrent.Executor.class,
                    new CameraHook()
            );

        } catch (Throwable e) {
            XposedBridge.log("CamYan CameraScan: No Camera API2");
        }
    }
}
