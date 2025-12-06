package com.gaba.eskukap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;

import java.io.InputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;
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
                            int w = image.getWidth(), h = image.getHeight();

                            XposedBridge.log("Eskukap: Frame "+w+"x"+h+" fmt="+fmt);

                            // PRIVATE не трогаем
                            if (fmt == 256) return;
                            if (fmt != ImageFormat.YUV_420_888) return;

                            Context ctx = getContext();
                            if (ctx == null) {
                                XposedBridge.log("Eskukap: ctx null");
                                return;
                            }

                            SharedPreferences sp = ctx.getSharedPreferences("eskukap", Context.MODE_PRIVATE);
                            String uriStr = sp.getString("img", null);
                            if (uriStr == null) return;

                            Bitmap bmp = loadBitmap(ctx, Uri.parse(uriStr));
                            if (bmp == null) return;

                            Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);
                            replace(image, scaled);
                            XposedBridge.log("Eskukap: frame replaced!");
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log("EskukapHook ERR: "+e);
        }
    }

    private Context getContext() {
        try {
            Object at = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentActivityThread"
            );
            return (Context) XposedHelpers.callMethod(at, "getApplication");
        } catch (Throwable e) {
            XposedBridge.log("Eskukap ctx ERR "+e);
            return null;
        }
    }

    private static Bitmap loadBitmap(Context ctx, Uri uri) {
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(is);
        } catch (Throwable e) {
            XposedBridge.log("Eskukap loadBitmap ERR "+e);
            return null;
        }
    }

    private static void replace(Image image, Bitmap bmp) {
        if (image.getFormat() != ImageFormat.YUV_420_888) return;

        int w=image.getWidth(), h=image.getHeight();
        bmp = Bitmap.createScaledBitmap(bmp, w, h, true);

        int[] px = new int[w*h];
        bmp.getPixels(px,0,w,0,0,w,h);

        byte[] y=new byte[w*h], u=new byte[(w*h)/4], v=new byte[(w*h)/4];

        int p=0;
        for(int j=0;j<h;j++){
            for(int i=0;i<w;i++){
                int c = px[p++];
                int r=(c>>16)&255,g=(c>>8)&255,b=c&255;
                int Y=(int)(0.299*r+0.587*g+0.114*b);
                int U=(int)(-0.169*r-0.331*g+0.5*b+128);
                int V=(int)(0.5*r-0.419*g-0.081*b+128);

                y[j*w+i]=(byte)Y;

                if((j%2==0)&&(i%2==0)){
                    int idx=(j/2)*(w/2)+(i/2);
                    u[idx]=(byte)U;
                    v[idx]=(byte)V;
                }
            }
        }

        Image.Plane[] pArr=image.getPlanes();

        // Y
        ByteBuffer yB=pArr[0].getBuffer();
        int ys=pArr[0].getRowStride(),yps=pArr[0].getPixelStride();
        for(int j=0;j<h;j++)
            for(int i=0;i<w;i++){
                yB.position(j*ys+i*yps);
                yB.put(y[j*w+i]);
            }

        // U
        ByteBuffer uB=pArr[1].getBuffer();
        int us=pArr[1].getRowStride(),ups=pArr[1].getPixelStride();
        for(int j=0;j<h/2;j++)
            for(int i=0;i<w/2;i++){
                uB.position(j*us+i*ups);
                uB.put(u[j*(w/2)+i]);
            }

        // V
        ByteBuffer vB=pArr[2].getBuffer();
        int vs=pArr[2].getRowStride(),vps=pArr[2].getPixelStride();
        for(int j=0;j<h/2;j++)
            for(int i=0;i<w/2;i++){
                vB.position(j*vs+i*vps);
                vB.put(v[j*(w/2)+i]);
            }
    }
}
