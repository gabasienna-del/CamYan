package com.gaba.eskukap.hook;

import android.hardware.Camera;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // работаем только с VK (можно добавить ещё пакеты)
        if (!lpparam.packageName.equals("com.vkontakte.android")) return;

        XposedBridge.log("CamYan: VK detected, applying camera hook...");

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
                        XposedBridge.log("CamYan: takePicture HOOKED before");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: takePicture HOOKED after");
                    }
                }
        );
    }
}
