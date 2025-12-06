package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageWriter;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * JPG -> YUV420 -> ImageWriter(YUV_420_888) -> Surface.
 *
 * Можно использовать, чтобы кормить Surface, который ждёт YUV кадры
 * (Camera2/CameraX/MediaCodec/ML и т.п.).
 */
public final class JpegYuvFeeder {

    private JpegYuvFeeder() {}

    // ==== Публичное API ================================================

    /**
     * Создать ImageWriter под заданный Surface.
     *
     * @param surface Surface, на который должны улетать кадры
     * @param width   ширина кадра
     * @param height  высота кадра
     */
    public static ImageWriter createYuvWriter(Surface surface, int width, int height) {
        // maxImages = 3, формат YUV_420_888
        return ImageWriter.newInstance(surface, 3, ImageFormat.YUV_420_888);
    }

    /**
     * Один раз загрузить JPG с диска, конвертировать в YUV420 и сохранить в структуру.
     * Далее эту структуру можно много раз отправлять в ImageWriter.
     */
    public static YuvFrame jpegFileToYuv(String path, int targetWidth, int targetHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        if (bitmap == null) {
            throw new IllegalArgumentException("Cannot decode jpeg: " + path);
        }

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        if (scaled != bitmap) {
            bitmap.recycle();
        }

        YuvFrame frame = bitmapToYuv420(scaled);
        scaled.recycle();
        return frame;
    }

    /**
     * Отправить готовый YUV кадр в ImageWriter.
     */
    public static void queueYuvToWriter(ImageWriter writer, YuvFrame frame) {
        Image image = writer.dequeueInputImage();
        try {
            fillImageFromYuvFrame(image, frame);
            long ts = System.nanoTime();
            image.setTimestamp(ts);
            writer.queueInputImage(image);
        } finally {
            // Важно: если queueInputImage не был вызван из-за исключения,
            // надо закрыть Image, чтобы не утечь буферы.
            try {
                image.close();
            } catch (Throwable ignore) {}
        }
    }

    // ==== Внутренние структуры =========================================

    /**
     * Простой контейнер для I420: Y (WxH), U (W/2 x H/2), V (W/2 x H/2).
     */
    public static final class YuvFrame {
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

    // ==== Bitmap -> YUV420 (I420) ======================================

    private static YuvFrame bitmapToYuv420(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;

        byte[] y = new byte[size];
        byte[] u = new byte[size / 4];
        byte[] v = new byte[size / 4];

        int[] argb = new int[size];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        // Считаем Y для каждого пикселя, U/V – усредняем по 2x2 блоку (I420)
        int yIndex = 0;
        int uIndex = 0;
        int vIndex = 0;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int c = argb[j * width + i];
                int R = Color.red(c);
                int G = Color.green(c);
                int B = Color.blue(c);

                int yy = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                if (yy < 0) yy = 0;
                if (yy > 255) yy = 255;
                y[yIndex++] = (byte) yy;
            }
        }

        // U/V на сетке (width/2 x height/2)
        for (int j = 0; j < height; j += 2) {
            for (int i = 0; i < width; i += 2) {
                int c1 = argb[j * width + i];
                int c2 = argb[j * width + Math.min(i + 1, width - 1)];
                int c3 = argb[Math.min(j + 1, height - 1) * width + i];
                int c4 = argb[Math.min(j + 1, height - 1) * width + Math.min(i + 1, width - 1)];

                int R1 = Color.red(c1), G1 = Color.green(c1), B1 = Color.blue(c1);
                int R2 = Color.red(c2), G2 = Color.green(c2), B2 = Color.blue(c2);
                int R3 = Color.red(c3), G3 = Color.green(c3), B3 = Color.blue(c3);
                int R4 = Color.red(c4), G4 = Color.green(c4), B4 = Color.blue(c4);

                int R = (R1 + R2 + R3 + R4) >> 2;
                int G = (G1 + G2 + G3 + G4) >> 2;
                int B = (B1 + B2 + B3 + B4) >> 2;

                int uu = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                int vv = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                if (uu < 0) uu = 0;
                if (uu > 255) uu = 255;
                if (vv < 0) vv = 0;
                if (vv > 255) vv = 255;

                u[uIndex++] = (byte) uu;
                v[vIndex++] = (byte) vv;
            }
        }

        return new YuvFrame(width, height, y, u, v);
    }

    // ==== Запись I420 в Image (YUV_420_888) ============================

    private static void fillImageFromYuvFrame(Image image, YuvFrame frame) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Image format must be YUV_420_888");
        }
        if (image.getWidth() != frame.width || image.getHeight() != frame.height) {
            throw new IllegalArgumentException("Image size mismatch");
        }

        Image.Plane[] planes = image.getPlanes();
        if (planes.length != 3) {
            throw new IllegalStateException("Expected 3 planes for YUV_420_888");
        }

        // Y plane
        copyPlane(frame.y, frame.width, frame.height,
                planes[0].getBuffer(),
                planes[0].getRowStride(),
                planes[0].getPixelStride());

        int chromaWidth = frame.width / 2;
        int chromaHeight = frame.height / 2;

        // U plane
        copyPlane(frame.u, chromaWidth, chromaHeight,
                planes[1].getBuffer(),
                planes[1].getRowStride(),
                planes[1].getPixelStride());

        // V plane
        copyPlane(frame.v, chromaWidth, chromaHeight,
                planes[2].getBuffer(),
                planes[2].getRowStride(),
                planes[2].getPixelStride());

        // Обрежем crop под весь кадр (на всякий случай)
        image.setCropRect(new Rect(0, 0, frame.width, frame.height));
    }

    private static void copyPlane(
            byte[] src,
            int width,
            int height,
            ByteBuffer dest,
            int rowStride,
            int pixelStride
    ) {
        dest.clear();

        int srcIndex = 0;
        for (int y = 0; y < height; y++) {
            int rowStart = dest.position();

            // идём по пикселям в строке и кладём через pixelStride
            for (int x = 0; x < width; x++) {
                int pos = rowStart + x * pixelStride;
                dest.position(pos);
                dest.put(src[srcIndex++]);
            }

            // прыгаем на следующий rowStride
            dest.position(rowStart + rowStride);
        }

        dest.flip();
    }
}
