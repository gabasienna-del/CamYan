package org.telegraam.addon.license;

import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.Cipher;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: KeyHelper.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0006\u001a\u00020\u0007J\u0006\u0010\b\u001a\u00020\tJ\n\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0002J\u0006\u0010\f\u001a\u00020\u0004J\u0006\u0010\r\u001a\u00020\u000bJ\u0006\u0010\u000e\u001a\u00020\u0004J\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0010\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0011"}, d2 = {"Lorg/telegraam/addon/license/KeyHelper;", "", "()V", "ENC_KEY_ALIAS", "", "SIGN_KEY_ALIAS", "ensureEncKeyPair", "", "ensureKeyPair", "Ljava/security/KeyPair;", "getEncPrivateKey", "Ljava/security/PrivateKey;", "getEncPublicKeyPem", "getPrivateKey", "getPublicKeyPem", "rsaOaepDecryptBase64", "ciphertextB64", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class KeyHelper {
    private static final String ENC_KEY_ALIAS = "lasthope_enc_key";
    public static final KeyHelper INSTANCE = new KeyHelper();
    private static final String SIGN_KEY_ALIAS = "lasthope_license";

    private KeyHelper() {
    }

    public final KeyPair ensureKeyPair() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (!ks.containsAlias(SIGN_KEY_ALIAS)) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "AndroidKeyStore");
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(SIGN_KEY_ALIAS, 12).setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")).setDigests("SHA-256").build();
            Intrinsics.checkNotNullExpressionValue(spec, "build(...)");
            kpg.initialize(spec);
            KeyPair generateKeyPair = kpg.generateKeyPair();
            Intrinsics.checkNotNullExpressionValue(generateKeyPair, "generateKeyPair(...)");
            return generateKeyPair;
        }
        Key key = ks.getKey(SIGN_KEY_ALIAS, null);
        Intrinsics.checkNotNull(key, "null cannot be cast to non-null type java.security.PrivateKey");
        PrivateKey priv = (PrivateKey) key;
        PublicKey pub = ks.getCertificate(SIGN_KEY_ALIAS).getPublicKey();
        return new KeyPair(pub, priv);
    }

    public final PrivateKey getPrivateKey() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        Key key = ks.getKey(SIGN_KEY_ALIAS, null);
        Intrinsics.checkNotNull(key, "null cannot be cast to non-null type java.security.PrivateKey");
        return (PrivateKey) key;
    }

    public final String getPublicKeyPem() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        Certificate cert = ks.getCertificate(SIGN_KEY_ALIAS);
        PublicKey pub = cert.getPublicKey();
        String encoded = Base64.encodeToString(pub.getEncoded(), 2);
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }

    public final void ensureEncKeyPair() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (ks.containsAlias(ENC_KEY_ALIAS)) {
            return;
        }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(ENC_KEY_ALIAS, 2).setKeySize(2048).setDigests("SHA-256").setEncryptionPaddings("OAEPPadding").build();
        Intrinsics.checkNotNullExpressionValue(spec, "build(...)");
        kpg.initialize(spec);
        kpg.generateKeyPair();
    }

    public final String getEncPublicKeyPem() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        Certificate cert = ks.getCertificate(ENC_KEY_ALIAS);
        PublicKey pub = cert.getPublicKey();
        String encoded = Base64.encodeToString(pub.getEncoded(), 2);
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }

    private final PrivateKey getEncPrivateKey() {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(ENC_KEY_ALIAS, null);
        if (entry == null) {
            return null;
        }
        KeyStore.PrivateKeyEntry privateKeyEntry = entry instanceof KeyStore.PrivateKeyEntry ? (KeyStore.PrivateKeyEntry) entry : null;
        if (privateKeyEntry != null) {
            return privateKeyEntry.getPrivateKey();
        }
        return null;
    }

    public final String rsaOaepDecryptBase64(String ciphertextB64) {
        Intrinsics.checkNotNullParameter(ciphertextB64, "ciphertextB64");
        PrivateKey priv = getEncPrivateKey();
        if (priv == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(2, priv);
        byte[] ct = Base64.decode(ciphertextB64, 0);
        byte[] plain = cipher.doFinal(ct);
        Intrinsics.checkNotNull(plain);
        return new String(plain, Charsets.UTF_8);
    }
}
