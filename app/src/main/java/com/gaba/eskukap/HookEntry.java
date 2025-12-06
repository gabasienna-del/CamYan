package com.gaba.eskukap;

import android.media.Image;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static byte[] buffer(ByteBuffer buffer){
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private static void saveYUVtoFile(ImageProxy proxy) {
        try {
            Image image = (Image) XposedHelpers.callMethod(proxy, "getImage");
            if (image == null) return;

            Image.Plane[] p = image.getPlanes();
            if (p.length < 3) return;

            File dir = new File("/sdcard/Eskukap/");
            dir.mkdirs();
            File file = new File(dir, "frame_" + System.currentTimeMillis() + ".yuv");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer(p[0].getBuffer())); // Y
            fos.write(buffer(p[1].getBuffer())); // U
            fos.write(buffer(p[2].getBuffer())); // V
            fos.close();

            XposedBridge.log("Eskukap: YUV saved -> " + file.getAbsolutePath());

        } catch (Throwable e) {
            XposedBridge.log("Eskukap ERROR saveYUV " + e);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        try {
            Class<?> analyzer = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader
            );

            XposedBridge.log("Eskukap: CameraX Analyzer hook OK");

            XposedHelpers.findAndHookMethod(
                    analyzer,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object imageProxy = param.args[0];
                            saveYUVtoFile((ImageProxy) imageProxy);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log("Eskukap failed hook CameraX " + e);
        }
    }
}
