package com.gaba.eskukap.ui;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.gaba.eskukap.R;

public class CamYanSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camyan_settings);

        ImageView preview = findViewById(R.id.previewImage);

        // безопасная загрузка (без чтения content://, чтобы не падало)
        if (preview != null) {
            preview.setImageResource(R.mipmap.ic_launcher); // заменишь позже на свою картинку
        }
    }
}
