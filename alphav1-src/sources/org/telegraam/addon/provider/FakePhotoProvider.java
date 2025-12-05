package org.telegraam.addon.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.io.FileNotFoundException;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.telegraam.addon.license.LicenseManager;

/* compiled from: FakePhotoProvider.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u0000 !2\u00020\u0001:\u0001!B\u0005¢\u0006\u0002\u0010\u0002J1\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0010\u0010\t\u001a\f\u0012\u0006\b\u0001\u0012\u00020\b\u0018\u00010\nH\u0016¢\u0006\u0002\u0010\u000bJ\b\u0010\f\u001a\u00020\rH\u0002J\u0012\u0010\u000e\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001c\u0010\u000f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0016J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\b\u0010\u0014\u001a\u00020\u0013H\u0016J\u001a\u0010\u0015\u001a\u0004\u0018\u00010\u00162\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\bH\u0016JO\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0005\u001a\u00020\u00062\u0010\u0010\u001a\u001a\f\u0012\u0006\b\u0001\u0012\u00020\b\u0018\u00010\n2\b\u0010\u001b\u001a\u0004\u0018\u00010\b2\u0010\u0010\u001c\u001a\f\u0012\u0006\b\u0001\u0012\u00020\b\u0018\u00010\n2\b\u0010\u001d\u001a\u0004\u0018\u00010\bH\u0016¢\u0006\u0002\u0010\u001eJ;\u0010\u001f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0010\u0010\t\u001a\f\u0012\u0006\b\u0001\u0012\u00020\b\u0018\u00010\nH\u0016¢\u0006\u0002\u0010 ¨\u0006\""}, d2 = {"Lorg/telegraam/addon/provider/FakePhotoProvider;", "Landroid/content/ContentProvider;", "()V", "delete", "", "uri", "Landroid/net/Uri;", "where", "", "args", "", "(Landroid/net/Uri;Ljava/lang/String;[Ljava/lang/String;)I", "ensureLicensed", "", "getType", "insert", "values", "Landroid/content/ContentValues;", "isEnabled", "", "onCreate", "openFile", "Landroid/os/ParcelFileDescriptor;", "mode", "query", "Landroid/database/Cursor;", "projection", "selection", "selectionArgs", "sortOrder", "(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;", "update", "(Landroid/net/Uri;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I", "Companion", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes5.dex */
public final class FakePhotoProvider extends ContentProvider {
    public static final String AUTH = "org.telegraam.addon.provider";
    private static final int CODE_PHOTO = 1;
    private static final int CODE_STATE = 2;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final String KEY_ENABLED = "enabled";
    private static final String PATH_PHOTO = "photo";
    private static final String PATH_STATE = "state";
    private static final Uri PHOTO_URI;
    private static final String PREFS_SWITCH = "lasthope";
    private static final Uri STATE_URI;
    private static final UriMatcher matcher;

    /* compiled from: FakePhotoProvider.kt */
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u0011\u0010\u000b\u001a\u00020\f¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u0011\u0010\u0010\u001a\u00020\f¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0014"}, d2 = {"Lorg/telegraam/addon/provider/FakePhotoProvider$Companion;", "", "()V", "AUTH", "", "CODE_PHOTO", "", "CODE_STATE", "KEY_ENABLED", "PATH_PHOTO", "PATH_STATE", "PHOTO_URI", "Landroid/net/Uri;", "getPHOTO_URI", "()Landroid/net/Uri;", "PREFS_SWITCH", "STATE_URI", "getSTATE_URI", "matcher", "Landroid/content/UriMatcher;", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    /* loaded from: classes5.dex */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final Uri getPHOTO_URI() {
            return FakePhotoProvider.PHOTO_URI;
        }

        public final Uri getSTATE_URI() {
            return FakePhotoProvider.STATE_URI;
        }
    }

    static {
        Uri parse = Uri.parse("content://org.telegraam.addon.provider/photo");
        Intrinsics.checkNotNullExpressionValue(parse, "parse(...)");
        PHOTO_URI = parse;
        Uri parse2 = Uri.parse("content://org.telegraam.addon.provider/state");
        Intrinsics.checkNotNullExpressionValue(parse2, "parse(...)");
        STATE_URI = parse2;
        UriMatcher $this$matcher_u24lambda_u241 = new UriMatcher(-1);
        $this$matcher_u24lambda_u241.addURI(AUTH, PATH_PHOTO, 1);
        $this$matcher_u24lambda_u241.addURI(AUTH, PATH_STATE, 2);
        matcher = $this$matcher_u24lambda_u241;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    private final void ensureLicensed() {
        Context ctx = getContext();
        if (ctx == null) {
            throw new SecurityException("No context");
        }
        if (!LicenseManager.INSTANCE.isLicensed(ctx)) {
            throw new SecurityException("License required");
        }
    }

    private final boolean isEnabled() {
        Context ctx = getContext();
        if (ctx == null) {
            return false;
        }
        return ctx.getSharedPreferences(PREFS_SWITCH, 0).getBoolean(KEY_ENABLED, false);
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        if (matcher.match(uri) == 2) {
            ensureLicensed();
            boolean isEnabled = isEnabled();
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{KEY_ENABLED});
            matrixCursor.addRow(new Integer[]{Integer.valueOf(isEnabled ? 1 : 0)});
            return matrixCursor;
        }
        return null;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        switch (matcher.match(uri)) {
            case 1:
                return "image/jpeg";
            case 2:
                return "vnd.android.cursor.item/vnd.org.telegraam.addon.provider.state";
            default:
                return null;
        }
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        Intrinsics.checkNotNullParameter(mode, "mode");
        if (matcher.match(uri) != 1) {
            throw new FileNotFoundException();
        }
        ensureLicensed();
        if (!isEnabled()) {
            throw new FileNotFoundException("substitution disabled");
        }
        Context context = getContext();
        Intrinsics.checkNotNull(context);
        File f = new File(context.getFilesDir(), "fake.jpg");
        if (!f.exists()) {
            throw new FileNotFoundException("fake.jpg not set");
        }
        return ParcelFileDescriptor.open(f, 268435456);
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        return null;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String where, String[] args) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        return 0;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String where, String[] args) {
        Intrinsics.checkNotNullParameter(uri, "uri");
        return 0;
    }
}
