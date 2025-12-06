package com.gaba.eskukap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Простой тестовый хук:
 * - логируем загрузку ru.yandex.taximeter
 * - хукаем String.toString() только для проверки работы модуля.
 */
public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log("EskukapHook: handleLoadPackage for " + lpparam.packageName);

        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.String",
                    lpparam.classLoader,
                    "toString",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("EskukapHook: String.toString() called");
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log("EskukapHook ERROR in hook: " + t);
        }
    }
}
