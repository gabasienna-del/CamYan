package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ImageFormat;

public class JpegYuvFeeder {

    public static class YuvFrame {
        public final int width;
        public final int height;
        public final byte[] y;
        public final byte[] u;
        public final byte[] v;

        public YuvFrame(int width, int height, byte[] y, byte[] u, byte[] v) {
            this.width = width;
            this.height = height;
            this.y = y; this.u = u; this.v = v;
        }
    }

    public static YuvFrame jpegToYuv(String path, int width, int height) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        if (bmp == null) throw new RuntimeException("Failed decode " + path);

        Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, true);

        int size = width * height;
        byte[] y = new byte[size];
        byte[] u = new byte[size/4];
        byte[] v = new byte[size/4];

        int idxY=0, idxUV=0;

        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                int c = scaled.getPixel(i,j);
                int R=(Color.red(c)), G=(Color.green(c)), B=(Color.blue(c));

                int Y = ((66*R+129*G+25*B+128)>>8)+16;
                y[idxY++] = (byte)Math.min(255,Math.max(0,Y));

                if(i%2==0 && j%2==0){
                    int U = ((-38*R-74*G+112*B+128)>>8)+128;
                    int V = ((112*R-94*G-18*B+128)>>8)+128;
                    u[idxUV] = (byte)Math.min(255,Math.max(0,U));
                    v[idxUV] = (byte)Math.min(255,Math.max(0,V));
                    idxUV++;
                }
            }
        }

        return new YuvFrame(width,height,y,u,v);
    }
}
