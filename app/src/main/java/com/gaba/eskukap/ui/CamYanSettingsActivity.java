package com.gaba.eskukap.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.gaba.eskukap.R;

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (is != null) {
                    preview.setImageBitmap(BitmapFactory.decodeStream(is));
                    is.close();
                }
            } catch (Exception ignored) {}
        }
    }
}
