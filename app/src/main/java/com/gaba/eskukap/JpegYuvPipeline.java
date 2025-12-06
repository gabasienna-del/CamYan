package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageWriter;
import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;

public class JpegYuvPipeline {

    private static final String TAG = "JpegYuvPipeline";

    // ===== JPG → YUV420 (I420: YYYY UU VV) =====
    public static byte[] jpegToYuv420(byte[] jpegData, int[] outWidthHeight) {
        if (jpegData == null || jpegData.length == 0) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        if (bitmap == null) {
            Log.e(TAG, "jpegToYuv420: Bitmap decode failed");
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        outWidthHeight[0] = width;
        outWidthHeight[1] = height;

        int frameSize = width * height;
        int qFrameSize = frameSize / 4;
        byte[] yuv = new byte[frameSize + 2 * qFrameSize]; // I420

        int[] argb = new int[frameSize];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + qFrameSize;

        for (int j = 0; j < height; j++) {
            int rowOffset = j * width;
            for (int i = 0; i < width; i++) {
                int c = argb[rowOffset + i];

                int r = (c >> 16) & 0xff;
                int g = (c >> 8) & 0xff;
                int b = c & 0xff;

                int y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                int u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                int v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) clamp(y);
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    yuv[uIndex++] = (byte) clamp(u);
                    yuv[vIndex++] = (byte) clamp(v);
                }
            }
        }

        bitmap.recycle();
        return yuv;
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    // ===== YUV420 (I420) → MediaCodec =====
    public static void queueYuvToMediaCodec(MediaCodec codec, byte[] yuv420, long ptsUs) {
        if (codec == null || yuv420 == null) {
            return;
        }

        try {
            int index = codec.dequeueInputBuffer(0);
            if (index >= 0) {
                ByteBuffer input = codec.getInputBuffer(index);
                if (input != null) {
                    input.clear();
                    if (input.capacity() < yuv420.length) {
                        Log.e(TAG, "queueYuvToMediaCodec: buffer too small");
                        return;
                    }
                    input.put(yuv420, 0, yuv420.length);
                    codec.queueInputBuffer(index, 0, yuv420.length, ptsUs, 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "queueYuvToMediaCodec error", e);
        }
    }

    // ===== YUV420 (I420) → ImageReader через ImageWriter =====
    public static void pushYuvToImageReader(ImageReader reader, byte[] yuv420, int width, int height) {
        if (reader == null || yuv420 == null) {
            return;
        }

        ImageWriter writer = null;
        Image image = null;

        try {
            writer = ImageWriter.newInstance(reader.getSurface(), 2);
            image = writer.dequeueInputImage();

            if (image == null) {
                Log.e(TAG, "pushYuvToImageReader: dequeueInputImage null");
                return;
            }

            if (image.getFormat() != android.graphics.ImageFormat.YUV_420_888) {
                Log.e(TAG, "pushYuvToImageReader: wrong image format: " + image.getFormat());
                return;
            }

            fillImageWithI420(image, yuv420, width, height);
            writer.queueInputImage(image);

        } catch (Exception e) {
            Log.e(TAG, "pushYuvToImageReader error", e);
        } finally {
            // по твоей просьбе: без image.close() и writer.close()
        }
    }

    // === НОВОЕ: перезаписать существующий Image (из ImageReader) нашим I420 ===
    public static void overwriteImageWithI420(Image image, byte[] yuv420, int width, int height) {
        if (image == null || yuv420 == null) {
            return;
        }
        try {
            fillImageWithI420(image, yuv420, width, height);
        } catch (Exception e) {
            Log.e(TAG, "overwriteImageWithI420 error", e);
        }
    }

    // Копируем I420 в Image YUV_420_888
    private static void fillImageWithI420(Image image, byte[] yuv420, int width, int height) {
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length < 3) {
            Log.e(TAG, "fillImageWithI420: planes invalid");
            return;
        }

        int frameSize = width * height;
        int qFrameSize = frameSize / 4;

        int yOffset = 0;
        int uOffset = frameSize;
        int vOffset = frameSize + qFrameSize;

        copyToPlane(planes[0], yuv420, yOffset, width, height, 1);
        copyToPlane(planes[1], yuv420, uOffset, width / 2, height / 2, 1);
        copyToPlane(planes[2], yuv420, vOffset, width / 2, height / 2, 1);
    }

    private static void copyToPlane(
            Image.Plane plane,
            byte[] src,
            int srcOffset,
            int planeWidth,
            int planeHeight,
            int srcPixelStride
    ) {
        ByteBuffer buffer = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();

        buffer.rewind();

        if (pixelStride == 1 && rowStride == planeWidth) {
            buffer.put(src, srcOffset, planeWidth * planeHeight * srcPixelStride);
            return;
        }

        int srcIndex = srcOffset;

        for (int row = 0; row < planeHeight; row++) {
            int rowStart = buffer.position();

            for (int col = 0; col < planeWidth; col++) {
                if (srcIndex >= src.length) {
                    return;
                }
                buffer.put(src[srcIndex]);
                srcIndex += srcPixelStride;

                if (pixelStride > 1 && col < planeWidth - 1) {
                    int pos = rowStart + col * pixelStride;
                    buffer.position(pos);
                }
            }

            int nextRowPos = rowStart + rowStride;
            buffer.position(nextRowPos);
        }
    }
}
