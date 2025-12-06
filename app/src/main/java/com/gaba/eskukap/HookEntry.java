package com.gaba.eskukap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.PixelCopy;
import android.view.Window;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    private int frame = 0;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.contains("taximeter")) return;
        XposedBridge.log("Eskukap PixelCopy Window Hook Loaded for " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final Activity act = (Activity) param.thisObject;

                        try {
                            // размеры экрана
                            DisplayMetrics dm = act.getResources().getDisplayMetrics();
                            int width = dm.widthPixels;
                            int height = dm.heightPixels;

                            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            final Window window = act.getWindow();
                            final Handler handler = new Handler(Looper.getMainLooper());

                            XposedBridge.log("Eskukap: Start PixelCopy for window " +
                                    width + "x" + height);

                            PixelCopy.request(
                                    window,
                                    bitmap,
                                    new PixelCopy.OnPixelCopyFinishedListener() {
                                        @Override
                                        public void onPixelCopyFinished(int result) {
                                            if (result == PixelCopy.SUCCESS) {
                                                try {
                                                    File dir = new File(Environment.getExternalStorageDirectory()
                                                            + "/Eskukap");
                                                    //noinspection ResultOfMethodCallIgnored
                                                    dir.mkdirs();
                                                    File file = new File(dir, "frame_" + (frame++) + ".jpg");

                                                    FileOutputStream fos = new FileOutputStream(file);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                                    fos.close();

                                                    XposedBridge.log("Eskukap: Saved frame to " +
                                                            file.getAbsolutePath());
                                                } catch (Throwable e) {
                                                    XposedBridge.log("Eskukap: Save error " + e);
                                                }
                                            } else {
                                                XposedBridge.log("Eskukap: PixelCopy failed, code=" + result);
                                            }
                                        }
                                    },
                                    handler
                            );

                        } catch (Throwable e) {
                            XposedBridge.log("Eskukap: PixelCopy hook error " + e);
                        }
                    }
                }
        );
    }
}
