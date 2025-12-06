package com.gaba.eskukap;

import android.media.Image;
import android.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;
import de.robv.android.xposed.XC_MethodHook;

public class FakeImageHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

        Image img = (Image) param.getResult();
        if (img == null) return;

        Log.i("FakeImageHook", "Replacing frame...");

        // Загружаем заранее подготовленное фото из /sdcard/fake.jpg
        byte[] jpegBytes = Files.readAllBytes(Paths.get("/sdcard/fake.jpg"));

        // Создаём фейковый Image через MediaCodec surface input (Нужно реализовать метод ниже)
        Image fake = ImageBytesToImage(jpegBytes, img.getWidth(), img.getHeight());

        if (fake != null) {
            param.setResult(fake);
            Log.i("FakeImageHook","Frame swapped ✔");
        } else {
            Log.e("FakeImageHook","Swap failed");
        }
    }

    // TODO — нужно реализовать создание Image из byte[] (это сложная часть)
    private Image ImageBytesToImage(byte[] data, int width, int height) {
        Log.e("FakeImageHook", "ImageBytesToImage() not implemented yet");
        return null;
    }
}
