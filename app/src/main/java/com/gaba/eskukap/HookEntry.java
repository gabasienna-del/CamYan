package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import java.io.File;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "Eskukap";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        XposedBridge.log(TAG + ": Loaded " + lpparam.packageName);

        // ---------------- ImageReader hook (если приложения использует Camera2) ----------------
        try {
            Class<?> imageReaderClass = XposedHelpers.findClass(
                "android.media.ImageReader", lpparam.classLoader);

            XposedBridge.log(TAG + ": ImageReader hook OK");

            XposedHelpers.findAndHookMethod(imageReaderClass, "acquireLatestImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Image img = (Image) param.getResult();
                        if (img != null) replaceImage(img);
                    }
                });

            XposedHelpers.findAndHookMethod(imageReaderClass, "acquireNextImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Image img = (Image) param.getResult();
                        if (img != null) replaceImage(img);
                    }
                });

        } catch (Throwable e) {
            XposedBridge.log(TAG + " ImageReader hook FAIL: " + e);
        }


        // ---------------- CameraX hook (ru.yandex.taximeter вероятно использует этот путь) ----------------
        try {
            Class<?> analyzer = XposedHelpers.findClass(
                "androidx.camera.core.ImageAnalysis$Analyzer",
                lpparam.classLoader
            );

            XposedBridge.log(TAG + ": CameraX Analyzer FOUND");

            XposedHelpers.findAndHookMethod(
                analyzer,
                "analyze",
                "androidx.camera.core.ImageProxy",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log(TAG + ": CameraX frame intercepted");
                        // здесь позже добавим ПОДМЕНУ кадра
                    }
                }
            );

        } catch (Throwable e) {
            XposedBridge.log(TAG + ": CameraX not present: " + e);
        }
    }

    // ------------------------- Подмена YUV кадра (работает через ImageReader) -------------------------
    private void replaceImage(Image img){
        if (img.getFormat() != ImageFormat.YUV_420_888) return;

        File f = new File("/sdcard/Eskukap/fake.jpg");
        if (!f.exists()) return;

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        if (bmp == null) return;

        int w = img.getWidth(), h = img.getHeight();
        if (bmp.getWidth() != w || bmp.getHeight() != h)
            bmp = Bitmap.createScaledBitmap(bmp, w, h, true);

        byte[] data = bitmapToNV21(bmp, w, h);
        if (data != null) writeNV21(img, data, w, h);

        XposedBridge.log(TAG + ": FRAME REPLACED -> fake.jpg");
    }

    private byte[] bitmapToNV21(Bitmap bmp, int w, int h){
        int[] argb = new int[w*h];
        bmp.getPixels(argb,0,w,0,0,w,h);
        byte[] yuv = new byte[w*h*3/2];
        int frame=w*h, y=0,uv=frame;

        for(int j=0;j<h;j++){
            for(int i=0;i<w;i++){
                int c=argb[j*w+i];
                int R=(c>>16)&255, G=(c>>8)&255, B=c&255;
                int Y=((66*R+129*G+25*B+128)>>8)+16;
                int U=((-38*R-74*G+112*B+128)>>8)+128;
                int V=((112*R-94*G-18*B+128)>>8)+128;

                yuv[y++]=(byte)cl(Y);
                if(j%2==0 && i%2==0){
                    yuv[uv++]=(byte)cl(V);
                    yuv[uv++]=(byte)cl(U);
                }
            }
        }
        return yuv;
    }

    private int cl(int v){return v<0?0:v>255?255:v;}

    private void writeNV21(Image img, byte[] nv21, int w, int h){
        Image.Plane[] p=img.getPlanes();
        ByteBuffer Y=p[0].getBuffer();
        int rs=p[0].getRowStride();
        for(int r=0;r<h;r++){
            Y.position(r*rs);
            Y.put(nv21,r*w,w);
        }

        ByteBuffer U=p[1].getBuffer(), V=p[2].getBuffer();
        int rsU=p[1].getRowStride(), psU=p[1].getPixelStride();
        int rsV=p[2].getRowStride(), psV=p[2].getPixelStride();

        int uvStart=w*h;
        for(int j=0;j<h/2;j++){
            for(int i=0;i<w/2;i++){
                int idx=uvStart+j*w+i*2;
                U.put(j*rsU+i*psU, nv21[idx+1]);
                V.put(j*rsV+i*psV, nv21[idx]);
            }
        }
    }
}
