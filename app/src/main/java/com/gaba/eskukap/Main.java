package com.gaba.eskukap;

public class Main {
    public static void main(String[] args) {

        // Путь к тестовой JPG
        String path = "/sdcard/test.jpg";     // <- поставь свою фотографию

        int width  = 1280;
        int height = 720;

        System.out.println("[START] JPG -> YUV420 conversion");

        try {
            FakeCameraHook.YuvFrame frame = FakeCameraHook.jpegToYuv(path, width, height);

            System.out.println("[OK] YUV created:");
            System.out.println("W=" + frame.width + " H=" + frame.height);
            System.out.println("Y=" + frame.y.length + " bytes");
            System.out.println("U=" + frame.u.length + " bytes");
            System.out.println("V=" + frame.v.length + " bytes");

        } catch(Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
