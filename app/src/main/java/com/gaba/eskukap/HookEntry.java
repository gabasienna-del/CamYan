package com.gaba.eskukap;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        String target = "com.yourapp.name"; // <<< поменяй на нужную цель

        if (!lpparam.packageName.equals(target)) return;

        Log.i("EskukapHook", "Hook active -> " + lpparam.packageName);

        // Хук ImageReader.acquireLatestImage на подмену кадра
        XposedHelpers.findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "acquireLatestImage",
                new FakeImageHook()
        );

        Log.i("EskukapHook", "FakeImageHook injected ✔");
    }
}
