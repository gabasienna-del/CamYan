package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.media.Image;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded taximeter");

        // ---- Хук открытия камеры ----
        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    android.os.Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera open request -> " + param.args[0]);
                        }
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera opened");
                        }
                    }
            );
        } catch (Throwable e) { XposedBridge.log("EskukapHook CAMERA2 HOOK ERROR " + e); }

        // ---- Перехват ImageReader (видео кадры) ----
        try {
            XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "onImageAvailable",
                ImageReader.OnImageAvailableListener.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log("EskukapHook: Frame incoming...");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ImageReader reader = (ImageReader) param.thisObject;
                        Image image = reader.acquireLatestImage();

                        if (image != null) {
                            XposedBridge.log("EskukapHook: Got frame -> " + image.getWidth() + "x" + image.getHeight());

                            // ❗ здесь дальше будет подмена изображения
                            image.close(); // временно закрываем кадр
                        }
                    }
                }
            );
        } catch (Throwable e) { XposedBridge.log("EskukapHook FRAME HOOK ERROR " + e); }
    }
}
