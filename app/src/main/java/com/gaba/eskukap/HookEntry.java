package com.gaba.eskukap;

import android.hardware.camera2.*;
import android.media.*;
import android.os.Handler;
import android.util.Log;
import android.media.ImageReader;
import android.media.Image;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;
        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // ---- CameraManager.openCamera ----
        try {
            XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                Handler.class,
                new XC_MethodHook() {
                    @Override protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log("EskukapHook: Camera open -> " + param.args[0]);
                    }
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
            );
        }catch(Exception e){XposedBridge.log("openCamera hook err "+e);}

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
                        XposedBridge.log("EskukapHook: Capture frame Camera2");
                    }
                }
            );
        }catch(Exception e){XposedBridge.log("CaptureCallback err "+e);}

        // ---- CameraX Analyzer.analyze ----
        try {
            Class<?> analyzerClass =
                XposedHelpers.findClass("androidx.camera.core.ImageAnalysis$Analyzer", lpparam.classLoader);

            XposedBridge.log("EskukapHook: CameraX Analyzer found");

            XposedHelpers.findAndHookMethod(
                analyzerClass,
                "analyze",
                "androidx.camera.core.ImageProxy",
                new XC_MethodHook(){
                    @Override protected void beforeHookedMethod(MethodHookParam param){
                        Log.i("EskukapHook","CameraX analyzer frame");
                    }
                }
            );
        }catch(Exception e){
            XposedBridge.log("EskukapHook: CameraX Analyzer not found");
        }

        // ---- MediaCodec queueInputBuffer ----
        try {
            XposedHelpers.findAndHookMethod(
                MediaCodec.class,
                "queueInputBuffer",
                int.class,int.class,int.class,long.class,int.class,
                new XC_MethodHook(){
                    @Override protected void beforeHookedMethod(MethodHookParam p){
                        XposedBridge.log("EskukapHook: MediaCodec input");
                    }
                }
            );
        }catch(Exception e){XposedBridge.log("queueInputBuffer err "+e);}

        // ---- ImageReader acquireLatestImage (без close) ----
        try {
            XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "acquireLatestImage",
                new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam param){
                        Image img=(Image)param.getResult();
                        if(img==null)return;
                        XposedBridge.log("EskukapHook: LatestImage "+img.getWidth()+"x"+img.getHeight());
                        // img не закрывается
                    }
                }
            );
        }catch(Exception e){XposedBridge.log("acquireLatestImage err "+e);}

        // ---- ImageReader acquireNextImage (без close) ----
        try {
            XposedHelpers.findAndHookMethod(
                "android.media.ImageReader",
                lpparam.classLoader,
                "acquireNextImage",
                new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam param){
                        Image img=(Image)param.getResult();
                        if(img==null)return;
                        XposedBridge.log("EskukapHook: NextImage "+img.getWidth()+"x"+img.getHeight());
                        // img не закрывается
                    }
                }
            );
        }catch(Exception e){XposedBridge.log("acquireNextImage err "+e);}
    }
}
