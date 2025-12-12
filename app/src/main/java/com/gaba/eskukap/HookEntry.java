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
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.gaba.eskukap.security.AntiDebug;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Объединённый HookEntry — поддерживает два режима:
 *  - Для ru.yandex.taximeter: хуки камеры / ImageReader / MediaCodec и масштабирование кадров.
 *  - Для com.gaba.eskukap: инициализация AntiDebug (startup scan + watchdog).
 */
public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "HookEntry";
    private static final String MY_PKG = "com.gaba.eskukap";
    private static final String TAXI_PKG = "ru.yandex.taximeter";
    private static final long WATCHDOG_PERIOD_MS = 5000L;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;

        // --- AntiDebug init for our package ---
        if (MY_PKG.equals(pkg)) {
            Log.i(TAG, "Loaded package (anti-debug): " + pkg);
            XposedBridge.log(TAG + ": loaded " + pkg);

            try {
                try {
                    AntiDebug.Result r = AntiDebug.INSTANCE.scan();
                    if (r != null && r.getSuspicious()) {
                        AntiDebug.INSTANCE.enforceOrDie("startup");
                    }
                } catch (Throwable scanEx) {
                    XposedBridge.log("AntiDebug initial scan failed: " + scanEx);
                    Log.e(TAG, "AntiDebug initial scan failed", scanEx);
                }

                AntiDebug.INSTANCE.startWatchdog(WATCHDOG_PERIOD_MS);

            } catch (Throwable t) {
                XposedBridge.log(t);
                Log.e(TAG, "Failed to initialize AntiDebug", t);
            }
        }

        // --- Camera & media hooks for the taxi app ---
        if (!TAXI_PKG.equals(pkg)) return;

        XposedBridge.log("EskukapHook: Loaded " + TAXI_PKG);

        // 1. CameraManager.openCamera
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

        // 2. Camera2 CaptureCallback.onCaptureCompleted
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
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Capture completed — frame available (Camera2)");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERA2 CaptureCallback HOOK ERROR: " + e);
        }

        // 3. CameraX Analyzer.analyze
        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: CameraX analyze() frame -> " + param.args[0]);
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook CAMERAX Analyzer HOOK ERROR: " + e);
        }

        // 4. MediaCodec.queueInputBuffer
        try {
            XposedHelpers.findAndHookMethod(
                    "android.media.MediaCodec",
                    lpparam.classLoader,
                    "queueInputBuffer",
                    int.class, int.class, int.class, long.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: MediaCodec.queueInputBuffer idx=" +
                                    param.args[0] + " off=" + param.args[1] +
                                    " size=" + param.args[2] + " pts=" + param.args[3]);
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook MEDIACODEC queueInputBuffer HOOK ERROR: " + e);
        }

        // 5. ImageReader.acquireLatestImage
        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Image image = (Image) param.getResult();
                            if (image == null) return;

                            int w = image.getWidth();
                            int h = image.getHeight();

                            XposedBridge.log("EskukapHook: ImageReader frame " + w + "x" + h +
                                    " fmt=" + image.getFormat());

                            try {
                                if (image.getFormat() == ImageFormat.YUV_420_888) {
                                    byte[] nv21 = yuv420ToNV21(image);
                                    if (nv21 == null) {
                                        XposedBridge.log("EskukapHook: yuv420ToNV21 returned null");
                                        return;
                                    }

                                    byte[] scaledJpeg = scaleToJpeg1280x720(nv21, w, h);
                                    if (scaledJpeg != null) {
                                        XposedBridge.log("EskukapHook: Scaled to 1280x720, jpegSize=" + scaledJpeg.length);
                                    }
                                }
                            } catch (Throwable t) {
                                XposedBridge.log("EskukapHook: resize 1280x720 error: " + t);
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook IMAGEREADER acquireLatestImage HOOK ERROR: " + e);
        }
    }

    // ==== YUV420 → NV21 ====
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

            if (yPixelStride != 1 || uPixelStride != 2 || vPixelStride != 2) {
                XposedBridge.log("EskukapHook: Unsupported pixel stride");
                return null;
            }

            byte[] nv21 = new byte[width * height * 3 / 2];
            int pos = 0;

            // Y
            for (int row = 0; row < height; row++) {
                yBuffer.position(row * yRowStride);
                yBuffer.get(nv21, pos, width);
                pos += width;
            }

            // VU
            int chromaH = height / 2;
            for (int row = 0; row < chromaH; row++) {
                int uRow = row * uRowStride;
                int vRow = row * vRowStride;
                for (int col = 0; col < width / 2; col++) {
                    uBuffer.position(uRow + col * uPixelStride);
                    vBuffer.position(vRow + col * vPixelStride);
                    nv21[pos++] = vBuffer.get();
                    nv21[pos++] = uBuffer.get();
                }
            }

            return nv21;

        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: yuv420ToNV21 exception: " + t);
            return null;
        }
    }

    // ==== NV21 → JPEG → Bitmap → scale 1280x720 ====
    private static byte[] scaleToJpeg1280x720(byte[] nv21, int w, int h) {
        try {
            YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
            if (!yuv.compressToJpeg(new Rect(0, 0, w, h), 90, jpegOut)) return null;

            Bitmap src = BitmapFactory.decodeByteArray(jpegOut.toByteArray(), 0, jpegOut.size());
            if (src == null) return null;

            Bitmap scaled = Bitmap.createScaledBitmap(src, 1280, 720, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);

            src.recycle();
            scaled.recycle();

            return out.toByteArray();

        } catch (Throwable t) {
            XposedBridge.log("EskukapHook: scaleToJpeg1280x720 exception: " + t);
            return null;
        }
    }
}
