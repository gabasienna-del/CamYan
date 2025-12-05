package org.telegraam.addon.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.telegraam.addon.license.LicenseApi;
import org.telegraam.addon.util.DeviceId;

/* compiled from: LicenseManager.kt */
@Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0004J\u000e\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fJ\b\u0010\u0011\u001a\u00020\u0004H\u0002J\u000e\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0013"}, d2 = {"Lorg/telegraam/addon/license/LicenseManager;", "", "()V", "KEY_DEVICE", "", "KEY_EXPIRES_AT", "KEY_LAST_SEEN", "KEY_SERVER_URL", "KEY_TOKEN", "PREFS", "getServerUrl", "ctx", "Landroid/content/Context;", "init", "", LicenseManager.KEY_TOKEN, "isLicensed", "isoUtcNow", "sendHeartbeat", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class LicenseManager {
    public static final LicenseManager INSTANCE = new LicenseManager();
    private static final String KEY_DEVICE = "device_id";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_LAST_SEEN = "last_seen";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_TOKEN = "token";
    private static final String PREFS = "license";

    private LicenseManager() {
    }

    public final boolean isLicensed(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        SharedPreferences p = ctx.getSharedPreferences(PREFS, 0);
        return p.contains(KEY_TOKEN);
    }

    public final boolean init(Context ctx, String token) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(token, "token");
        KeyHelper.INSTANCE.ensureKeyPair();
        KeyHelper.INSTANCE.ensureEncKeyPair();
        String pub = KeyHelper.INSTANCE.getPublicKeyPem();
        LicenseApi.ApiResp resp = LicenseApi.INSTANCE.activate(ctx, token, pub);
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, 0);
        if (resp.getOk()) {
            String enc = resp.getEnc_base_url();
            if (enc != null) {
                try {
                    String url = KeyHelper.INSTANCE.rsaOaepDecryptBase64(enc);
                    String str = url;
                    if (!(str == null || str.length() == 0)) {
                        prefs.edit().putString(KEY_SERVER_URL, url).apply();
                    }
                } catch (Throwable th) {
                }
            }
            String deviceId = DeviceId.INSTANCE.get(ctx);
            SharedPreferences.Editor putLong = prefs.edit().putString(KEY_TOKEN, token).putString(KEY_DEVICE, deviceId).putLong(KEY_LAST_SEEN, System.currentTimeMillis());
            String expires_at = resp.getExpires_at();
            if (expires_at == null) {
                expires_at = "";
            }
            putLong.putString(KEY_EXPIRES_AT, expires_at).apply();
            return true;
        }
        prefs.edit().remove(KEY_TOKEN).remove(KEY_DEVICE).remove(KEY_LAST_SEEN).remove(KEY_EXPIRES_AT).remove(KEY_SERVER_URL).apply();
        return false;
    }

    public final String getServerUrl(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        SharedPreferences p = ctx.getSharedPreferences(PREFS, 0);
        return p.getString(KEY_SERVER_URL, null);
    }

    public final boolean sendHeartbeat(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, 0);
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null) {
            return false;
        }
        String deviceId = DeviceId.INSTANCE.get(ctx);
        String uuid = UUID.randomUUID().toString();
        Intrinsics.checkNotNullExpressionValue(uuid, "toString(...)");
        String nonce = StringsKt.replace$default(uuid, "-", "", false, 4, (Object) null);
        String now = isoUtcNow();
        String msg = token + "|" + deviceId + "|" + nonce + "|" + now;
        Signature $this$sendHeartbeat_u24lambda_u241 = Signature.getInstance("SHA256withECDSA");
        $this$sendHeartbeat_u24lambda_u241.initSign(KeyHelper.INSTANCE.getPrivateKey());
        byte[] bytes = msg.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        $this$sendHeartbeat_u24lambda_u241.update(bytes);
        String signature = Base64.encodeToString($this$sendHeartbeat_u24lambda_u241.sign(), 2);
        LicenseApi licenseApi = LicenseApi.INSTANCE;
        Intrinsics.checkNotNull(signature);
        LicenseApi.ApiResp resp = licenseApi.heartbeat(ctx, token, signature, nonce, now);
        if (!resp.getOk()) {
            return false;
        }
        SharedPreferences.Editor putLong = prefs.edit().putLong(KEY_LAST_SEEN, System.currentTimeMillis());
        String expires_at = resp.getExpires_at();
        if (expires_at == null) {
            expires_at = "";
        }
        putLong.putString(KEY_EXPIRES_AT, expires_at).apply();
        return true;
    }

    private final String isoUtcNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String format = sdf.format(new Date());
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }
}
