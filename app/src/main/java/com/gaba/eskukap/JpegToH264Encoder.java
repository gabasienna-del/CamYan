package com.gaba.eskukap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class JpegToH264Encoder {

    private static final String TAG = "EskukapJpegEncoder";

    private MediaCodec codec;
    private MediaCodec.BufferInfo bufferInfo;

    private int width;
    private int height;
    private int fps = 30;
    private int bitRate = 2_000_000;

    public JpegToH264Encoder(int width, int height) throws Exception {
        this.width = width;
        this.height = height;
        initEncoder();
    }

    private void initEncoder() throws Exception {
        codec = MediaCodec.createEncoderByType("video/avc");
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();

        Log.i(TAG, "Encoder inited " + width + "x" + height);
    }

    /**
     * JPEG (byte[]) -> Bitmap -> NV21 (YUV420) -> queueInputBuffer()
     */
    public void encodeJpegFrame(byte[] jpegData) {
        if (jpegData == null || jpegData.length == 0) return;

        try {
            // 1) JPEG -> Bitmap
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bmp = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, opts);
            if (bmp == null) {
                Log.w(TAG, "decode JPEG failed");
                return;
            }

            // 2) scale под размер энкодера
            Bitmap scaled = bmp;
            if (bmp.getWidth() != width || bmp.getHeight() != height) {
                scaled = Bitmap.createScaledBitmap(bmp, width, height, true);
                bmp.recycle();
            }

            int[] argb = new int[width * height];
            scaled.getPixels(argb, 0, width, 0, 0, width, height);
            scaled.recycle();

            // 3) ARGB -> NV21
            byte[] nv21 = new byte[width * height * 3 / 2];
            argbToNV21(argb, nv21, width, height);

            // 4) Вкидываем NV21 в MediaCodec
            queueYuvToCodec(nv21);

        } catch (Throwable e) {
            Log.e(TAG, "encodeJpegFrame error", e);
        }
    }

    /**
     * Подача YUV кадра в MediaCodec
     */
    private void queueYuvToCodec(byte[] yuvFrame) {
        if (codec == null) return;

        try {
            int inputIndex = codec.dequeueInputBuffer(10000);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                if (inputBuffer != null) {
                    inputBuffer.clear();
                    inputBuffer.put(yuvFrame);

                    long ptsUs = System.nanoTime() / 1000L;
                    codec.queueInputBuffer(inputIndex, 0, yuvFrame.length, ptsUs, 0);
                }
            }

            drainOutput();
        } catch (Throwable e) {
            Log.e(TAG, "queueYuvToCodec error", e);
        }
    }

    /**
     * Читаем закодированные H.264 NAL
     */
    private void drainOutput() {
        if (codec == null) return;

        while (true) {
            int outIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            if (outIndex >= 0) {
                ByteBuffer outBuffer = codec.getOutputBuffer(outIndex);
                if (outBuffer != null && bufferInfo.size > 0) {
                    byte[] data = new byte[bufferInfo.size];
                    outBuffer.get(data);
                    onEncodedFrame(data, bufferInfo.presentationTimeUs, bufferInfo.flags);
                }
                codec.releaseOutputBuffer(outIndex, false);
            } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = codec.getOutputFormat();
                Log.i(TAG, "Encoder format changed: " + newFormat);
            } else {
                break;
            }
        }
    }

    /**
     * Здесь готовый H.264 кадр
     */
    protected void onEncodedFrame(byte[] h264, long ptsUs, int flags) {
        Log.i(TAG, "Encoded frame " + h264.length + " bytes, pts=" + ptsUs + " flags=" + flags);
        // TODO: запись в файл / отправка в сеть / muxer и т.п.
    }

    /**
     * ARGB -> NV21 (YUV420)
     */
    private void argbToNV21(int[] argb, byte[] yuv, int width, int height) {
        int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int c = argb[j * width + i];

                a = (c >> 24) & 0xff;
                R = (c >> 16) & 0xff;
                G = (c >> 8) & 0xff;
                B = c & 0xff;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                if (Y < 0) Y = 0;
                if (Y > 255) Y = 255;
                if (U < 0) U = 0;
                if (U > 255) U = 255;
                if (V < 0) V = 0;
                if (V > 255) V = 255;

                yuv[yIndex++] = (byte) Y;

                if ((j % 2 == 0) && (i % 2 == 0) && (uvIndex + 1 < yuv.length)) {
                    yuv[uvIndex++] = (byte) V;
                    yuv[uvIndex++] = (byte) U;
                }
            }
        }
    }

    public void release() {
        if (codec != null) {
            try {
                codec.stop();
            } catch (Exception ignored) {}
            try {
                codec.release();
            } catch (Exception ignored) {}
            codec = null;
        }
    }
}
