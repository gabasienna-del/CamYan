package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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

        XposedHelpers.findAndHookMethod(ImageReader.class, "acquireNextImage", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Image image = (Image) param.getResult();
                if(image == null) return;

                int format = image.getFormat();
                int w=image.getWidth(), h=image.getHeight();

                XposedBridge.log("Eskukap: Frame format=" + format + " size=" + w + "x" + h);

                if(format != ImageFormat.JPEG) return; // пока меняем только JPEG

                File f = new File(IMG_PATH);
                if(!f.exists()){
                    XposedBridge.log("Eskukap: fake.jpg NOT FOUND at "+IMG_PATH);
                    return;
                }

                // ---- читаем картинку ----
                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f));
                if(bmp==null){
                    XposedBridge.log("Eskukap: decode failed");
                    return;
                }

                // ---- сжимаем под размер камеры ----
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% качества — помещается
                byte[] fake = baos.toByteArray();

                Image.Plane p = image.getPlanes()[0];
                ByteBuffer buf = p.getBuffer();
                buf.clear();

                if(fake.length > buf.remaining()){
                    XposedBridge.log("Eskukap: resized JPEG too big ("+fake.length+" > "+buf.remaining()+") try lower quality");
                    return;
                }

                buf.put(fake);
                XposedBridge.log("Eskukap: *** JPEG REPLACED OK ***");
            }
        });
    }
}
