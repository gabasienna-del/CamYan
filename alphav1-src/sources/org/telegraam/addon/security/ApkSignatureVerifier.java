package org.telegraam.addon.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.security.MessageDigest;
import java.util.Arrays;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.StringsKt;

/* compiled from: ApkSignatureVerifier.kt */
@Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0004H\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0010\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0010"}, d2 = {"Lorg/telegraam/addon/security/ApkSignatureVerifier;", "", "()V", "EXPECTED_SHA256_HEX", "", "currentCertSha256Hex", "pm", "Landroid/content/pm/PackageManager;", "packageName", "isSignatureValid", "", "ctx", "Landroid/content/Context;", "sha256Hex", "bytes", "", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class ApkSignatureVerifier {
    private static final String EXPECTED_SHA256_HEX = "42A928DA62FC241AB28F6BA95DAF4C6E6341070429077364185C06560A659CB0";
    public static final ApkSignatureVerifier INSTANCE = new ApkSignatureVerifier();

    private ApkSignatureVerifier() {
    }

    public final boolean isSignatureValid(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        PackageManager pm = ctx.getPackageManager();
        String pkg = ctx.getPackageName();
        Intrinsics.checkNotNull(pm);
        Intrinsics.checkNotNull(pkg);
        String hex = currentCertSha256Hex(pm, pkg);
        if (hex == null) {
            return false;
        }
        return StringsKt.equals(hex, EXPECTED_SHA256_HEX, true);
    }

    private final String currentCertSha256Hex(PackageManager pm, String packageName) {
        String sha256Hex;
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                PackageInfo pi = pm.getPackageInfo(packageName, 134217728);
                Intrinsics.checkNotNullExpressionValue(pi, "getPackageInfo(...)");
                Signature[] apkContentsSigners = pi.signingInfo.getApkContentsSigners();
                Intrinsics.checkNotNullExpressionValue(apkContentsSigners, "getApkContentsSigners(...)");
                byte[] sig = ((Signature) ArraysKt.first(apkContentsSigners)).toByteArray();
                Intrinsics.checkNotNull(sig);
                sha256Hex = sha256Hex(sig);
            } else {
                PackageInfo pi2 = pm.getPackageInfo(packageName, 64);
                Intrinsics.checkNotNullExpressionValue(pi2, "getPackageInfo(...)");
                Signature[] signatures = pi2.signatures;
                Intrinsics.checkNotNullExpressionValue(signatures, "signatures");
                byte[] sig2 = ((Signature) ArraysKt.first(signatures)).toByteArray();
                Intrinsics.checkNotNull(sig2);
                sha256Hex = sha256Hex(sig2);
            }
            return sha256Hex;
        } catch (Throwable th) {
            return null;
        }
    }

    private final String sha256Hex(byte[] bytes) {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder sb = new StringBuilder(d.length * 2);
        Intrinsics.checkNotNull(d);
        for (byte b : d) {
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
            Intrinsics.checkNotNullExpressionValue(format, "format(...)");
            sb.append(format);
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
        return sb2;
    }
}
