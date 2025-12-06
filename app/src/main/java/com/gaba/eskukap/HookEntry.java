package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded taximeter");

        // Хук открытия камеры
        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    android.os.Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("EskukapHook: Camera open request -> " + param.args[0]);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("EskukapHook: Camera opened");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 HOOK ERROR: " + e);
        }
    }
}
