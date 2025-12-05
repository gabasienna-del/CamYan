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

    // Путь, куда SettingsActivity кладёт картинку
    private static final String FAKE_PATH =
            "/storage/emulated/0/Pictures/CamYan/fake.jpg";

    public static void init(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!lpparam.packageName.equals("com.vkontakte.android")) return;

        XposedBridge.log("FakeImageHook: VK detected, hooking ImageReader");

        try {
            XposedHelpers.findAndHookConstructor(
                    ImageReader.class,
                    int.class, int.class, int.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            ImageReader reader = (ImageReader) param.thisObject;

                            XposedBridge.log("FakeImageHook: ImageReader created, attaching listener");

                            reader.setOnImageAvailableListener(imgReader -> {
                                Image image = null;
                                try {
                                    image = imgReader.acquireLatestImage();
                                    if (image == null) return;

                                    Bitmap fake = BitmapFactory.decodeFile(FAKE_PATH);
                                    if (fake == null) {
                                        XposedBridge.log("FakeImageHook: fake image not found at " + FAKE_PATH);
                                        return;
                                    }

                                    Image.Plane[] planes = image.getPlanes();
                                    if (planes.length > 0) {
                                        ByteBuffer buffer = planes[0].getBuffer();
                                        byte[] bytes = bitmapToJpeg(fake);

                                        if (bytes != null && bytes.length <= buffer.capacity()) {
                                            buffer.rewind();
                                            buffer.put(bytes);
                                            XposedBridge.log("FakeImageHook: IMAGE OVERRIDDEN SUCCESS");
                                        } else {
                                            XposedBridge.log("FakeImageHook: buffer too small for fake image");
                                        }
                                    }

                                } catch (Throwable e) {
                                    XposedBridge.log("FakeImageHook ERROR: " + e);
                                } finally {
                                    if (image != null) image.close();
                                }
                            }, null);
                        }
                    });

        } catch (Throwable e) {
            XposedBridge.log("FakeImageHook FAIL: " + e);
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
