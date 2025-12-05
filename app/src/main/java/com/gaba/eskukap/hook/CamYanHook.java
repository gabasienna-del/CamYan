package com.gaba.eskukap.hook;

import android.hardware.Camera;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final List<String> TARGET_PACKAGES = Arrays.asList(
            "com.vkontakte.android",
            "org.telegram.messenger",
            "com.whatsapp",
            "com.instagram.android",
            "com.google.android.GoogleCamera",
            "com.sec.android.app.camera"   // стандартная камера Samsung
            // сюда можно добавлять свои пакеты
    );

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!TARGET_PACKAGES.contains(lpparam.packageName)) {
            return; // остальные приложения не трогаем
        }

        XposedBridge.log("CamYan: " + lpparam.packageName + " detected, applying camera hook...");

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
                        XposedBridge.log("CamYan: " + lpparam.packageName + " takePicture BEFORE");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: " + lpparam.packageName + " takePicture AFTER");
                    }
                }
        );
    }
}
