package com.gaba.eskukap.hook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FakeImageHook {

    private static final String PKG_VK = "com.vkontakte.android";
    private static final String PKG_TAXI = "ru.yandex.taximeter";

    private static final String FAKE_PATH =
            "/storage/emulated/0/Pictures/CamYan/fake.jpg";

    public static void init(final XC_LoadPackage.LoadPackageParam lpparam) {

        String pkg = lpparam.packageName;

        if (!PKG_VK.equals(pkg) && !PKG_TAXI.equals(pkg)) return;

        XposedBridge.log("FakeImageHook: " + pkg + " detected, hooking ImageReader");

        try {
            // Перехватываем ВСЕ конструкторы ImageReader
            XposedHelpers.findAndHookConstructor(
                    ImageReader.class, new Object[]{XC_MethodHook.class},
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            ImageReader reader = (ImageReader) param.thisObject;

                            XposedBridge.log("FakeImageHook: " + pkg + " ImageReader created");

                            reader.setOnImageAvailableListener(imgReader -> {
                                Image image = null;
                                try {
                                    image = imgReader.acquireLatestImage();
                                    if (image == null) return;

                                    Bitmap fake = BitmapFactory.decodeFile(FAKE_PATH);
                                    if (fake == null) {
                                        XposedBridge.log("FakeImageHook: NO fake.jpg at " + FAKE_PATH);
                                        return;
                                    }

                                    Image.Plane[] planes = image.getPlanes();
                                    if (planes.length > 0) {
                                        ByteBuffer buffer = planes[0].getBuffer();
                                        byte[] bytes = bitmapToJpeg(fake);

                                        if (bytes != null && bytes.length <= buffer.capacity()) {
                                            buffer.rewind();
                                            buffer.put(bytes);
                                            XposedBridge.log("FakeImageHook: IMAGE REPLACED OK in " + pkg);
                                        } else {
                                            XposedBridge.log("FakeImageHook: BUFFER TOO SMALL in " + pkg);
                                        }
                                    }

                                } catch (Throwable e) {
                                    XposedBridge.log("FakeImageHook ERROR (" + pkg + "): " + e);
                                } finally {
                                    if (image != null) image.close();
                                }
                            }, null);
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log("FakeImageHook FAIL HOOK (" + pkg + "): " + e);
        }
    }

    private static byte[] bitmapToJpeg(Bitmap bmp) {
        try {
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            return stream.toByteArray();
        } catch (Throwable e) {
            XposedBridge.log("bitmapToJpeg ERROR: " + e);
            return null;
        }
    }
}
