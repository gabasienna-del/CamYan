package com.gaba.eskukap.hook;

import android.util.Log;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final String TAG = "CamYan";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // только выбранные приложения, иначе будет спам
        if (!lpparam.packageName.contains("camera") &&
            !lpparam.packageName.contains("face") &&
            !lpparam.packageName.contains("photo") &&
            !lpparam.packageName.contains("live") &&
            !lpparam.packageName.contains("yandex") &&
            !lpparam.packageName.contains("taxi")) return;

        Log.i(TAG, "HOOK STARTED -> " + lpparam.packageName);

        // Сканируем классы приложения
        try {
            ClassLoader loader = lpparam.classLoader;
            for (String cls : new String[]{
                    "camera", "face", "capture", "preview", "vision", "mlkit", "detect"
            }) {
                try {
                    for (String pkg : new String[]{
                            lpparam.packageName, "android.hardware", "android.camera", "com"
                    }) {
                        String full = pkg + "." + cls;
                        try {
                            Class<?> cl = loader.loadClass(full);

                            Log.i(TAG, "FOUND CLASS -> " + full);

                            // Хукаем все методы класса
                            for (Method m : cl.getDeclaredMethods()) {
                                XposedHelpers.findAndHookMethod(full, loader, m.getName(),
                                        new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) {
                                                Log.i(TAG, "CALL -> " + full + " :: " + m.getName());
                                            }
                                        });
                            }
                        } catch (Throwable ignore) {}
                    }
                } catch (Throwable ignore) {}
            }
        } catch (Throwable e) {
            Log.e(TAG, "Scan error = " + e);
        }
    }
}
