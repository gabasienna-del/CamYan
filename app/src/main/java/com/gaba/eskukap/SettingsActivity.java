package com.gaba.eskukap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SettingsActivity extends Activity {

    private static final int PICK = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btn = new Button(this);
        btn.setText("Выбрать фото (fake.jpg)");

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
            save(data.getData());
        }
    }

    private void save(Uri u) {
        try {
            File out = new File(Environment.getExternalStorageDirectory(), "fake.jpg");
            InputStream in = getContentResolver().openInputStream(u);
            FileOutputStream o = new FileOutputStream(out);

            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) o.write(b, 0, n);

            in.close();
            o.close();

            Toast.makeText(this, "Сохранено в /sdcard/fake.jpg", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e, Toast.LENGTH_LONG).show();
        }
    }
}
