package com.gaba.eskukap.hook;

import android.hardware.Camera;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Лог запуска
        XposedBridge.log("CamYan: loaded in " + lpparam.packageName);

        // ---------------- Camera1 TakePicture ----------------
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera",
                lpparam.classLoader,
                "takePicture",
                Camera.ShutterCallback.class,
                Camera.PictureCallback.class,
                Camera.PictureCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: Camera1 takePicture AFTER");
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("CamYan ERR Camera1: " + e.getMessage());
        }

        // ---------------- Camera2 Capture ----------------
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.camera2.CameraCaptureSession",
                lpparam.classLoader,
                "capture",
                CaptureRequest.class,
                CaptureCallback.class,
                Handler.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("CamYan: Camera2 capture AFTER");
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("CamYan ERR Camera2: " + e.getMessage());
        }

        // Intercept JPEG through ImageReader
        try {
            XposedHelpers.findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "newInstance",
                int.class, int.class, int.class, int.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Object result = XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

                        XposedHelpers.findAndHookMethod(result.getClass(), "acquireLatestImage", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam p) throws Throwable {
                                Image img = (Image) p.getResult();
                                if (img != null && img.getFormat() == 256) { // JPEG
                                    saveImage(img);
                                    XposedBridge.log("CamYan: JPEG intercepted and saved 🚀");
                                }
                            }
                        });
                        return result;
                    }
                }
            );
        } catch (Throwable e) { XposedBridge.log("CamYan Hook ImageReader ERR: " + e.getMessage()); }
    }

    private void saveImage(Image image) {
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/CamYan/last.jpg");
            f.getParentFile().mkdirs();

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bytes);
            fos.close();

            XposedBridge.log("CamYan: saved → " + f.getAbsolutePath());
        } catch (Exception e) {
            XposedBridge.log("CamYan Save ERR: " + e.getMessage());
        }
    }
}
