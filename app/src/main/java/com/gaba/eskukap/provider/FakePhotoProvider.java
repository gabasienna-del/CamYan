package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class FakePhotoProvider extends ContentProvider {

    public static final String AUTHORITY = "com.gaba.eskukap.fakephoto";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/image");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null; // позже заменим на выдачу выбранной картинки
    }

    @Nullable @Override
    public String getType(@NonNull Uri uri) {
        return "image/jpeg";
    }

    @Nullable @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) { return 0; }
}
