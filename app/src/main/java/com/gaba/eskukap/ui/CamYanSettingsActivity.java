package com.gaba.eskukap.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.gaba.eskukap.R;

public class CamYanSettingsActivity extends AppCompatActivity {

    public static final String KEY_FAKE_PHOTO_URI = "fake_photo_uri";
    private ImageView preview;
    private TextView info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camyan_settings);

        preview = findViewById(R.id.imagePreview);
        info = findViewById(R.id.imageInfo);

        updatePreview();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public void updatePreview() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String uriString = prefs.getString(KEY_FAKE_PHOTO_URI, null);

        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            preview.setImageURI(uri);
            info.setText("Выбрано фото:\n" + uri);
        } else {
            preview.setImageDrawable(null);
            info.setText("Фейковое фото не выбрано");
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final int REQ_PICK_IMAGE = 1001;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.camyan_prefs, rootKey);

            Preference choose =
                    findPreference("choose_fake_photo");
            if (choose != null) {
                choose.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQ_PICK_IMAGE);
                    return true;
                });
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQ_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri == null) return;

                // сохраняем постоянное разрешение на чтение
                final int flags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                requireContext().getContentResolver()
                        .takePersistableUriPermission(uri, flags);

                SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(requireContext());
                prefs.edit()
                        .putString(CamYanSettingsActivity.KEY_FAKE_PHOTO_URI, uri.toString())
                        .apply();

                if (getActivity() instanceof CamYanSettingsActivity) {
                    ((CamYanSettingsActivity) getActivity()).updatePreview();
                }
            }
        }
    }
}
