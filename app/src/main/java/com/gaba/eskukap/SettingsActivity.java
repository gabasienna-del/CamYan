package com.gaba.eskukap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public class SettingsActivity extends Activity {

    private static final int PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button b = new Button(this);
        b.setText("Выбрать фото для камеры (JPG)");
        b.setOnClickListener(v -> openGallery());
        setContentView(b);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) save(uri);
        }
    }

    private void save(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File dir = new File("/data/local/tmp/eskukap/");
            dir.mkdirs();
            File out = new File(dir,"frame.jpg");
            FileOutputStream fos = new FileOutputStream(out);

            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) > 0) fos.write(buf,0,r);

            is.close();
            fos.close();

            Toast.makeText(this,"Фото сохранено:\n"+out.getAbsolutePath(),Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,"Ошибка: "+e,Toast.LENGTH_LONG).show();
        }
    }
}
