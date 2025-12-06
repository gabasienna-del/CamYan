package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.media.Image;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // Camera open log
        XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam p){
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
        );

        // ImageReader acquireLatestImage hook - FRAME REPLACER
        XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "acquireLatestImage",
                new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Image img = (Image) param.getResult();
                        if (img == null) return;

                        int w = img.getWidth();
                        int h = img.getHeight();

                        XposedBridge.log("EskukapHook: Frame " + w + "x" + h);

                        // LOAD JPEG
                        byte[] jpeg = FileHelper.loadJPEG();
                        if(jpeg == null){
                            XposedBridge.log("EskukapHook: ❌ JPEG NOT FOUND");
                            return;
                        }

                        // Convert JPEG → YUV
                        int[] out = new int[2];
                        byte[] yuv = JpegYuvPipeline.jpegToYuv420(jpeg,out);
                        if(yuv == null){
                            XposedBridge.log("EskukapHook: ❌ JPEG to YUV fail");
                            return;
                        }

                        // Push to virtual ImageReader surface (frame injection)
                        JpegYuvPipeline.pushYuvToImageReader((ImageReader)param.thisObject,yuv,out[0],out[1]);

                        XposedBridge.log("EskukapHook: ✅ FRAME REPLACED WITH JPEG");
                    }
                }
        );
    }
}
