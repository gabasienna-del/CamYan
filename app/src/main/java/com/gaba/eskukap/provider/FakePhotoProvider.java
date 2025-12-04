package com.gaba.eskukap.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

public class FakePhotoProvider {

    private static final String PREF_KEY_URI = "fake_photo_uri";

    @Nullable
    public static Uri getFakeImage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String uriStr = prefs.getString(PREF_KEY_URI, null);
        if (uriStr != null && !uriStr.isEmpty()) {
            try {
                return Uri.parse(uriStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
