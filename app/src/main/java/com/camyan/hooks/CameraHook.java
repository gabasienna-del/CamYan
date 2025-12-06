package com.camyan.hooks;

import android.media.Image;
import android.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.robv.android.xposed.XC_MethodHook;

public class CameraHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Image img = (Image) param.getResult();
        if (img == null) return;

        Log.i("CameraHook", "Replacing frame...");

        // Загружаем подготовленный файл вместо камеры
        byte[] jpegBytes = Files.readAllBytes(Paths.get("/sdcard/fake.jpg"));

        // Конвертация jpg → Image (пока заглушка)
        Image fake = ImageBytesToImage(jpegBytes, img.getWidth(), img.getHeight());

        if (fake != null) {
            param.setResult(fake);
            Log.i("CameraHook", "Frame swapped ✔");
        } else {
            Log.e("CameraHook", "Swap failed ❌");
        }
    }

    // сюда позже вставим нормальную реализацию
    private Image ImageBytesToImage(byte[] data, int w, int h) {
        return null;
    }
}
