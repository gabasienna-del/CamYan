package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.gaba.eskukap.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;

public class FakePhotoProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fakephoto";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/photo");

    private static final int CODE_PHOTO = 1;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "photo", CODE_PHOTO);
    }

    @Override
    public boolean onCreate() {
        // Ничего не инициализируем, просто возвращаем true
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Всегда отдаём JPEG
        return "image/jpeg";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Пока не реализуем, модулю это не нужно
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // Заглушка: файл не найден. Позже сюда подставим реальное фото.
        throw new FileNotFoundException("No fake photo yet");
    }
}
