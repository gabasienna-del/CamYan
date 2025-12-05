package com.gaba.eskukap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

public class SettingsActivity extends Activity {

    private static final int PICK = 101;
    // Итоговый путь, с которым работает хук:
    // /storage/emulated/0/Pictures/CamYan/fake.jpg

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btn = new Button(this);
        btn.setText("ВЫБРАТЬ ФОТО (FAKE.JPG)");

        btn.setOnClickListener(v -> pick());

        layout.addView(btn,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(layout);
    }

    private void pick() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK);
    }

    @Override
    protected void onActivityResult(int rq, int res, Intent data) {
        super.onActivityResult(rq, res, data);
        if (rq == PICK && res == RESULT_OK && data != null && data.getData() != null) {
            saveToPictures(data.getData());
        }
    }

    private void saveToPictures(Uri src) {
        try {
            // создаём/перезаписываем картинку в Pictures/CamYan/fake.jpg
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "fake.jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/CamYan");

            Uri dst = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (dst == null) {
                Toast.makeText(this, "Не удалось создать файл в Pictures", Toast.LENGTH_LONG).show();
                return;
            }

            InputStream in = getContentResolver().openInputStream(src);
            OutputStream out = getContentResolver().openOutputStream(dst, "w");

            if (in == null || out == null) {
                Toast.makeText(this, "Ошибка доступа к файлам", Toast.LENGTH_LONG).show();
                if (in != null) in.close();
                if (out != null) out.close();
                return;
            }

            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            in.close();
            out.flush();
            out.close();

            Toast.makeText(this,
                    "Сохранено в Pictures/CamYan/fake.jpg",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e, Toast.LENGTH_LONG).show();
        }
    }
}
