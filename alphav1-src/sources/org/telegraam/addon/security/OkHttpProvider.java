package org.telegraam.addon.security;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.OkHttpClient;
import org.telegraam.addon.R;

/* compiled from: OkHttpProvider.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\n"}, d2 = {"Lorg/telegraam/addon/security/OkHttpProvider;", "", "()V", "DEFAULT_URL", "", "getServerBaseUrl", "ctx", "Landroid/content/Context;", "newPinnedClient", "Lokhttp3/OkHttpClient;", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class OkHttpProvider {
    private static final String DEFAULT_URL = "https://149.104.105.41:8443";
    public static final OkHttpProvider INSTANCE = new OkHttpProvider();

    private OkHttpProvider() {
    }

    public final String getServerBaseUrl(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        SharedPreferences prefs = ctx.getSharedPreferences("license", 0);
        String string = prefs.getString("server_url", DEFAULT_URL);
        return string == null ? DEFAULT_URL : string;
    }

    public final OkHttpClient newPinnedClient(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = ctx.getResources().openRawResource(R.raw.license_server);
        Intrinsics.checkNotNullExpressionValue(caInput, "openRawResource(...)");
        InputStream inputStream = caInput;
        try {
            InputStream it = inputStream;
            Certificate ca = cf.generateCertificate(it);
            CloseableKt.closeFinally(inputStream, null);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("server", ca);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            Intrinsics.checkNotNullExpressionValue(trustManagers, "getTrustManagers(...)");
            Object first = ArraysKt.first(trustManagers);
            Intrinsics.checkNotNull(first, "null cannot be cast to non-null type javax.net.ssl.X509TrustManager");
            X509TrustManager trustManager = (X509TrustManager) first;
            SSLContext $this$newPinnedClient_u24lambda_u243 = SSLContext.getInstance("TLS");
            $this$newPinnedClient_u24lambda_u243.init(null, new X509TrustManager[]{trustManager}, new SecureRandom());
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            SSLSocketFactory socketFactory = $this$newPinnedClient_u24lambda_u243.getSocketFactory();
            Intrinsics.checkNotNullExpressionValue(socketFactory, "getSocketFactory(...)");
            return builder.sslSocketFactory(socketFactory, trustManager).hostnameVerifier(new HostnameVerifier() { // from class: org.telegraam.addon.security.OkHttpProvider$$ExternalSyntheticLambda0
                @Override // javax.net.ssl.HostnameVerifier
                public final boolean verify(String str, SSLSession sSLSession) {
                    boolean newPinnedClient$lambda$4;
                    newPinnedClient$lambda$4 = OkHttpProvider.newPinnedClient$lambda$4(str, sSLSession);
                    return newPinnedClient$lambda$4;
                }
            }).connectTimeout(15L, TimeUnit.SECONDS).readTimeout(15L, TimeUnit.SECONDS).writeTimeout(15L, TimeUnit.SECONDS).build();
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean newPinnedClient$lambda$4(String str, SSLSession session) {
        return Intrinsics.areEqual(session.getPeerHost(), "149.104.105.41");
    }
}
