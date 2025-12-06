package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.media.Image;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        // Хукаем только таксометр
        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // ---- 1. Хук открытия камеры (CameraManager.openCamera) ----
        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    android.os.Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera open request -> " + param.args[0]);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera opened");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 openCamera HOOK ERROR: " + e);
        }

        // ---- 2. Camera2: перехват кадров через CaptureCallback.onCaptureCompleted ----
        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraCaptureSession$CaptureCallback",
                    lpparam.classLoader,
                    "onCaptureCompleted",
                    android.hardware.camera2.CameraCaptureSession.class,
                    android.hardware.camera2.CaptureRequest.class,
                    android.hardware.camera2.TotalCaptureResult.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("EskukapHook: Capture completed — frame available (Camera2)");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 CaptureCallback HOOK ERROR: " + e);
        }

        // ---- 3. CameraX: Analyzer.analyze(ImageProxy) ----
        // ВАЖНО: используем имена классов строками, чтобы не тянуть androidx зависимость в модуль
        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object imageProxy = param.args[0];
                            // Тут YUV кадр CameraX
                            XposedBridge.log("EskukapHook: CameraX analyze() frame -> " + imageProxy);
                            // TODO: подмена содержимого через ImageProxy, если нужно
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERAX Analyzer HOOK ERROR: " + e);
        }

        // ---- 4. MediaCodec.queueInputBuffer (видеопоток) ----
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.MediaCodec",
                    lpparam.classLoader,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int index = (int) param.args[0];
                            int offset = (int) param.args[1];
                            int size   = (int) param.args[2];
                            long pts   = (long) param.args[3];

                            XposedBridge.log("EskukapHook: MediaCodec.queueInputBuffer idx=" +
                                    index + " off=" + offset + " size=" + size + " pts=" + pts);
                            // TODO: здесь можно менять содержимое ByteBuffer перед кодеком
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook MEDIACODEC queueInputBuffer HOOK ERROR: " + e);
        }

        // ---- 5. ImageReader: хук acquireLatestImage() + автомасштабирование до 1280x720 ----
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image image = (Image) param.getResult();

                            if (image != null) {
                                int w = image.getWidth();
                                int h = image.getHeight();

                                XposedBridge.log("EskukapHook: ImageReader frame " + w + "x" + h
                                        + " fmt=" + image.getFormat());

                                try {
                                    if (image.getFormat() == ImageFormat.YUV_420_888) {
                                        byte[] nv21 = yuv420ToNV21(image);
                                        if (nv21 == null) {
                                            XposedBridge.log("EskukapHook: yuv420ToNV21 returned null (unsupported strides)");
                                            return;
                                        }

                                        byte[] scaledJpeg = scaleToJpeg1280x720(nv21, w, h);
                                        if (scaledJpeg != null) {
                                            XposedBridge.log("EskukapHook: Scaled to 1280x720, jpegSize=" + scaledJpeg.length);
                                            // TODO: здесь потом сделаем обратное JPEG -> YUV и подмену кадра в пайплайне
                                        } else {
                                            XposedBridge.log("EskukapHook: scaleToJpeg1280x720 failed");
                                        }
                                    } else {
                                        XposedBridge.log("EskukapHook: Unsupported image format for resize: " + image.getFormat());
                                    }
                                } catch (Throwable t) {
                                    XposedBridge.log("EskukapHook: resize 1280x720 error: " + t);
                                }

                                // ПО ТВОЕЙ ПРОСЬБЕ: image.close() не вызываем, чтобы не ломать пайплайн
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGEREADER acquireLatestImage HOOK ERROR: " + e);
        }
    }

    // ====== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РЕСАЙЗА 1280x720 ======

    // Простая конвертация YUV_420_888 -> NV21
    // Работает, когда strides нормальные (pixelStride/rowStride типичные).
    private static byte[] yuv420ToNV21(Image image) {
        try {
            Image.Plane[] planes = image.getPlanes();
            int width = image.getWidth();
            int height = image.getHeight();

            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int yRowStride = planes[0].getRowStride();
            int yPixelStride = planes[0].getPixelStride();

            int uRowStride = planes[1].getRowStride();
            int uPixelStride = planes[1].getPixelStride();

            int vRowStride = planes[2].getRowStride();
            int vPixelStride = planes[2].getPixelStride();

            // Поддерживаем только самый частый кейс (pixelStride/rowStride как у обычного NV21)
            if (yPixelStride != 1 || uPixelStride != 2 || vPixelStride != 2) {
                XposedBridge.log("EskukapHook: Unsupported pixelStride Y=" + yPixelStride +
                        " U=" + uPixelStride + " V=" + vPixelStride);
                return null;
            }

            byte[] nv21 = new byte[width * height * 3 / 2];
            int pos = 0;

            // Копируем Y
            for (int row = 0; row < height; row++) {
                int yRowStart = row * yRowStride;
                yBuffer.position(yRowStart);
                yBuffer.get(nv21, pos, width);
                pos += width;
            }

            // UV (VU для NV21)
            int chromaHeight = height / 2;
            for (int row = 0; row < chromaHeight; row++) {
                int uRowStart = row * uRowStride;
                int vRowStart = row * vRowStride;
                for (int col = 0; col < width / 2; col++) {
                    int uIndex = uRowStart + col * uPixelStride;
                    int vIndex = vRowStart + col * vPixelStride;

                    uBuffer.position(uIndex);
                    vBuffer.position(vIndex);

                    byte u = uBuffer.get();
                    byte v = vBuffer.get();

                    nv21[pos++] = v;
                    nv21[pos++] = u;
                }
            }

            return nv21;
        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: yuv420ToNV21 exception: " + t);
            return null;
        }
    }

    // NV21 -> JPEG -> Bitmap -> scale 1280x720 -> JPEG
    private static byte[] scaleToJpeg1280x720(byte[] nv21, int srcWidth, int srcHeight) {
        try {
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, srcWidth, srcHeight, null);
            ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
            boolean ok = yuvImage.compressToJpeg(new Rect(0, 0, srcWidth, srcHeight), 90, jpegOut);
            if (!ok) {
                return null;
            }

            byte[] jpegData = jpegOut.toByteArray();
            Bitmap srcBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            if (srcBitmap == null) {
                return null;
            }

            Bitmap scaled = Bitmap.createScaledBitmap(srcBitmap, 1280, 720, true);

            ByteArrayOutputStream scaledOut = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, scaledOut);

            srcBitmap.recycle();
            scaled.recycle();

            return scaledOut.toByteArray();
        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: scaleToJpeg1280x720 exception: " + t);
            return null;
        }
    }
}

