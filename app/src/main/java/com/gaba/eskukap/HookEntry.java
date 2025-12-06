package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String IMG_PATH = "/data/local/tmp/eskukap_fake.jpg";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("Eskukap: Loaded ru.yandex.taximeter");

        XposedHelpers.findAndHookMethod(ImageReader.class, "acquireNextImage", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Image image = (Image) param.getResult();
                if(image == null) return;

                int w=image.getWidth(), h=image.getHeight();
                int format = image.getFormat();
                XposedBridge.log("Eskukap Frame ="+format+" "+w+"x"+h);

                File f = new File(IMG_PATH);
                if(!f.exists()) return;

                // Загружаем и ресайзим картинку под размер камеры
                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f));
                if(bmp == null) return;
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);

                // Конвертация в YUV420
                int ySize = w*h;
                int uvSize = w*h/2;
                byte[] yuv = new byte[ySize + uvSize];

                int i = 0;
                for(int y=0;y<h;y++){
                    for(int x=0;x<w;x++){
                        int c=scaled.getPixel(x,y);
                        int R=(c>>16)&0xff, G=(c>>8)&0xff, B=c&0xff;
                        int Y=(int)(0.299*R+0.587*G+0.114*B);
                        yuv[i++] = (byte)Y;
                    }
                }
                int j = ySize;
                for(int y=0;y<h;y+=2){
                    for(int x=0;x<w;x+=2){
                        int c=scaled.getPixel(x,y);
                        int R=(c>>16)&0xff, G=(c>>8)&0xff, B=c&0xff;
                        int U=(int)(-0.169*R-0.331*G+0.5*B+128);
                        int V=(int)(0.5*R-0.419*G-0.081*B+128);
                        yuv[j++] = (byte)U;
                        yuv[j++] = (byte)V;
                    }
                }

                try {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer Y = planes[0].getBuffer();
                    ByteBuffer U = planes[1].getBuffer();
                    ByteBuffer V = planes[2].getBuffer();

                    Y.put(yuv,0,ySize);
                    U.put(yuv,ySize,uvSize/2);
                    V.put(yuv,ySize+uvSize/2,uvSize/2);

                    XposedBridge.log("Eskukap: *** YUV FRAME REPLACED OK ***");

                }catch(Throwable e){
                    XposedBridge.log("Eskukap YUV error "+e);
                }
            }
        });
    }
}
