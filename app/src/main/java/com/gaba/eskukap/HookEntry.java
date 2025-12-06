package com.gaba.eskukap;

import android.app.Activity;
import android.view.SurfaceView;
import android.view.Surface;
import android.graphics.Bitmap;
import android.view.PixelCopy;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
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
        XposedBridge.log("Eskukap PixelCopy Hook Loaded!");

        XposedHelpers.findAndHookMethod(
                "android.app.Activity", lpparam.classLoader, "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Activity act = (Activity) param.thisObject;

                        SurfaceView sv = act.findViewById(
                                act.getResources().getIdentifier("camera_view","id",lpparam.packageName));

                        if (sv == null) {
                            XposedBridge.log("Eskukap: SurfaceView not found");
                            return;
                        }

                        Surface surface = sv.getHolder().getSurface();

                        // Создаем bitmap по размеру экрана камеры
                        Bitmap bitmap = Bitmap.createBitmap(720,1280, Bitmap.Config.ARGB_8888);

                        PixelCopy.request(surface, bitmap,
                                result -> {
                                    if (result == PixelCopy.SUCCESS) {
                                        try {
                                            File dir = new File(Environment.getExternalStorageDirectory()+"/Eskukap");
                                            dir.mkdirs();
                                            File file = new File(dir,"frame_"+(frame++)+".jpg");

                                            FileOutputStream fos = new FileOutputStream(file);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                            fos.close();

                                            XposedBridge.log("Eskukap Saved: " + file.getAbsolutePath());
                                        } catch (Exception e) {
                                            XposedBridge.log("Eskukap Save Error: "+e.toString());
                                        }
                                    } else {
                                        XposedBridge.log("Eskukap PixelCopy failed: "+result);
                                    }
                                },
                                new Handler(Looper.getMainLooper())
                        );

                    }
                }
        );
    }
}
