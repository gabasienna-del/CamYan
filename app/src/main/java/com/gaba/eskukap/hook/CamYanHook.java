package com.gaba.eskukap.hook;

import android.hardware.Camera;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Чтобы не спамить в системные процессы
        if (lpparam.packageName.equals("android")
                || lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        // 1) Пытаемся хукнуть старую android.hardware.Camera
        try {
            XposedBridge.log("CamYan: try hook android.hardware.Camera in " + lpparam.packageName);

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
                            XposedBridge.log("CamYan: " + lpparam.packageName + " -> Camera.takePicture BEFORE");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("CamYan: " + lpparam.packageName + " -> Camera.takePicture AFTER");
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log("CamYan: no android.hardware.Camera in " + lpparam.packageName + " : " + t.getMessage());
        }

        // 2) Пытаемся хукнуть Camera2: CameraManager.openCamera(...)
        try {
            XposedBridge.log("CamYan: try hook Camera2 in " + lpparam.packageName);

            Class<?> camManagerClass = XposedHelpers.findClass(
                    "android.hardware.camera2.CameraManager",
                    lpparam.classLoader
            );

            XposedBridge.hookAllMethods(camManagerClass, "openCamera", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("CamYan: " + lpparam.packageName + " -> CameraManager.openCamera BEFORE");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("CamYan: " + lpparam.packageName + " -> CameraManager.openCamera AFTER");
                }
            });

        } catch (Throwable t) {
            XposedBridge.log("CamYan: no Camera2 in " + lpparam.packageName + " : " + t.getMessage());
        }
    }
}
