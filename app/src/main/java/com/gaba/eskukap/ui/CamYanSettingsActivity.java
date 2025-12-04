package com.gaba.eskukap.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.gaba.eskukap.R;

import java.io.IOException;

public class CamYanSettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // наш layout: app/src/main/res/layout/activity_settings.xml
        setContentView(R.layout.activity_settings);

        Button select = findViewById(R.id.btn_select);
        Button start  = findViewById(R.id.btn_start);
        Button stop   = findViewById(R.id.btn_stop);
        img = findViewById(R.id.img_preview);

        // выбор фото из галереи
        select.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            startActivityForResult(intent, PICK_IMAGE);
        });

        // пока заглушки — потом сюда добавим сохранение Uri
        start.setOnClickListener(v -> {
            // TODO: сохранить Uri выбранного фото для хука
        });

        stop.setOnClickListener(v -> {
            img.setImageDrawable(null);
            // TODO: очистить сохранённый Uri
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), uri);
                img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
