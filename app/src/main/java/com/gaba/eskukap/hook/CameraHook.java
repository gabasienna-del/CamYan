package com.gaba.eskukap.hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class CameraHook extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        XposedBridge.log("CamYan CameraHook BEFORE -> " + param.method.getName());
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        XposedBridge.log("CamYan CameraHook AFTER -> " + param.method.getName());
    }
}
