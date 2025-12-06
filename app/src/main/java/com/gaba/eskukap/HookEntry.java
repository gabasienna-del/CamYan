package com.gaba.eskukap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("EskukapHook: handleLoadPackage OK for " + lpparam.packageName);

        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraDevice",
                    lpparam.classLoader,
                    "createCaptureSession",
                    java.util.List.class,
                    android.hardware.camera2.CameraCaptureSession.StateCallback.class,
                    java.util.concurrent.Executor.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("Eskukap: Camera hook triggered");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("Eskukap ERROR: " + e);
        }
    }
}
