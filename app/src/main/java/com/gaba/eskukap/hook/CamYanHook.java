package com.gaba.eskukap.hook;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    private static final String TAG = "CamYan";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Работаем только с нужными приложениями
        if (!"com.vkontakte.android".equals(lpparam.packageName)
                && !"ru.yandex.taximeter".equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + ": " + lpparam.packageName + " detected, applying camera hook...");

        // ---------- Хук CameraManager.openCamera ----------
        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    android.hardware.camera2.CameraDevice.StateCallback.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> CameraManager.openCamera BEFORE");
                            // тут потом можно подменять ID камеры, если нужно
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> CameraManager.openCamera AFTER");
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook CameraManager.openCamera in "
                    + lpparam.packageName + ": " + t.getMessage());
        }

        // ---------- Хук реализации capture(), НЕ абстрактного метода ----------
        try {
            // Реальный класс реализации с небуквенным API
            Class<?> implClass = XposedHelpers.findClassIfExists(
                    "android.hardware.camera2.impl.CameraCaptureSessionImpl",
                    null  // boot classloader
            );

            if (implClass == null) {
                XposedBridge.log(TAG + ": CameraCaptureSessionImpl not found, skip capture hook in "
                        + lpparam.packageName);
                return;
            }

            XposedBridge.log(TAG + ": hooking CameraCaptureSessionImpl.capture in "
                    + lpparam.packageName);

            XposedHelpers.findAndHookMethod(
                    implClass,
                    "capture",
                    CaptureRequest.class,
                    CameraCaptureSession.CaptureCallback.class,
                    Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> capture BEFORE");
                            // здесь потом можно подменять запрос/картинку
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": " + lpparam.packageName
                                    + " -> capture AFTER");
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook capture() impl in "
                    + lpparam.packageName + ": " + t.getMessage());
        }
    }
}
