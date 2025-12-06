package com.gaba.eskukap;

import android.util.Log;
import java.io.File;

public class FakeCameraHook {

    private static final String TAG = "EskukapCamera";

    // Тест: проверяем работу без камеры
    public static void test() {
        Log.i(TAG, "FakeCameraHook loaded OK!");
        File img = new File("/sdcard/test.jpg");
        Log.i(TAG, "Test file: " + img.exists());
    }
}
