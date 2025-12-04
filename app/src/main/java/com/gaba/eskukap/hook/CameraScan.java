package com.gaba.eskukap.hook;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CameraScan implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // лог во все пакеты чтобы понять где откликается камера
        XposedBridge.log("CamYan SCAN -> " + lpparam.packageName);

        // ----------------- Camera API1 -----------------
        try {
            Class<?> cam = XposedHelpers.findClass("android.hardware.Camera", lpparam.classLoader);
            XposedBridge.log("CamYan: Camera API1 FOUND in " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(cam, "takePicture",
                    android.hardware.Camera.ShutterCallback.class,
                    android.hardware.Camera.PictureCallback.class,
                    android.hardware.Camera.PictureCallback.class,
                    new CameraHook()
            );

        } catch (Throwable ignored) {}

        // ----------------- Camera API2 -----------------
        try {
            Class<?> cam2 = XposedHelpers.findClass("android.hardware.camera2.CameraDevice", lpparam.classLoader);
            XposedBridge.log("CamYan: Camera API2 FOUND in " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(cam2, "createCaptureSession",
                    java.util.List.class,
                    android.hardware.camera2.CameraCaptureSession.StateCallback.class,
                    java.util.concurrent.Executor.class,
                    new CameraHook()
            );

        } catch (Throwable ignored) {}
    }
}
