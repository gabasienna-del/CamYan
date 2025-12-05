package com.gabasienna.camyan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.IOException;

public class CameraHandler {

    private static final int REQUEST_IMG = 1001;
    private static Bitmap lastBitmap;

    public static void startCamera(Activity activity){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, REQUEST_IMG);
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMG && resultCode == Activity.RESULT_OK){
            try {
                Bitmap bmp;

                if(data.getData() != null){      // URI фото
                    Uri uri = data.getData();
                    bmp = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                } else {                        // миниатюра в data
                    bmp = (Bitmap) data.getExtras().get("data");
                }

                if(bmp != null){
                    lastBitmap = bmp;
                    Toast.makeText(activity,"Фото получено",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity,"Bitmap пустой",Toast.LENGTH_SHORT).show();
                }

            } catch(IOException e){
                Toast.makeText(activity,"Ошибка: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    public static Bitmap getLastBitmap(){
        return lastBitmap;
    }
}
