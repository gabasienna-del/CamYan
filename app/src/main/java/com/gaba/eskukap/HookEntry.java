package com.gaba.eskukap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.PixelCopy;
import android.view.Window;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    int frame = 0;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.contains("taximeter")) return;

        XposedBridge.log("Eskukap PixelCopy DELAY Hook Loaded " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(
            "android.app.Activity", lpparam.classLoader, "onWindowFocusChanged", boolean.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    boolean hasFocus = (boolean) param.args[0];
                    Activity act = (Activity) param.thisObject;
                    if (!hasFocus) return;

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {

                        try {
                            DisplayMetrics dm = act.getResources().getDisplayMetrics();
                            int width = dm.widthPixels;
                            int height = dm.heightPixels;

                            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            Window win = act.getWindow();

                            XposedBridge.log("Eskukap PixelCopy TRY after UI, size = "+width+"x"+height);

                            PixelCopy.request(win, bmp, result -> {
                                if (result == PixelCopy.SUCCESS) {
                                    try {
                                        File dir = new File(Environment.getExternalStorageDirectory()+"/Eskukap");
                                        dir.mkdirs();
                                        File f = new File(dir,"frame_"+(frame++)+".jpg");

                                        FileOutputStream fos = new FileOutputStream(f);
                                        bmp.compress(Bitmap.CompressFormat.JPEG,90,fos);
                                        fos.close();

                                        XposedBridge.log("Eskukap SAVED → "+f.getAbsolutePath());
                                    } catch (Exception e) {
                                        XposedBridge.log("Eskukap Save ERR: "+e);
                                    }
                                } else {
                                    XposedBridge.log("Eskukap PixelCopy FAIL code="+result);
                                }
                            }, new Handler(Looper.getMainLooper()));

                        } catch (Exception e){
                            XposedBridge.log("Eskukap ERR: "+e);
                        }

                    }, 2000); // ждём 2 секунды после появления UI
                }
            }
        );
    }
}
