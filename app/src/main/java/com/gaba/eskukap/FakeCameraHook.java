package com.gaba.eskukap;

import android.media.Image;
import android.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;
import de.robv.android.xposed.XC_MethodHook;

public class FakeCameraHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        try {
            Image img = (Image) param.getResult();
            if (img == null) return;

            Log.i("FakeCamHook", "Frame intercepted -> trying to replace...");

            byte[] jpegBytes = Files.readAllBytes(Paths.get("/sdcard/fake.jpg"));

            // TODO: конвертация jpegBytes -> Image (нужно дописать ниже)
            // пока просто лог
            Log.i("FakeCamHook", "Loaded /sdcard/fake.jpg (" + jpegBytes.length + " bytes)");

        } catch (Throwable e) {
            Log.e("FakeCamHook", "Error: " + e);
        }
    }
}
