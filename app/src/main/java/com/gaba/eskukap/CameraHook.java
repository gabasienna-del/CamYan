package com.gaba.eskukap;

import android.content.Intent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CameraHook {

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Eskukap: loaded " + lpparam.packageName);

        // Пример: логировать onActivityResult во всех Activity
        XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onActivityResult",
                int.class, int.class, Intent.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("Eskukap: onActivityResult " + lpparam.packageName);
                    }
                }
        );
    }
}
