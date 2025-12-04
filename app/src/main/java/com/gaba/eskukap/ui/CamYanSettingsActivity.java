package com.gaba.eskukap.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gaba.eskukap.R;

import java.io.IOException;

public class CamYanSettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView imgPreview;
    private TextView tvStatus;
    private Uri selectedUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camyan_settings);

        imgPreview = findViewById(R.id.img_preview);
        tvStatus   = findViewById(R.id.tv_status);

        Button btnPick  = findViewById(R.id.btn_pick);
        Button btnSave  = findViewById(R.id.btn_save);

        // загружаем после запуска
        SharedPreferences prefs = getSharedPreferences("camyan_prefs", MODE_PRIVATE);
        String saved = prefs.getString("fake_image_uri", null);

        if (saved != null) {
            selectedUri = Uri.parse(saved);
            imgPreview.setImageURI(selectedUri);
            tvStatus.setText("Фото выбрано");
        } else {
            tvStatus.setText("Фото не выбрано");
        }

        // кнопка выбора
        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        // кнопка сохранить
        btnSave.setOnClickListener(v -> {
            if (selectedUri != null) {
                prefs.edit().putString("fake_image_uri", selectedUri.toString()).apply();
                tvStatus.setText("Сохранено ✓");
            } else {
                tvStatus.setText("Сначала выбери фото");
            }
        });
    }

    // БЕЗ return — безопасно через вложенный if
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                selectedUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                    imgPreview.setImageBitmap(bitmap);
                    tvStatus.setText("Фото выбрано ✓");
                } catch (IOException e) {
                    tvStatus.setText("Ошибка загрузки фото");
                }
            } else {
                tvStatus.setText("Фото не выбрано");
            }
        }
    }
}
