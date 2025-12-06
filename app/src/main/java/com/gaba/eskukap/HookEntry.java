package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedBridge.log("Eskukap: Loaded -> " + lpparam.packageName);

        try {

            XposedHelpers.findAndHookConstructor(
                SurfaceTexture.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {

                        SurfaceTexture tex = (SurfaceTexture) param.thisObject;

                        tex.setOnFrameAvailableListener(
                            new SurfaceTexture.OnFrameAvailableListener() {
                                @Override
                                public void onFrameAvailable(SurfaceTexture surfaceTexture) {

                                    XposedBridge.log("Eskukap: Surface frame!");

                                    // 👉 Здесь будет подмена кадров GPU
                                    fakeFrame();
                                }
                            }, new Handler(Looper.getMainLooper()));

                        XposedBridge.log("Eskukap: SurfaceTexture HOOK OK");
                    }
                });

        } catch (Throwable e) {
            XposedBridge.log("Eskukap: ST hook fail " + e);
        }
    }

    // -------- Функция, где мы вставим fake.jpg вместо камеры --------

    private void fakeFrame() {

        File img = new File("/sdcard/Eskukap/fake.jpg");
        if (!img.exists()) return;

        Bitmap bmp = BitmapFactory.decodeFile(img.getAbsolutePath());
        if (bmp == null) return;

        // Сейчас просто логируем, чтобы убедиться что кадровый вызов ловится
        XposedBridge.log("Eskukap: fake frame call OK");
    }
}
