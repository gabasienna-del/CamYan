package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    private int frame = 0;
    private boolean capturing = false;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.contains("taximeter")) return;

        XposedBridge.log("Eskukap: TextureView CAM hook loaded");

        XposedHelpers.findAndHookMethod(
                TextureView.class,
                "onSurfaceTextureAvailable",
                SurfaceTexture.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        TextureView tv = (TextureView) param.thisObject;

                        XposedBridge.log("Eskukap: CAMERA Surface FOUND ✔ start capture");

                        capturing = true;
                        startLoop(tv);
                    }
                }
        );
    }

    private void startLoop(TextureView tv) {

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (!capturing) return;

                try {
                    if (tv.isAvailable()) {
                        Bitmap bmp = tv.getBitmap();

                        if (bmp != null) {

                            File dir = new File(tv.getContext().getExternalFilesDir(null), "Eskukap_cam");
                            dir.mkdirs();

                            File file = new File(dir, "cam_" + (frame++) + ".jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            fos.close();

                            XposedBridge.log("Eskukap CAM SAVED → " + file.getAbsolutePath());
                        }
                    }
                } catch (Throwable e) {
                    XposedBridge.log("Eskukap CAM ERROR: " + e);
                }

                handler.postDelayed(this, 800); // ~1.2 FPS, можно уменьшить до 50-150 мс
            }
        };

        handler.postDelayed(run, 1000);
    }
}