// ========= HARDWAREBUFFER (fmt 256) → Bitmap → scale 1280x720 =========
@androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.Q)
private static android.graphics.Bitmap hardwareBufferToScaledBitmap(Image image) {
    try {
        android.hardware.HardwareBuffer hb = image.getHardwareBuffer();
        if (hb == null) return null;

        // Определение формата (часто RGBA_8888)
        int pixelFormat = android.graphics.PixelFormat.RGBA_8888;

        android.graphics.Bitmap src = android.graphics.Bitmap.wrapHardwareBuffer(hb, pixelFormat);
        if (src == null) return null;

        android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(src, 1280, 720, true);
        return scaled;
    } catch (Throwable t) {
        de.robv.android.xposed.XposedBridge.log("EskukapHook: hardwareBufferToBitmap ERR: " + t);
        return null;
    }
}
// ===== ВСТАВЛЯЕМ ВНУТРИ afterHookedMethod К image FORMAT=256 =====
// ПОИСК: if (image.getFormat() == ImageFormat.YUV_420_888) { ... }
// ПОСЛЕ НЕГО ДОБАВЬ ЭТО:

if (image.getFormat() == 256) {  // PRIVATE → HardwareBuffer stream
    android.graphics.Bitmap bmp = hardwareBufferToScaledBitmap(image);
    if (bmp != null) {
        de.robv.android.xposed.XposedBridge.log("EskukapHook: PRIVATE frame scaled to 1280x720 OK");
        // TODO: здесь позже подменим кадр JPEG/YUV
    } else {
        de.robv.android.xposed.XposedBridge.log("EskukapHook: PRIVATE convert failed");
    }
}
