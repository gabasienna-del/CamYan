package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.gaba.eskukap.BuildConfig;   // MUST EXIST after fix

import java.io.FileNotFoundException;

public class FakePhotoProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fakephoto";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/photo");

    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri){ return "image/jpeg"; }
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){ return null; }
    @Override public Uri insert(Uri u, ContentValues v){ return null; }
    @Override public int delete(Uri u,String s,String[] a){ return 0; }
    @Override public int update(Uri u,ContentValues v,String s,String[] a){ return 0; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new FileNotFoundException("Fake photo not implemented yet");
    }
}
