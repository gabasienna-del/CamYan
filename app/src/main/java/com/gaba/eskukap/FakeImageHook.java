package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;
import java.io.File;

import de.robv.android.xposed.XC_MethodHook;

public class FakeImageHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

        Image img = (Image) param.getResult();
        if (img == null) {
            Log.e("FakeImageHook", "No frame received");
            return;
        }

        Log.i("FakeImageHook", "Frame intercepted → injecting fake image...");

        File file = new File("/sdcard/fake.jpg");
        if (!file.exists()) {
            Log.e("FakeImageHook", "fake.jpg not found in /sdcard");
            return;
        }

        Bitmap fake = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (fake == null) {
            Log.e("FakeImageHook", "decode failed");
            return;
        }

        Bitmap resized = Bitmap.createScaledBitmap(fake, img.getWidth(), img.getHeight(), false);

        Image.Plane[] planes = img.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        byte[] fakeBytes = new byte[buffer.remaining()];
        ByteBuffer tmp = ByteBuffer.wrap(fakeBytes);
        resized.copyPixelsToBuffer(tmp);

        buffer.rewind();
        buffer.put(fakeBytes);

        Log.i("FakeImageHook", "Frame replaced ✔");
    }
}
