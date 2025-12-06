package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
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

                        // ---- YUV → NV21 → JPEG → Scale 1280x720 ----
                        if (fmt == ImageFormat.YUV_420_888) {
                            byte[] nv21 = yuv420ToNV21(image);
                            if (nv21 != null) {
                                byte[] jpeg = scaleNV21(nv21, w, h);
                                if (jpeg != null) {
                                    XposedBridge.log("Eskukap: YUV scaled 1280x720 OK ("+jpeg.length+"b)");
                                }
                            }
                        }

                        // ---- PRIVATE (256) → HardwareBuffer → Bitmap → Scale ----
                        if (fmt == 256 && Build.VERSION.SDK_INT >= 29) {
                            Bitmap bmp = privateToBitmapScaled(image);
                            if (bmp != null) {
                                XposedBridge.log("Eskukap: PRIVATE scaled 1280x720 OK");
                            }
                        }
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook ImageReader error: " + e);
        }
    }

    // ========= YUV → NV21 =========
    private static byte[] yuv420ToNV21(Image img) {
        try {
            Image.Plane[] p = img.getPlanes();
            int w = img.getWidth(), h = img.getHeight();

            ByteBuffer y = p[0].getBuffer();
            ByteBuffer u = p[1].getBuffer();
            ByteBuffer v = p[2].getBuffer();

            int yRow = p[0].getRowStride();
            int uRow = p[1].getRowStride();
            int vRow = p[2].getRowStride();

            byte[] out = new byte[w*h*3/2];
            int pos=0;

            for(int i=0;i<h;i++){
                y.position(i*yRow);
                y.get(out,pos,w);
                pos+=w;
            }

            for(int i=0;i<h/2;i++){
                for(int j=0;j<w/2;j++){
                    u.position(i*uRow+j*2);
                    v.position(i*vRow+j*2);
                    out[pos++] = v.get();
                    out[pos++] = u.get();
                }
            }
            return out;

        } catch (Throwable e) {
            XposedBridge.log("NV21 ERR:"+e);
            return null;
        }
    }

    // ========= NV21 → JPEG → Scale =========
    private static byte[] scaleNV21(byte[] nv21,int w,int h){
        try{
            android.graphics.YuvImage yuv=new android.graphics.YuvImage(nv21, ImageFormat.NV21,w,h,null);
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0,0,w,h),90,os);
            byte[] jpeg=os.toByteArray();
            Bitmap bmp=android.graphics.BitmapFactory.decodeByteArray(jpeg,0,jpeg.length);
            Bitmap out=Bitmap.createScaledBitmap(bmp,1280,720,true);
            ByteArrayOutputStream res=new ByteArrayOutputStream();
            out.compress(Bitmap.CompressFormat.JPEG,90,res);
            return res.toByteArray();

        }catch(Throwable e){
            XposedBridge.log("scaleNV21 ERR:"+e);
            return null;
        }
    }

    // ========= PRIVATE → HardwareBuffer → Bitmap → Scale =========
    private static Bitmap privateToBitmapScaled(Image img){
        try{
            android.hardware.HardwareBuffer hb=img.getHardwareBuffer();
            if(hb==null) return null;
            Bitmap src = Bitmap.wrapHardwareBuffer(hb, android.graphics.PixelFormat.RGBA_8888);
            if(src==null)return null;
            return Bitmap.createScaledBitmap(src,1280,720,true);

        }catch(Throwable e){
            XposedBridge.log("privateToBitmapScaled ERR:"+e);
            return null;
        }
    }
}
