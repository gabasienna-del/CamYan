package com.gaba.eskukap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.View;
import android.view.PixelCopy;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static boolean capturedOnce = false;

    private static void saveBitmap(Bitmap bmp) {
        try {
            File dir = new File("/sdcard/Eskukap/");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "frame_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(out);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();
            XposedBridge.log("Eskukap: SAVED -> " + out.getAbsolutePath());
        } catch (Throwable e) {
            XposedBridge.log("Eskukap: Save ERR " + e);
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log("Eskukap: PixelCopy hook for " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onWindowFocusChanged",
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        boolean hasFocus = (Boolean) param.args[0];
                        if (!hasFocus) return;           // окно потеряло фокус
                        if (capturedOnce) return;         // уже сняли кадр

                        final Activity act = (Activity) param.thisObject;
                        Window window = act.getWindow();
                        if (window == null) return;

                        final View decor = window.getDecorView();
                        if (decor == null) return;

                        final int w = decor.getWidth();
                        final int h = decor.getHeight();
                        if (w <= 0 || h <= 0) {
                            XposedBridge.log("Eskukap: decor size 0, skip");
                            return;
                        }

                        XposedBridge.log("Eskukap: Start PixelCopy for window " + w + "x" + h);

                        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                        Handler handler = new Handler(Looper.getMainLooper());

                        // чуть подождать, чтобы точно был surface
                        decor.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PixelCopy.request(window, bitmap, copyResult -> {
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            XposedBridge.log("Eskukap: PixelCopy OK");
                                            saveBitmap(bitmap);
                                            capturedOnce = true;
                                        } else {
                                            XposedBridge.log("Eskukap: PixelCopy fail code=" + copyResult);
                                        }
                                    }, handler);
                                } catch (Throwable e) {
                                    XposedBridge.log("Eskukap: PixelCopy error " + e);
                                }
                            }
                        }, 500); // 0.5 сек задержка
                    }
                });
    }
}
