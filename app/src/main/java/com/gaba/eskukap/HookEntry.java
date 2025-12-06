package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.media.Image;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

// нужные импорты для jpg->yuv
import com.gaba.eskukap.JpegYuvPipeline;
import com.gaba.eskukap.FileHelper;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        try {
            XposedHelpers.findAndHookMethod(
                    CameraManager.class,
                    "openCamera",
                    String.class,
                    CameraDevice.StateCallback.class,
                    android.os.Handler.class,
                    new XC_MethodHook() {
                        protected void beforeHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera open request -> " + param.args[0]);
                        }
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log("EskukapHook: Camera opened");
                        }
                    }
            );
        } catch (Throwable e){ XposedBridge.log("openCamera hook err "+e); }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.hardware.camera2.CameraCaptureSession$CaptureCallback",
                    lpparam.classLoader,
                    "onCaptureCompleted",
                    android.hardware.camera2.CameraCaptureSession.class,
                    android.hardware.camera2.CaptureRequest.class,
                    android.hardware.camera2.TotalCaptureResult.class,
                    new XC_MethodHook(){
                        protected void afterHookedMethod(MethodHookParam param){
                            XposedBridge.log("EskukapHook: Capture complete Camera2");
                        }
                    }
            );
        } catch(Throwable e){ XposedBridge.log("capture hook err "+e); }

        try {
            XposedHelpers.findAndHookMethod(
                    "androidx.camera.core.ImageAnalysis$Analyzer",
                    lpparam.classLoader,
                    "analyze",
                    "androidx.camera.core.ImageProxy",
                    new XC_MethodHook(){
                        protected void afterHookedMethod(MethodHookParam param){
                            XposedBridge.log("EskukapHook: CameraX analyze()");
                        }
                    }
            );
        } catch(Throwable e){ XposedBridge.log("Analyzer hook err "+e); }

        try {
            XposedHelpers.findAndHookMethod(
                    ImageReader.class,
                    "acquireLatestImage",
                    new XC_MethodHook(){
                        protected void afterHookedMethod(MethodHookParam param){
                            Image img = (Image) param.getResult();
                            if(img == null) return;

                            XposedBridge.log("EskukapHook: ImageReader frame "+img.getWidth()+"x"+img.getHeight());

                            // ==== JPEG → YUV подключён аккуратно ====
                            try{
                                String jpg = "/sdcard/eskukap/frame.jpg";
                                byte[] data = FileHelper.readFile(jpg);

                                if(data != null){
                                    int[] wh = new int[2];
                                    byte[] yuv = JpegYuvPipeline.jpegToYuv420(data, wh);

                                    if(yuv!=null){
                                        XposedBridge.log("EskukapHook: JPG→YUV OK "+wh[0]+"x"+wh[1]+" size="+yuv.length);
                                        // Далее сможем реально заменить кадр в codec или ImageWriter
                                    }
                                }else XposedBridge.log("EskukapHook: JPEG not found");
                            }catch(Throwable e){
                                XposedBridge.log("EskukapHook jpeg->yuv err "+e);
                            }
                        }
                    }
            );
        } catch(Throwable e){ XposedBridge.log("ImageReader hook err "+e); }
    }
}
