package com.gaba.eskukap;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;

public class FileHelper {

    private static final String TAG="EskukapFile";

    private static final String[] paths = new String[]{
            "/data/local/tmp/eskukap/frame.jpg",     // SettingsActivity сохраняет сюда
            "/sdcard/eskukap/frame.jpg",
            "/storage/emulated/0/eskukap/frame.jpg"
    };

    public static byte[] loadJPEG(){
        for(String p: paths){
            try{
                File f=new File(p);
                if(f.exists()){
                    FileInputStream fis=new FileInputStream(f);
                    byte[] d=new byte[(int)f.length()];
                    fis.read(d); fis.close();
                    Log.i(TAG,"JPEG loaded: "+p+" size="+d.length);
                    return d;
                }
            }catch(Throwable e){
                Log.e(TAG,"read err "+p+" -> "+e);
            }
        }
        Log.e(TAG,"NO JPEG FOUND");
        return null;
    }
}
