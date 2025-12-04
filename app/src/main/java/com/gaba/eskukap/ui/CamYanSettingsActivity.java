package com.gaba.eskukap.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.gaba.eskukap.R;

public class CamYanSettingsActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1001;
    private static final String PREF_KEY_URI = "fake_photo_uri";

    private ImageView preview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camyansettings);

        Button btnSelect = findViewById(R.id.btn_select_photo);
        preview = findViewById(R.id.img_preview);

        // Показать уже сохранённое фото, если есть
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uriStr = prefs.getString(PREF_KEY_URI, null);
        if (uriStr != null) {
            Uri uri = Uri.parse(uriStr);
            preview.setImageURI(uri);
        }

        // Кнопка: открыть галерею
        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // Показываем превью
                preview.setImageURI(uri);

                // Сохраняем URI в настройках – его будет читать FakePhotoProvider
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putString(PREF_KEY_URI, uri.toString()).apply();
            }
        }
    }

    // Вспомогательный метод, если надо будет получать URI из кода/хука
    @Nullable
    public static Uri getSavedFakeImage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String uriStr = prefs.getString(PREF_KEY_URI, null);
        if (uriStr != null) {
            try {
                return Uri.parse(uriStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
