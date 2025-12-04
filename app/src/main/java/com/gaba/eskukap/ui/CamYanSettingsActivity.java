package com.gaba.eskukap.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.net.Uri;

import java.io.IOException;

public class CamYanSettingsActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button select = findViewById(R.id.btn_select);
        Button start = findViewById(R.id.btn_start);
        Button stop = findViewById(R.id.btn_stop);
        img = findViewById(R.id.img_preview);

        select.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        start.setOnClickListener(v -> {
            // TODO: сохранить путь в SharedPref или в файл
        });

        stop.setOnClickListener(v -> {
            // TODO: сбросить фото
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                img.setImageBitmap(bitmap);
            } catch(IOException e){ e.printStackTrace(); }
        }
    }
}
