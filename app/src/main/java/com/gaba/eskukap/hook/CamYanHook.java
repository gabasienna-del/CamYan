package com.gaba.eskukap.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final String PKG_VK = "com.vkontakte.android";
    private static final String PKG_TAXI = "ru.yandex.taximeter";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        String pkg = lpparam.packageName;

        if (!PKG_VK.equals(pkg) && !PKG_TAXI.equals(pkg)) {
            return;
        }

        XposedBridge.log("CamYan: " + pkg + " detected, applying camera hook...");

        try {
            // Хук CameraCaptureSessionImpl.capture (логирование)
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.impl.CameraCaptureSessionImpl",
                    lpparam.classLoader,
                    "capture",
                    android.hardware.camera2.CaptureRequest.class,
                    android.hardware.camera2.CameraCaptureSession.CaptureCallback.class,
                    android.os.Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("CamYan: " + pkg + " -> capture BEFORE");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("CamYan: " + pkg + " -> capture AFTER");
                        }
                    });

        } catch (Throwable t) {
            XposedBridge.log("CamYanHook ERROR (" + pkg + "): " + t);
        }

        // Запускаем хук подмены картинки
        FakeImageHook.init(lpparam);
    }
}
