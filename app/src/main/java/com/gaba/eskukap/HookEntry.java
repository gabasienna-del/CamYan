package com.gaba.eskukap;

import android.media.Image;
import android.graphics.ImageFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import androidx.camera.core.ImageProxy;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    // конвертация ByteBuffer → byte[]
    private static byte[] getBytes(ByteBuffer buffer){
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    // сохранение YUV3 плоскостей в файл
    private static void saveYUV(ImageProxy proxy) {
        try {
            Image image = proxy.getImage();
            if (image == null) return;

            Image.Plane[] p = image.getPlanes();
            if (p.length < 3) return;

            File dir = new File("/sdcard/Eskukap/");
            dir.mkdirs();
            File file = new File(dir, "frame_" + System.currentTimeMillis() + ".yuv");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getBytes(p[0].getBuffer()));   // Y
            fos.write(getBytes(p[1].getBuffer()));   // U
            fos.write(getBytes(p[2].getBuffer()));   // V
            fos.close();

            XposedBridge.log("Eskukap: YUV saved -> " + file.getAbsolutePath());

        } catch (Throwable e) {
            XposedBridge.log("Eskukap ERROR saveYUV -> " + e);
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

            XposedBridge.log("Eskukap: CameraX Analyzer FOUND");

            XposedHelpers.findAndHookMethod(
                    analyzer,
                    "analyze",
                    ImageProxy.class,                        // <--- фикс
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            ImageProxy proxy = (ImageProxy) param.args[0];
                            saveYUV(proxy);
                        }
                    });

            XposedBridge.log("Eskukap: Hook installed successfully");

        } catch (Throwable e) {
            XposedBridge.log("Eskukap FAIL hook CameraX -> " + e);
        }
    }
}
