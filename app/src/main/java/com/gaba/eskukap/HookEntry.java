package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;

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


        // ---------------- CameraX ImageProxy Hook ----------------
        try {

            Class<?> imageProxy = XposedHelpers.findClass(
                "androidx.camera.core.ImageProxy",
                lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                imageProxy,
                "getPlanes",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {

                        Object[] planes = (Object[]) param.getResult();
                        if (planes == null || planes.length == 0) return;

                        XposedBridge.log(TAG + ": CameraX frame OK — planes captured");

                        try {
                            Image img = (Image) XposedHelpers.callMethod(param.thisObject, "getImage");
                            if (img != null) replace(img);
                        } catch (Throwable ignore) {}

                    }
                });

            XposedBridge.log(TAG + ": CameraX ImageProxy hook OK");

        } catch (Throwable t) {
            XposedBridge.log(TAG + ": No CameraX ImageProxy");
        }


        // ---------------- ImageReader fallback ----------------
        try {
            Class<?> imageReaderClass = XposedHelpers.findClass(
                "android.media.ImageReader",
                lpparam.classLoader);

            XposedHelpers.findAndHookMethod(imageReaderClass, "acquireLatestImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Image img = (Image) param.getResult();
                        if (img != null) replace(img);
                    }
                });

            XposedHelpers.findAndHookMethod(imageReaderClass, "acquireNextImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Image img = (Image) param.getResult();
                        if (img != null) replace(img);
                    }
                });

            XposedBridge.log(TAG + ": ImageReader hook OK");

        } catch (Throwable ignored) {}
    }


    // -------- подмена кадра --------
    private void replace(Image img){

        if (img.getFormat() != ImageFormat.YUV_420_888) {
            XposedBridge.log(TAG + ": Frame format not YUV_420_888");
            return;
        }

        File f = new File("/sdcard/Eskukap/fake.jpg");
        if (!f.exists()) {
            XposedBridge.log(TAG + ": fake.jpg not found");
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        if (bmp == null) return;

        int w = img.getWidth(), h = img.getHeight();

        if (bmp.getWidth()!=w || bmp.getHeight()!=h)
            bmp = Bitmap.createScaledBitmap(bmp,w,h,true);

        byte[] nv21 = toNV21(bmp,w,h);
        if (nv21!=null) write(img,nv21,w,h);

        XposedBridge.log(TAG + ": FRAME REPLACED");
    }


    private byte[] toNV21(Bitmap bmp,int w,int h){
        int[] a=new int[w*h];
        bmp.getPixels(a,0,w,0,0,w,h);
        byte[] yuv=new byte[w*h*3/2]; int frame=w*h,y=0,uv=frame;

        for(int j=0;j<h;j++){
            for(int i=0;i<w;i++){
                int c=a[j*w+i];
                int R=(c>>16)&255,G=(c>>8)&255,B=c&255;
                int Y=((66*R+129*G+25*B+128)>>8)+16;
                int U=((-38*R-74*G+112*B+128)>>8)+128;
                int V=((112*R-94*G-18*B+128)>>8)+128;
                yuv[y++]=(byte)clip(Y);
                if(j%2==0&&i%2==0){
                    yuv[uv++]=(byte)clip(V);
                    yuv[uv++]=(byte)clip(U);
                }
            }
        } return yuv;
    }

    private int clip(int v){return v<0?0:v>255?255:v;}

    private void write(Image img,byte[] nv21,int w,int h){
        Image.Plane[]p=img.getPlanes();

        ByteBuffer Y=p[0].getBuffer();
        int rs=p[0].getRowStride();
        for(int r=0;r<h;r++){
            Y.position(r*rs);
            Y.put(nv21,r*w,w);
        }

        ByteBuffer U=p[1].getBuffer(),V=p[2].getBuffer();
        int rsU=p[1].getRowStride(),psU=p[1].getPixelStride();
        int rsV=p[2].getRowStride(),psV=p[2].getPixelStride();
        int uv=w*h;

        for(int j=0;j<h/2;j++)
            for(int i=0;i<w/2;i++){
                int idx=uv+j*w+i*2;
                U.put(j*rsU+i*psU, nv21[idx+1]);
                V.put(j*rsV+i*psV, nv21[idx]);
            }
    }
}
