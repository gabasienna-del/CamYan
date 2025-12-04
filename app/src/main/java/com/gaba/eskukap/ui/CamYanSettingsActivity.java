package com.gaba.eskukap.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.gaba.eskukap.R;

public class CamYanSettingsActivity extends AppCompatActivity {

    ImageView preview;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_camyan_settings);
        preview=findViewById(R.id.imagePreview);
        update();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container,new Frag()).commit();
    }

    void update(){
        String uri=PreferenceManager.getDefaultSharedPreferences(this).getString("fake_photo_uri",null);
        if(uri!=null) preview.setImageURI(Uri.parse(uri));
    }

    public static class Frag extends PreferenceFragmentCompat{
        int REQ=2000;

        @Override public void onCreatePreferences(Bundle b,String k){
            setPreferencesFromResource(R.xml.camyan_prefs,k);

            findPreference("choose_fake_photo").setOnPreferenceClickListener(p->{
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("image/*");
                startActivityForResult(i,REQ);
                return true;
            });
        }

        @Override public void onActivityResult(int r,int c,Intent d){
            super.onActivityResult(r,c,d);
            if(r==REQ && c==AppCompatActivity.RESULT_OK && d!=null){
                Uri u=d.getData();
                if(u!=null){
                    requireContext().getContentResolver()
                            .takePersistableUriPermission(u,d.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION|
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

                    SharedPreferences p=PreferenceManager.getDefaultSharedPreferences(requireContext());
                    p.edit().putString("fake_photo_uri",u.toString()).apply();
                    ((CamYanSettingsActivity)getActivity()).update();
                }
            }
        }
    }
}
