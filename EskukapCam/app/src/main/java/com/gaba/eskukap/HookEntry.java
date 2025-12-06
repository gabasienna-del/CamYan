package com.gaba.eskukap;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import androidx.camera.core.ImageProxy;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.nio.ByteBuffer;

public class HookEntry implements IXposedHookLoadPackage {

    private static final int TARGET_W = 1280;
    private static final int TARGET_H = 720;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;

        XposedBridge.log("EskukapHook: Loaded → ru.yandex.taximeter");

        // Camera2 open log
        XposedHelpers.findAndHookMethod(
                CameraManager.class,
                "openCamera",
                String.class,
                CameraDevice.StateCallback.class,
                android.os.Handler.class,
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        XposedBridge.log("EskukapHook: Camera opened");
                    }
                }
        );

        // Hook CameraX Analyzer.analyze(ImageProxy)
        try {
            Class<?> analyzer = XposedHelpers.findClass(
                    "androidx.camera.core.ImageAnalysis$Analyzer", lpparam.classLoader);
            Class<?> imageProxy = XposedHelpers.findClass(
                    "androidx.camera.core.ImageProxy", lpparam.classLoader);

            XposedBridge.log("EskukapHook: CameraX Analyzer found — hooking");

            XposedHelpers.findAndHookMethod(
                    analyzer,
                    "analyze",
                    imageProxy,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {

                            try {
                                ImageProxy img = (ImageProxy) param.args[0];
                                if (img == null) return;

                                int w = img.getWidth();
                                int h = img.getHeight();

                                byte[] yuv = yuv420ToNV21(img);
                                byte[] resized = resizeNV21(yuv, w, h, TARGET_W, TARGET_H);

                                XposedBridge.log("EskukapHook: Frame " + w + "x" + h +
                                        " → resized 1280x720, bytes=" + resized.length);

                                // >>> здесь можно сохранять или подменять кадр <<<
                                // FileHelper.save(resized);
                                // JpegYuvPipeline.pushYuvToImageReader(reader,resized,1280,720);

                            } catch (Throwable e) {
                                XposedBridge.log("EskukapHook: analyze error " + e);
                            }
                        }
                    }
            );

        } catch (Throwable e) {
            XposedBridge.log("EskukapHook: Analyzer hook FAIL → " + e);
        }
    }

    // ===========================
    // YUV_420_888 → NV21
    // ===========================
    private static byte[] yuv420ToNV21(ImageProxy img) {

        int w = img.getWidth(), h = img.getHeight();
        byte[] out = new byte[w*h*3/2];

        ByteBuffer Y = img.getPlanes()[0].getBuffer();
        ByteBuffer U = img.getPlanes()[1].getBuffer();
        ByteBuffer V = img.getPlanes()[2].getBuffer();

        int yStride = img.getPlanes()[0].getRowStride();
        int uvStride = img.getPlanes()[1].getRowStride();
        int uvPix = img.getPlanes()[1].getPixelStride();

        // Y copy
        byte[] row = new byte[yStride];
        int pos = 0;
        for(int i=0;i<h;i++){
            Y.position(i*yStride);
            Y.get(row,0,yStride);
            System.arraycopy(row,0,out,pos,w);
            pos+=w;
        }

        // UV copy (NV21 format = VU interleaved)
        int uvH = h/2, uvW=w/2;
        byte[] uRow = new byte[uvStride];
        byte[] vRow = new byte[uvStride];
        int uvPos = w*h;

        for(int i=0;i<uvH;i++){
            U.position(i*uvStride);
            V.position(i*uvStride);
            U.get(uRow,0,uvStride);
            V.get(vRow,0,uvStride);

            for(int j=0;j<uvW;j++){
                int idx=j*uvPix;
                out[uvPos++] = vRow[idx]; // V
                out[uvPos++] = uRow[idx]; // U
            }
        }
        return out;
    }

    // ===========================
    // Fast NV21 Resize → 1280x720 (nearest-neighbour)
    // ===========================
    private static byte[] resizeNV21(byte[] src, int sw, int sh, int dw, int dh){

        byte[] dst = new byte[dw*dh*3/2];

        float sx = (float)sw/dw;
        float sy = (float)sh/dh;

        // Y-plane
        for(int y=0;y<dh;y++){
            int syy=(int)(y*sy)*sw;
            int dyy=y*dw;
            for(int x=0;x<dw;x++){
                dst[dyy+x]=src[syy+(int)(x*sx)];
            }
        }

        // UV-plane
        int srcUV=sw*sh, dstUV=dw*dh, uvH=dh/2;
        float sxuv=sx, syuv=sy/2;

        for(int y=0;y<uvH;y++){
            int syy=(int)(y*syuv)*sw;
            int dyy=y*dw;
            for(int x=0;x<dw;x+=2){
                int s=(int)(x*sxuv);
                dst[dstUV+dyy+x]   = src[srcUV+syy+s];   // V
                dst[dstUV+dyy+x+1] = src[srcUV+syy+s+1]; // U
            }
        }
        return dst;
    }
}
