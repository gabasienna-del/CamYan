package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * Чистый конвертер JPG -> YUV420 (I420).
 * Без android.media.* — только Bitmap/Color.
 */
public class JpegYuvFeeder {

    // Простая структура кадра YUV420
    public static class YuvFrame {
        public final int width;
        public final int height;
        public final byte[] y;
        public final byte[] u;
        public final byte[] v;

        public YuvFrame(int width, int height, byte[] y, byte[] u, byte[] v) {
            this.width = width;
            this.height = height;
            this.y = y;
            this.u = u;
            this.v = v;
        }
    }

    /**
     * Преобразовать файл JPEG в YUV420.
     */
    public static YuvFrame jpegToYuv(String path, int targetWidth, int targetHeight) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        if (bmp == null) {
            throw new RuntimeException("Failed to decode jpeg: " + path);
        }

        Bitmap scaled = Bitmap.createScaledBitmap(bmp, targetWidth, targetHeight, true);
        if (scaled != bmp) {
            bmp.recycle();
        }

        YuvFrame frame = bitmapToYuv420(scaled);
        scaled.recycle();
        return frame;
    }

    /**
     * Bitmap -> YUV420 (I420: сначала все Y, потом все U, потом V).
     */
    private static YuvFrame bitmapToYuv420(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;

        byte[] y = new byte[size];
        byte[] u = new byte[size / 4];
        byte[] v = new byte[size / 4];

        int[] argb = new int[size];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        int yIndex = 0;
        int uIndex = 0;
        int vIndex = 0;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int c = argb[j * width + i];

                int R = Color.red(c);
                int G = Color.green(c);
                int B = Color.blue(c);

                int Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                if (Y < 0) Y = 0;
                if (Y > 255) Y = 255;
                y[yIndex++] = (byte) Y;

                // Для U/V берём каждый 2x2 блок
                if ((j & 1) == 0 && (i & 1) == 0) {
                    int U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                    int V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                    if (U < 0) U = 0;
                    if (U > 255) U = 255;
                    if (V < 0) V = 0;
                    if (V > 255) V = 255;

                    u[uIndex++] = (byte) U;
                    v[vIndex++] = (byte) V;
                }
            }
        }

        return new YuvFrame(width, height, y, u, v);
    }
}
