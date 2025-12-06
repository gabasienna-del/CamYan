package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // приложение для хука
        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("Eskukap: TextureView CAM hook init");

        Class<?> listener = XposedHelpers.findClass(
                "android.view.TextureView$SurfaceTextureListener",
                lpparam.classLoader
        );

        XposedHelpers.findAndHookMethod(listener,
                "onSurfaceTextureAvailable",
                SurfaceTexture.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("Eskukap: Camera TextureView available");

                        TextureView view = (TextureView) param.thisObject;

                        // Делаем кадр и сохраняем
                        Bitmap bmp = view.getBitmap();
                        if (bmp == null) return;

                        File dir = new File("/sdcard/Eskukap/");
                        dir.mkdirs();
                        File file = new File(dir, "frame_" + System.currentTimeMillis() + ".jpg");

                        FileOutputStream fos = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                        fos.close();

                        XposedBridge.log("Eskukap SAVED → " + file.getAbsolutePath());
                    }
                });
    }
}
