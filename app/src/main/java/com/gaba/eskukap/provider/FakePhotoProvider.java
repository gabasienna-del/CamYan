package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gaba.eskukap.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;

public class FakePhotoProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fakephoto";

    @Override
    public boolean onCreate() { return true; }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) { return "image/jpeg"; }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        File fake = new File(getContext().getFilesDir(),"fake.jpg");
        return ParcelFileDescriptor.open(fake, ParcelFileDescriptor.MODE_READ_ONLY);
    }
}
