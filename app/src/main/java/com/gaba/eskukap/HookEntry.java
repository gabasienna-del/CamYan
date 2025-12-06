package com.gaba.eskukap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        // тут выбираешь, для какого приложения хукать
        String target = "ru.yandex.taximeter"; // или "ru.yandex.taximeter.passport" или "com.vkontakte.android"

        if (!lpparam.packageName.equals(target)) return;

        XposedBridge.log("EskukapHook: handleLoadPackage for " + lpparam.packageName);

        // хукаем системный класс ImageReader
        findAndHookMethod(
                "android.media.ImageReader",
                null,                 // ВАЖНО: системный класс → null
                "acquireLatestImage",
                new FakeCameraHook()
        );

        XposedBridge.log("EskukapHook: Camera hook injected");
    }
}
