package org.telegraam.addon.util;

import android.content.ContentResolver;
import android.net.Uri;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: IO.kt */
@Metadata(d1 = {"\u0000\u001a\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007¨\u0006\b"}, d2 = {"copyUriToFile", "", "cr", "Landroid/content/ContentResolver;", "uri", "Landroid/net/Uri;", "out", "Ljava/io/File;", "app_debug"}, k = 2, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes7.dex */
public final class IOKt {
    public static final void copyUriToFile(ContentResolver cr, Uri uri, File out) {
        Intrinsics.checkNotNullParameter(cr, "cr");
        Intrinsics.checkNotNullParameter(uri, "uri");
        Intrinsics.checkNotNullParameter(out, "out");
        InputStream openInputStream = cr.openInputStream(uri);
        if (openInputStream != null) {
            FileOutputStream fileOutputStream = openInputStream;
            try {
                InputStream input = fileOutputStream;
                fileOutputStream = new FileOutputStream(out);
                try {
                    FileOutputStream output = fileOutputStream;
                    byte[] buf = new byte[16384];
                    while (true) {
                        int r = input.read(buf);
                        if (r == -1) {
                            Unit unit = Unit.INSTANCE;
                            CloseableKt.closeFinally(fileOutputStream, null);
                            Unit unit2 = Unit.INSTANCE;
                            CloseableKt.closeFinally(fileOutputStream, null);
                            return;
                        }
                        output.write(buf, 0, r);
                    }
                } finally {
                }
            } catch (Throwable th) {
                try {
                    throw th;
                } finally {
                }
            }
        }
    }
}
