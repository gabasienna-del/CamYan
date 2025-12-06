package com.gaba.eskukap;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        // приложение для теста камеры — ставь любое
        String target = "ru.yandex.taximeter"; // можешь заменить на com.vkontakte.android

        if (!lpparam.packageName.equals(target)) return;

        Log.i("EskukapHook", "Hook active for " + lpparam.packageName);

        findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "acquireLatestImage",
                new FakeCameraHook()
        );

        Log.i("EskukapHook", "Camera hook injected ✔");
    }
}
