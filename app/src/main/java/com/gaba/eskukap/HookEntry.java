package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static void saveFrame(TextureView tv) {
        try {
            Bitmap bmp = tv.getBitmap();
            if (bmp == null) {
                XposedBridge.log("Eskukap CAM: bitmap null");
                return;
            }

            File baseDir = tv.getContext().getExternalFilesDir(null);
            if (baseDir == null) {
                XposedBridge.log("Eskukap CAM: baseDir null");
                return;
            }

            File dir = new File(baseDir, "Eskukap_cam");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();

            File file = new File(dir, "cam_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            XposedBridge.log("Eskukap CAM SAVED -> " + file.getAbsolutePath());
        } catch (Throwable e) {
            XposedBridge.log("Eskukap CAM save error: " + e);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("Eskukap: hook setSurfaceTextureListener");

        Class<?> textureViewClass = XposedHelpers.findClass(
                "android.view.TextureView",
                lpparam.classLoader
        );

        // Хукаем установку слушателя
        XposedHelpers.findAndHookMethod(
                textureViewClass,
                "setSurfaceTextureListener",
                SurfaceTextureListener.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        final TextureView tv = (TextureView) param.thisObject;
                        final Object listener = param.args[0];

                        if (listener == null) {
                            XposedBridge.log("Eskukap CAM: listener null");
                            return;
                        }

                        Class<?> listenerClass = listener.getClass();
                        XposedBridge.log("Eskukap CAM: listener class = " + listenerClass.getName());

                        try {
                            // Хук onSurfaceTextureAvailable конкретного класса
                            XposedHelpers.findAndHookMethod(
                                    listenerClass,
                                    "onSurfaceTextureAvailable",
                                    SurfaceTexture.class, int.class, int.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param2) throws Throwable {
                                            XposedBridge.log("Eskukap CAM: onSurfaceTextureAvailable");
                                            saveFrame(tv); // один кадр при старте
                                        }
                                    }
                            );
                        } catch (Throwable e) {
                            XposedBridge.log("Eskukap CAM: hook onSurfaceTextureAvailable error " + e);
                        }

                        try {
                            // Дополнительно хук onSurfaceTextureUpdated для кадров в процессе
                            XposedHelpers.findAndHookMethod(
                                    listenerClass,
                                    "onSurfaceTextureUpdated",
                                    SurfaceTexture.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param3) throws Throwable {
                                            // Можно сильно заспамить, поэтому пока делаем 1 кадр раз в несколько вызовов
                                            Object counterObj = XposedHelpers.getAdditionalInstanceField(tv, "eskukap_counter");
                                            int counter = (counterObj instanceof Integer) ? (Integer) counterObj : 0;
                                            counter++;
                                            if (counter >= 15) { // примерно каждый 15-й кадр
                                                counter = 0;
                                                XposedBridge.log("Eskukap CAM: onSurfaceTextureUpdated -> save");
                                                saveFrame(tv);
                                            }
                                            XposedHelpers.setAdditionalInstanceField(tv, "eskukap_counter", counter);
                                        }
                                    }
                            );
                        } catch (Throwable e) {
                            XposedBridge.log("Eskukap CAM: hook onSurfaceTextureUpdated error " + e);
                        }
                    }
                }
        );
    }
}
