package com.gaba.eskukap;

import android.view.Surface;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.PixelCopy;
import android.app.Activity;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.ByteBuffer;

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
        XposedBridge.log("Eskukap PixelCopy Hook loaded!");

        XposedHelpers.findAndHookMethod(
            "android.app.Activity", lpparam.classLoader, "onResume",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity act = (Activity) param.thisObject;

                    try {
                        SurfaceView sv = act.findViewById(
                            act.getResources().getIdentifier("camera_view","id",lpparam.packageName));

                        if (sv == null) {
                            XposedBridge.log("Eskukap: SurfaceView not found");
                            return;
                        }

                        Surface surf = sv.getHolder().getSurface();
                        Bitmap bmp = Bitmap.createBitmap(720,1280, Bitmap.Config.ARGB_8888);

                        PixelCopy.request(surf, bmp, result -> {
                            try {
                                File dir = new File(Environment.getExternalStorageDirectory()+"/Eskukap");
                                dir.mkdirs();
                                File f = new File(dir,"frame_"+(frame++)+".jpg");
                                FileOutputStream fos = new FileOutputStream(f);
                                bmp.compress(Bitmap.CompressFormat.JPEG,90,fos);
                                fos.close();
                                XposedBridge.log("Eskukap Saved "+f.getAbsolutePath());
                            } catch(Exception e){ XposedBridge.log("Eskukap save error "+e); }
                        }, act.getMainLooper());

                    } catch(Exception e){
                        XposedBridge.log("Eskukap ERR "+e);
                    }
                }
            }
        );
    }
}
