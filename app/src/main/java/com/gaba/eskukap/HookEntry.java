package com.gaba.eskukap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.media.Image;
import android.media.ImageReader;
import android.graphics.ImageFormat;

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

        XposedBridge.log("EskukapHook: Loaded ru.yandex.taximeter");

        Class<?> cls = XposedHelpers.findClass(
                "androidx.camera.core.ImageAnalysis$Analyzer",
                lpparam.classLoader
        );

        XposedHelpers.findAndHookMethod(
                cls,
                "analyze",
                "androidx.camera.core.ImageProxy",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Image image = (Image) XposedHelpers.callMethod(param.args[0], "getImage");
                        if (image == null) return;

                        int w = image.getWidth();
                        int h = image.getHeight();
                        int fmt = image.getFormat();

                        XposedBridge.log("Eskukap: Frame " + w + "x" + h + " fmt=" + fmt);

                        Uri uri = getUri();
                        if (uri == null) return;

                        Bitmap bmp = loadBitmap(uri);
                        if (bmp == null) return;

                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);

                        // -------- PRIVATE frame (fmt=256) → создаём fake YUV ----------
                        if (fmt == 256) {
                            ImageReader reader = ImageReader.newInstance(w, h, ImageFormat.YUV_420_888, 1);
                            Image fake = reader.acquireNextImage();
                            if (fake != null) {
                                replace(fake, scaled);
                                param.setResult(fake);
                                XposedBridge.log("Eskukap: PRIVATE → replaced fake frame!");
                            }
                            return;
                        }

                        // -------- Normal YUV replace ----------
                        if (fmt == ImageFormat.YUV_420_888) {
                            replace(image, scaled);
                            XposedBridge.log("Eskukap: YUV frame replaced!");
                        }
                    }
                }
        );
    }

    // ---------------- Replace image planes ----------------
    private void replace(Image img, Bitmap bmp) {
        ByteBuffer y = img.getPlanes()[0].getBuffer();
        ByteBuffer u = img.getPlanes()[1].getBuffer();
        ByteBuffer v = img.getPlanes()[2].getBuffer();

        int w = img.getWidth(), h = img.getHeight();
        int sizeY = w * h;
        int sizeUV = sizeY / 4;

        byte[] Y = new byte[sizeY];
        byte[] U = new byte[sizeUV];
        byte[] V = new byte[sizeUV];

        int idx = 0;
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++) {
                int c = bmp.getPixel(i, j);
                int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;

                int yv = (int)(0.299*r + 0.587*g + 0.114*b);
                int uv = (int)((-0.169*r - 0.331*g + 0.5*b) + 128);
                int vv = (int)((0.5*r - 0.419*g - 0.081*b) + 128);

                Y[idx] = (byte)(yv);
                if ((j%2==0)&&(i%2==0)) {
                    int pos = (j/2)*(w/2)+(i/2);
                    U[pos]=(byte)uv; V[pos]=(byte)vv;
                }
                idx++;
            }

        y.put(Y,0,Y.length);
        u.put(U,0,U.length);
        v.put(V,0,V.length);
    }

    // ---------------- Load saved image ----------------
    private Bitmap loadBitmap(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            XposedBridge.log("Eskukap: loadBitmap error "+e);
            return null;
        }
    }

    private Uri getUri() {
        SharedPreferences sp = getContext().getSharedPreferences("eskukap", Context.MODE_PRIVATE);
        String u = sp.getString("img", null);
        return u==null ? null : Uri.parse(u);
    }

    private Context getContext() {
        try {
            return (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
        } catch (Throwable e) {
            return null;
        }
    }
}
