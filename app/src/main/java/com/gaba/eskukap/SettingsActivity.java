package com.gaba.eskukap;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.content.SharedPreferences;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences("eskukap", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad,pad,pad,pad);

        TextView info = new TextView(this);
        info.setText("Выбрана картинка:\n" + pref.getString("img","<не выбрано>"));
        layout.addView(info);

        Button select = new Button(this);
        select.setText("Выбрать JPEG");
        layout.addView(select);

        select.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/jpeg");
            startActivityForResult(pick,100);
        });

        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int req,int res, Intent data){
        super.onActivityResult(req,res,data);
        if(req==100 && res==RESULT_OK && data!=null){

            Uri uri = data.getData();
            if(uri!=null){

                pref.edit().putString("img", uri.toString()).apply();
                recreate(); // обновить UI
            }
        }
    }
}
