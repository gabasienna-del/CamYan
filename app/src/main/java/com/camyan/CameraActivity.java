package com.camyan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {

    private static final int REQ_CAMERA = 101;
    private String currentPath;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        img = new ImageView(this);
        setContentView(img);

        startFullCamera(); // запускаем фото
    }

    // ---------- СТАРТ КАМЕРЫ ----------
    private void startFullCamera() {
        try {
            File file = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(intent, REQ_CAMERA);

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка камеры: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- СОЗДАНИЕ ФАЙЛА ----------
    private File createImageFile() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "CamYan");

        if (!dir.exists()) dir.mkdirs();

        File img = new File(dir, time + ".jpg");
        currentPath = img.getAbsolutePath();
        return img;
    }

    // ---------- РЕЗУЛЬТАТ КАМЕРЫ + BITMAP ----------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            Bitmap bmp = BitmapFactory.decodeFile(currentPath);

            if (bmp != null) {
                img.setImageBitmap(bmp);
                Toast.makeText(this, "Фото OK: " + currentPath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bitmap NULL", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
