package com.gaba.eskukap.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.vkontakte.android")) return;

        XposedBridge.log("CamYan: VK detected. Hook apply...");

        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.camera2.CameraManager",
                lpparam.classLoader,
                "openCamera",
                String.class,
                android.hardware.camera2.CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new de.robv.android.xposed.XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: openCamera BEFORE");
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: openCamera AFTER");
                    }
                }
            );

        } catch (Throwable t) {
            XposedBridge.log("CamYan ERROR: " + t);
        }
    }
}
