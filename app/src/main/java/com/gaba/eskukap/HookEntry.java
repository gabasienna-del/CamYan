package com.gaba.eskukap;

import android.media.ImageReader;
import android.media.Image;
import android.graphics.ImageFormat;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.contains("taximeter")) return;

        XposedBridge.log("Eskukap: Loaded " + lpparam.packageName);

        try {
            XposedHelpers.findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "acquireLatestImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Image image = (Image) param.getResult();
                        if (image == null) return;

                        // ---------------- FORMAT + PLANES LOG ----------------
                        int format = image.getFormat();
                        int planesCount = image.getPlanes().length;
                        XposedBridge.log("Eskukap: format=" + format + " planes=" + planesCount);

                        if (format != ImageFormat.YUV_420_888 || planesCount < 3) {
                            XposedBridge.log("Eskukap: not YUV420 or planes<3 → skip frame");
                            image.close();
                            return;
                        }
                        // -----------------------------------------------------

                        image.close(); // пока только тест логов
                    }
                }
            );

        } catch (Throwable e) {
            XposedBridge.log("Eskukap ERROR: " + e);
        }
    }
}
