package com.gaba.eskukap;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String IMG_PATH = "/data/local/tmp/eskukap_fake.jpg";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if(!lpparam.packageName.equals("ru.yandex.taximeter")) return;
        XposedBridge.log("Eskukap: Loaded ru.yandex.taximeter");

        // Hook ImageReader.acquireNextImage()
        XposedHelpers.findAndHookMethod(ImageReader.class, "acquireNextImage", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Image image = (Image) param.getResult();
                if(image == null) return;

                int format = image.getFormat();
                int width  = image.getWidth();
                int height = image.getHeight();

                XposedBridge.log("Eskukap: Frame format=" + format + " size=" + width + "x" + height);

                // Разрешаем JPEG + YUV
                if(format != ImageFormat.JPEG && format != ImageFormat.YUV_420_888){
                    XposedBridge.log("Eskukap: Skip, unsupported format");
                    return;
                }

                File file = new File(IMG_PATH);
                if(!file.exists()){
                    XposedBridge.log("Eskukap: fake.jpg NOT FOUND at " + IMG_PATH);
                    return;
                }

                // ---- JPEG подмена ----
                if(format == ImageFormat.JPEG){
                    XposedBridge.log("Eskukap: JPEG detected → replace...");

                    byte[] fake = Files.readAllBytes(file.toPath());
                    Image.Plane[] p = image.getPlanes();
                    ByteBuffer buf = p[0].getBuffer();
                    buf.clear();
                    buf.put(fake);

                    XposedBridge.log("Eskukap: JPEG replaced OK");
                    return;
                }

                // ---- YUV подмена ----
                if(format == ImageFormat.YUV_420_888){
                    XposedBridge.log("Eskukap: YUV detected → replace...");

                    byte[] fake = Files.readAllBytes(file.toPath());
                    Image.Plane[] planes = image.getPlanes();
                    planes[0].getBuffer().put(fake,0,Math.min(fake.length,planes[0].getBuffer().remaining()));

                    XposedBridge.log("Eskukap: YUV replaced OK");
                }
            }
        });
    }
}
