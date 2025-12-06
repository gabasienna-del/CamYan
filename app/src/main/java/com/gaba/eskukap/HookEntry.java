package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.Image;
import android.media.MediaCodec;
import android.os.Handler;
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

        // ---- Camera2 openCamera ----
        try {
            XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                Handler.class,
                new XC_MethodHook() {
                    @Override protected void beforeHookedMethod(MethodHookParam param){
                        XposedBridge.log("EskukapHook: Camera open -> " + param.args[0]);
                    }
                    @Override protected void afterHookedMethod(MethodHookParam param){
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
            );
        } catch (Throwable e){XposedBridge.log("Camera2 hook error " + e);}

        // ---- Camera2 CaptureCallback ----
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.camera2.CameraCaptureSession$CaptureCallback",
                lpparam.classLoader,
                "onCaptureCompleted",
                CameraCaptureSession.class,
                CaptureRequest.class,
                TotalCaptureResult.class,
                new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam param){
                        XposedBridge.log("EskukapHook: Camera2 frame completed");
                    }
                }
            );
        } catch (Throwable e){XposedBridge.log("CaptureCallback hook error " + e);}

        // ---- CameraX Analyzer.analyze ----
        try {
            Class<?> analyzerClass = XposedHelpers.findClass(
                "androidx.camera.core.ImageAnalysis$Analyzer", lpparam.classLoader);

            XposedBridge.log("EskukapHook: CameraX Analyzer found!");

            XposedHelpers.findAndHookMethod(
                analyzerClass,
                "analyze",
                "androidx.camera.core.ImageProxy",
                new XC_MethodHook(){
                    @Override protected void beforeHookedMethod(MethodHookParam param){
                        Log.i("EskukapHook","CameraX Analyzer frame");
                    }
                }
            );
        } catch (Throwable e){
            XposedBridge.log("EskukapHook: CameraX Analyzer NOT found");
        }

        // ---- MediaCodec queueInputBuffer ----
        try{
            XposedHelpers.findAndHookMethod(
                MediaCodec.class,
                "queueInputBuffer",
                int.class,int.class,int.class,long.class,int.class,
                new XC_MethodHook(){
                    @Override protected void beforeHookedMethod(MethodHookParam p){
                        XposedBridge.log("EskukapHook: MediaCodec input buffer");
                    }
                }
            );
        } catch(Throwable e){XposedBridge.log("MediaCodec hook error " + e);}

        // ==== ImageReader Hooks (без image.close) ====

        try{
            XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "acquireLatestImage",
                new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam p) throws Throwable {
                        Image img = (Image)p.getResult();
                        if(img == null) return;

                        XposedBridge.log("EskukapHook: acquireLatestImage -> "+
                                img.getWidth()+"x"+img.getHeight());

                        // ❗Кадр остаётся открытым, доступен для YUV подмены
                    }
                }
            );
        }catch(Throwable e){XposedBridge.log("acquireLatestImage hook error "+e);}

        try{
            XposedHelpers.findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "acquireNextImage",
                new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam p) throws Throwable {
                        Image img = (Image)p.getResult();
                        if(img == null) return;

                        XposedBridge.log("EskukapHook: acquireNextImage -> "+
                                img.getWidth()+"x"+img.getHeight());

                        // ❗Сохраняем кадр — доступен для обработки
                    }
                }
            );
        }catch(Throwable e){XposedBridge.log("acquireNextImage hook error "+e);}

    }
}
