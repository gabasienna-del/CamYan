package com.gaba.eskukap.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

// ВАЖНО: BuildConfig импорт
import com.gaba.eskukap.BuildConfig;

public class FakePhotoProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fakephoto";

    @Nullable
    public static Uri getFakeImage(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                null,null,null
        );

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        }
        return null;
    }
}
