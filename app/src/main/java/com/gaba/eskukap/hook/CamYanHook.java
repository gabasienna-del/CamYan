package com.gaba.eskukap.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // ПРОСТО ЛОГ – без return, без фильтра по пакету
        XposedBridge.log("CamYan: handleLoadPackage -> " + lpparam.packageName);
    }
}
