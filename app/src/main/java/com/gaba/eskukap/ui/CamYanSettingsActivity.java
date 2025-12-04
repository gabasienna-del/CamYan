package com.gaba.eskukap.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;

import java.io.InputStream;

public class CamYanSettingsActivity extends Activity {

    private static final int PICK_IMAGE = 1001;
    private ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camyansettings);

        Button select = findViewById(R.id.btn_select_photo);
        preview = findViewById(R.id.img_preview);

        select.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                preview.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (Exception ignored) {}
        }
    }
}
