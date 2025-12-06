package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!"ru.yandex.taximeter".equals(lpparam.packageName)) return;
        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        // ---------- ImageReader.acquireLatestImage ----------
        try {
            XposedHelpers.findAndHookMethod(
                ImageReader.class,
                "acquireLatestImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Image image = (Image) param.getResult();
                        if (image == null) return;

                        int fmt = image.getFormat();
                        int w = image.getWidth();
                        int h = image.getHeight();

                        XposedBridge.log("Eskukap: Frame " + w + "x" + h + " fmt=" + fmt);

                        // ========== YUV 420 -> JPEG resize ==========
                        if (fmt == ImageFormat.YUV_420_888) {
                            byte[] nv21 = yuvToNV21(image);
                            if (nv21 != null) {
                                byte[] jpeg = resizeNV21(nv21, w, h);
                                if (jpeg != null)
                                    XposedBridge.log("Eskukap: YUV scaled 1280x720 ✓ size=" + jpeg.length);
                            }
                        }

                        // ========== PRIVATE -> HardwareBuffer -> Bitmap -> Resize ==========
                        if (fmt == 256 && Build.VERSION.SDK_INT >= 29) {
                            Bitmap bmp = privateResize(image);
                            if (bmp != null)
                                XposedBridge.log("Eskukap: PRIVATE scaled 1280x720 ✓");
                        }
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("Eskukap IMAGE HOOK ERR: " + e);
        }
    }

    // ------------------ Convert YUV -> NV21 ------------------
    private static byte[] yuvToNV21(Image img) {
        try {
            int w = img.getWidth(), h = img.getHeight();
            Image.Plane[] p = img.getPlanes();

            ByteBuffer Y = p[0].getBuffer();
            ByteBuffer U = p[1].getBuffer();
            ByteBuffer V = p[2].getBuffer();

            int yRow = p[0].getRowStride();
            int uRow = p[1].getRowStride();
            int vRow = p[2].getRowStride();

            byte[] out = new byte[w*h*3/2];
            int pos=0;

            for(int i=0;i<h;i++){
                Y.position(i*yRow);
                Y.get(out,pos,w);
                pos+=w;
            }
            for(int i=0;i<h/2;i++){
                for(int j=0;j<w/2;j++){
                    U.position(i*uRow + j*2);
                    V.position(i*vRow + j*2);
                    out[pos++] = V.get();
                    out[pos++] = U.get();
                }
            }
            return out;

        }catch(Throwable e){
            XposedBridge.log("NV21 ERR "+e);
            return null;
        }
    }

    // ------------------ NV21 -> JPEG -> scale 1280x720 ------------------
    private static byte[] resizeNV21(byte[] nv,int w,int h){
        try{
            android.graphics.YuvImage yuv=new android.graphics.YuvImage(nv,ImageFormat.NV21,w,h,null);
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0,0,w,h),90,os);
            byte[] jpg=os.toByteArray();

            Bitmap bmp = BitmapFactory.decodeByteArray(jpg,0,jpg.length);
            Bitmap out = Bitmap.createScaledBitmap(bmp,1280,720,true);

            ByteArrayOutputStream r=new ByteArrayOutputStream();
            out.compress(Bitmap.CompressFormat.JPEG,90,r);
            return r.toByteArray();

        }catch(Throwable e){
            XposedBridge.log("resizeNV21 ERR "+e);
            return null;
        }
    }

    // ------------------ PRIVATE -> Bitmap -> Resize 1280x720 ------------------
    private static Bitmap privateResize(Image img){
        try{
            HardwareBuffer hb = img.getHardwareBuffer();
            if (hb == null) return null;

            ColorSpace cs = ColorSpace.get(ColorSpace.Named.SRGB);
            Bitmap src = Bitmap.wrapHardwareBuffer(hb, android.graphics.PixelFormat.RGBA_8888, cs);
            if(src==null)return null;

            return Bitmap.createScaledBitmap(src,1280,720,true);

        }catch(Throwable e){
            XposedBridge.log("private ERR "+e);
            return null;
        }
    }
}
