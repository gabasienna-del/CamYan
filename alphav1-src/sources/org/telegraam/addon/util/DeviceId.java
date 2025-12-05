package org.telegraam.addon.util;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;

/* compiled from: DeviceId.kt */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\t"}, d2 = {"Lorg/telegraam/addon/util/DeviceId;", "", "()V", "KEY", "", "PREFS", "get", "context", "Landroid/content/Context;", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes7.dex */
public final class DeviceId {
    public static final DeviceId INSTANCE = new DeviceId();
    private static final String KEY = "device_id";
    private static final String PREFS = "lasthope.prefs";

    private DeviceId() {
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0040, code lost:
    
        if (r3 == null) goto L18;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.String get(android.content.Context r10) {
        /*
            r9 = this;
            java.lang.String r0 = "context"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r10, r0)
            java.lang.String r0 = "lasthope.prefs"
            r1 = 0
            android.content.SharedPreferences r0 = r10.getSharedPreferences(r0, r1)
            java.lang.String r2 = "device_id"
            r3 = 0
            java.lang.String r4 = r0.getString(r2, r3)
            r5 = r4
            java.lang.CharSequence r5 = (java.lang.CharSequence) r5
            r6 = 1
            if (r5 == 0) goto L1f
            boolean r5 = kotlin.text.StringsKt.isBlank(r5)
            if (r5 == 0) goto L20
        L1f:
            r1 = r6
        L20:
            if (r1 == 0) goto L56
        L23:
            android.content.ContentResolver r1 = r10.getContentResolver()     // Catch: java.lang.Exception -> L2e
            java.lang.String r5 = "android_id"
            java.lang.String r1 = android.provider.Settings.Secure.getString(r1, r5)     // Catch: java.lang.Exception -> L2e
            goto L30
        L2e:
            r1 = move-exception
            r1 = r3
        L30:
            if (r1 == 0) goto L42
            r5 = r1
            r7 = 0
            r8 = r5
            java.lang.CharSequence r8 = (java.lang.CharSequence) r8
            boolean r8 = kotlin.text.StringsKt.isBlank(r8)
            r6 = r6 ^ r8
            if (r6 == 0) goto L40
            r3 = r1
        L40:
            if (r3 != 0) goto L4a
        L42:
            java.util.UUID r3 = java.util.UUID.randomUUID()
            java.lang.String r3 = r3.toString()
        L4a:
            r4 = r3
            android.content.SharedPreferences$Editor r3 = r0.edit()
            android.content.SharedPreferences$Editor r2 = r3.putString(r2, r4)
            r2.apply()
        L56:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegraam.addon.util.DeviceId.get(android.content.Context):java.lang.String");
    }
}
