package com.gaba.eskukap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.gaba.eskukap.BuildConfig;
import java.io.FileNotFoundException;

public class FakePhotoProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fakephoto";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/photo");

    private static final int CODE_PHOTO = 1;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static { MATCHER.addURI(AUTHORITY,"photo",CODE_PHOTO); }

    @Override public boolean onCreate(){ return true; }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri,@NonNull String mode)
            throws FileNotFoundException {

        if(MATCHER.match(uri)!=CODE_PHOTO) throw new FileNotFoundException();

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
        String img = p.getString("fake_photo_uri",null);
        if(img==null){ Log.e("FakePhoto","no image"); throw new FileNotFoundException(); }

        return getContext().getContentResolver().openFileDescriptor(Uri.parse(img),"r");
    }

    @Nullable @Override public Cursor query(Uri u,String[]p,String s,String[]a,String o){return null;}
    @Nullable @Override public String getType(Uri uri){return "image/jpeg";}
    @Nullable @Override public Uri insert(Uri u,ContentValues v){return null;}
    @Override public int delete(Uri u,String s,String[]a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[]a){return 0;}
}
