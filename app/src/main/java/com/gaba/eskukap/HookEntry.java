package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import android.graphics.SurfaceTexture;
import android.view.PixelCopy;
import android.app.Activity;
import android.view.Window;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("Eskukap: hook active - scanning layouts");

        XposedHelpers.findAndHookMethod(
                TextureView.class,
                "setSurfaceTextureListener",
                TextureView.SurfaceTextureListener.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("Eskukap: hook TextureView attached");

                        TextureView tv = (TextureView) param.thisObject;

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            XposedBridge.log("Eskukap: TRY PixelCopy");

                            Bitmap bmp = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
                            Window w = getActivityWindow(tv);
                            if (w == null) return;

                            PixelCopy.request(w, bmp, copyResult -> {
                                if (copyResult == PixelCopy.SUCCESS) {
                                    saveBitmap(bmp);
                                    XposedBridge.log("Eskukap: FRAME SAVED");
                                }
                            }, new Handler(Looper.getMainLooper()));
                        }, 1500);
                    }
                }
        );
    }

    private Window getActivityWindow(View v) {
        try {
            Activity a = (Activity) v.getContext();
            return a.getWindow();
        } catch (Throwable e) { return null; }
    }

    private void saveBitmap(Bitmap bmp) {
        try {
            File dir = new File("/sdcard/Eskukap/");
            if (!dir.exists()) dir.mkdirs();

            File f = new File(dir, "frame_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.close();
        } catch (Throwable e) {
            XposedBridge.log("Eskukap SAVE ERROR: " + e.getMessage());
        }
    }
}
