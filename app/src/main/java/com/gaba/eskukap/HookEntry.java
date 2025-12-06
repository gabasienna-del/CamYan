package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;

import androidx.camera.core.ImageProxy;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // LOG CAMERA
        XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
        );

        // ---------- CameraX ANALYZER HOOK ----------
        try {
            Class<?> analyzer = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader
            );
            Class<?> imageProxy = XposedHelpers.findClass(
                    "androidx.camera.core.ImageProxy",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    analyzer,
                    "analyze",
                    imageProxy,
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {

                            ImageProxy img = (ImageProxy) param.args[0];
                            int w = img.getWidth(), h = img.getHeight();

                            byte[] nv21 = yuv420ToNv21(img);
                            if(nv21 == null) return;

                            // NV21 → JPEG
                            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, w, h, null);
                            ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, w, h), 95, jpegStream);
                            byte[] jpegBytes = jpegStream.toByteArray();

                            // JPEG → Bitmap
                            Bitmap bmp = BitmapFactory.decodeByteArray(jpegBytes,0,jpegBytes.length);
                            if(bmp == null) return;

                            // ███▼ МАСШТАБИРОВАНИЕ ДО 1280×720 ▼███
                            Bitmap scaled = Bitmap.createScaledBitmap(bmp, 1280, 720, true);

                            // Bitmap → JPEG
                            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out2);
                            byte[] jpeg720 = out2.toByteArray();

                            XposedBridge.log("EskukapHook: Resized to 1280x720, size="+jpeg720.length);

                            // ----- если нужно подменять -----
                            // byte[] yuv720 = JpegYuvPipeline.jpegToYuv420(jpeg720,new int[]{1280,720});
                            // JpegYuvPipeline.pushYuvToImageReader(reader,yuv720,1280,720);
                        }
                    }
            );

        } catch(Exception e){
            XposedBridge.log("EskukapHook: CameraX hook fail "+e);
        }
    }

    // ███████ YUV420 → NV21 (для JPEG) ███████
    private static byte[] yuv420ToNv21(ImageProxy image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer y = planes[0].getBuffer();
        ByteBuffer u = planes[1].getBuffer();
        ByteBuffer v = planes[2].getBuffer();

        int yStride = planes[0].getRowStride();
        int uvStride = planes[1].getRowStride();
        int uvPixStride = planes[1].getPixelStride();

        byte[] nv21 = new byte[width * height * 3 / 2];

        // ---- copy Y ----
        int pos = 0;
        byte[] row = new byte[yStride];
        for (int i = 0; i < height; i++) {
            y.position(i * yStride);
            y.get(row, 0, yStride);
            System.arraycopy(row, 0, nv21, pos, width);
            pos += width;
        }

        // ---- copy UV as VU ----
        int uvPos = width * height;
        byte[] uRow = new byte[uvStride];
        byte[] vRow = new byte[uvStride];

        for (int i = 0; i < height / 2; i++) {
            int start = i * uvStride;
            u.position(start);
            v.position(start);
            u.get(uRow,0,uvStride);
            v.get(vRow,0,uvStride);

            for(int j=0; j<width/2; j++){
                int idx = j * uvPixStride;
                nv21[uvPos++] = vRow[idx];
                nv21[uvPos++] = uRow[idx];
            }
        }
        return nv21;
    }
}
