package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.view.PixelCopy;
import android.view.Surface;
import android.graphics.SurfaceTexture;

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

                        XposedBridge.log("Eskukap: Frame "+w+"x"+h+" fmt="+fmt);

                        // ========== NORMAL YUV (decode instantly) ==========
                        if (fmt == ImageFormat.YUV_420_888) {
                            byte[] nv21 = yuvToNV21(image);
                            if(nv21!=null){
                                byte[] out = scaleNV21(nv21,w,h);
                                if(out!=null)
                                    XposedBridge.log("Eskukap: YUV scaled 1280x720 ✓");
                            }
                        }

                        // ========== PRIVATE / HARDWARE GPU BUFFER ==========
                        if (fmt == 256 && Build.VERSION.SDK_INT>=26) {
                            Bitmap bmp = privateCopy(image,w,h);
                            if(bmp!=null){
                                Bitmap out = Bitmap.createScaledBitmap(bmp,1280,720,true);
                                XposedBridge.log("Eskukap: PRIVATE → Bitmap → scaled ✓");
                            }
                        }
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook ERROR: "+e);
        }
    }

    // ----------- PRIVATE → Bitmap via PixelCopy (GPU safe method) -----------
    private Bitmap privateCopy(Image img,int w,int h){
        try{
            SurfaceTexture tex = new SurfaceTexture(0);
            tex.setDefaultBufferSize(w,h);
            Surface surf = new Surface(tex);

            Bitmap bmp = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);

            final Object lock=new Object();
            final boolean[] done={false};

            PixelCopy.request(surf, bmp, (r)->{
                synchronized(lock){done[0]=true;lock.notify();}
            }, null);

            synchronized(lock){lock.wait(80);}

            surf.release();
            tex.release();

            return done[0]?bmp:null;

        }catch(Throwable e){
            XposedBridge.log("Eskukap: PixelCopy ERR "+e);
            return null;
        }
    }

    // ------------------ YUV -> NV21 ------------------
    private static byte[] yuvToNV21(Image img){
        try{
            int w=img.getWidth(),h=img.getHeight();
            Image.Plane[]p=img.getPlanes();

            ByteBuffer Y=p[0].getBuffer();
            ByteBuffer U=p[1].getBuffer();
            ByteBuffer V=p[2].getBuffer();

            int yRow=p[0].getRowStride();
            int uRow=p[1].getRowStride();
            int vRow=p[2].getRowStride();

            byte[]out=new byte[w*h*3/2];int pos=0;

            for(int i=0;i<h;i++){Y.position(i*yRow);Y.get(out,pos,w);pos+=w;}
            for(int i=0;i<h/2;i++)
                for(int j=0;j<w/2;j++){
                    U.position(i*uRow+j*2);
                    V.position(i*vRow+j*2);
                    out[pos++]=V.get();out[pos++]=U.get();
                }
            return out;

        }catch(Exception e){return null;}
    }

    // =============== NV21 → JPEG → scale ===============
    private static byte[] scaleNV21(byte[]nv,int w,int h){
        try{
            android.graphics.YuvImage y=new android.graphics.YuvImage(nv,ImageFormat.NV21,w,h,null);
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            y.compressToJpeg(new Rect(0,0,w,h),90,os);
            byte[]jpg=os.toByteArray();
            Bitmap b=BitmapFactory.decodeByteArray(jpg,0,jpg.length);
            Bitmap o=Bitmap.createScaledBitmap(b,1280,720,true);

            ByteArrayOutputStream r=new ByteArrayOutputStream();
            o.compress(Bitmap.CompressFormat.JPEG,90,r);
            return r.toByteArray();

        }catch(Exception e){return null;}
    }
}
