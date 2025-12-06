package com.gaba.eskukap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.net.Uri;
import android.content.SharedPreferences;

public class SetImage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Uri uri = Uri.parse("file:///sdcard/DCIM/test.jpg"); // <-- путь к фото
        SharedPreferences sp = getSharedPreferences("eskukap", MODE_PRIVATE);
        sp.edit().putString("img", uri.toString()).apply();

        Toast.makeText(this,"Image set!",Toast.LENGTH_LONG).show();
        finish();
    }
}
