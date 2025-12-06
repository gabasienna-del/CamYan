package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.InputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;
        XposedBridge.log("EskukapHook: Camera2 mode active");

        Class<?> cls = XposedHelpers.findClass(
                "android.media.ImageReader",
                lpparam.classLoader
        );

        XposedHelpers.findAndHookMethod(
                cls,
                "acquireNextImage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Image img = (Image) param.getResult();
                        if (img == null) return;

                        int w = img.getWidth();
                        int h = img.getHeight();
                        int fmt = img.getFormat();

                        XposedBridge.log("Eskukap: Frame " + w + "x" + h + " fmt=" + fmt);

                        Uri uri = getUri();
                        if (uri == null) return;

                        Bitmap bmp = loadBitmap(uri);
                        if (bmp == null) return;

                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);

                        replaceYUV(img, scaled);

                        XposedBridge.log("Eskukap: Camera2 frame replaced!");
                        param.setResult(img);
                    }
                }
        );
    }

    // ---------------- Replace YUV planes ----------------
    private void replaceYUV(Image img, Bitmap bmp) {
        ByteBuffer y = img.getPlanes()[0].getBuffer();
        ByteBuffer u = img.getPlanes()[1].getBuffer();
        ByteBuffer v = img.getPlanes()[2].getBuffer();

        int w = img.getWidth(), h = img.getHeight();
        int sizeY = w*h, sizeUV = sizeY/4;

        byte[] Y=new byte[sizeY];
        byte[] U=new byte[sizeUV];
        byte[] V=new byte[sizeUV];

        int idx=0;
        for(int j=0;j<h;j++)
            for(int i=0;i<w;i++){
                int c=bmp.getPixel(i,j);
                int r=(c>>16)&255, g=(c>>8)&255, b=c&255;
                int yv=(int)(0.299*r+0.587*g+0.114*b);
                int uv=(int)((-0.169*r-0.331*g+0.5*b)+128);
                int vv=(int)((0.5*r-0.419*g-0.081*b)+128);
                Y[idx]=(byte)yv;
                if(j%2==0 && i%2==0){
                    int p=(j/2)*(w/2)+(i/2);
                    U[p]=(byte)uv; V[p]=(byte)vv;
                }
                idx++;
            }

        y.put(Y,0,Y.length);
        u.put(U,0,U.length);
        v.put(V,0,V.length);
    }

    private Bitmap loadBitmap(Uri uri){
        try (InputStream is=getContext().getContentResolver().openInputStream(uri)){
            return BitmapFactory.decodeStream(is);
        }catch(Exception e){
            XposedBridge.log("Eskukap: image load err "+e);
            return null;
        }
    }

    private Uri getUri(){
        SharedPreferences sp=getContext().getSharedPreferences("eskukap", Context.MODE_PRIVATE);
        String u=sp.getString("img",null);
        return u==null?null:Uri.parse(u);
    }

    private Context getContext(){
        try{
            return (Context)XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread",null),
                "currentApplication"
            );
        }catch(Throwable e){return null;}
    }
}
