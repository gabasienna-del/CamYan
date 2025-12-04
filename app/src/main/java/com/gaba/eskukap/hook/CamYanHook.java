package com.gaba.eskukap.hook;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final String TAG = "CamYan";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Log.i(TAG, "Loaded package: " + lpparam.packageName);

        // хук Camera API 1
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera",
                lpparam.classLoader,
                "open",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Log.i(TAG, "Camera1 OPEN detected");
                    }
                }
            );
        } catch (Throwable e) {
            Log.i(TAG, "Camera1 not found");
        }

        // хук Camera API 2
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.camera2.CameraDevice",
                lpparam.classLoader,
                "createCaptureSession",
                java.util.List.class,
                android.hardware.camera2.CameraCaptureSession.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Log.i(TAG, "Camera2 CREATE SESSION triggered!");
                    }
                }
            );
        } catch (Throwable e) {
            Log.i(TAG, "Camera2 not found");
        }
    }
}
