package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

public class FakePhotoProvider extends ContentProvider {

    // Жёстко задаём authority, без BuildConfig
    public static final String AUTHORITY = "com.gaba.eskukap.fakephoto";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/photo");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "image/jpeg";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri,
                                         @NonNull String mode)
            throws FileNotFoundException {

        // Тестовый файл fake.jpg в internal storage приложения
        File fake = new File(getContext().getFilesDir(), "fake.jpg");
        if (!fake.exists()) {
            throw new FileNotFoundException("fake.jpg not found");
        }
        return ParcelFileDescriptor.open(fake,
                ParcelFileDescriptor.MODE_READ_ONLY);
    }
}
