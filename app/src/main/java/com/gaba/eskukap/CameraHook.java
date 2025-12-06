package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import java.io.File;
import java.nio.ByteBuffer;

public class CameraHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Image img = (Image) param.getResult();
        if (img == null) {
            Log.e("EskukapCamera", "No image result");
            return;
        }

        Log.i("EskukapCamera", "Frame intercepted!");

        // ======== путь к подменяемому фото =========
        File fakeFile = new File("/sdcard/fake.jpg");
        if (!fakeFile.exists()) {
            Log.e("EskukapCamera", "fake.jpg not found in /sdcard");
            return;
        }

        Bitmap fake = BitmapFactory.decodeFile(fakeFile.getAbsolutePath());
        if (fake == null) {
            Log.e("EskukapCamera", "Failed to decode fake image");
            return;
        }

        // Замена байтов
        Image.Plane[] planes = img.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        Bitmap resized = Bitmap.createScaledBitmap(fake, img.getWidth(), img.getHeight(), false);
        ByteBuffer fakeBuffer = ByteBuffer.allocate(data.length);
        resized.copyPixelsToBuffer(fakeBuffer);

        buffer.rewind();
        buffer.put(fakeBuffer.array(), 0, data.length);

        Log.i("EskukapCamera", "Frame replaced ✔");
    }
}
