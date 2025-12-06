package com.gaba.eskukap;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executor;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Целевое приложение (пока такси. Потом можно добавить другие)
        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("EskukapHook LOADED: " + lpparam.packageName);

        try {

            // Hook Camera2 createCaptureSession → получаем момент старта камеры
            XposedHelpers.findAndHookMethod(
                    CameraDevice.class.getName(),
                    lpparam.classLoader,
                    "createCaptureSession",
                    List.class,
                    CameraCaptureSession.StateCallback.class,
                    Executor.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("Eskukap CameraSession created!");
                        }
                    }
            );

        } catch (Throwable e) {
            XposedBridge.log("EskukapHook ERROR: " + e);
        }
    }
}
