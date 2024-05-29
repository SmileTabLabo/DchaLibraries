package jp.co.benesse.touch.sbox;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
import jp.co.benesse.touch.util.Logger;
/* loaded from: classes.dex */
public class KeyStoreAdapter {
    protected final KeyStore mKeyStore;
    private static final String TAG = KeyStoreAdapter.class.getSimpleName();
    public static final String KEYSTORE_ALIAS = KeyStoreAdapter.class.getPackage().getName();

    public KeyStoreAdapter(Context context) {
        this.mKeyStore = loadKeyStore(context);
    }

    protected KeyStore loadKeyStore(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                createSecretKey(context, KEYSTORE_ALIAS);
            }
            return keyStore;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    protected SecretKey createSecretKey(Context context, String str) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(createKeyGenParameterSpec(context, str));
        return keyGenerator.generateKey();
    }

    protected KeyGenParameterSpec createKeyGenParameterSpec(Context context, String str) {
        return new KeyGenParameterSpec.Builder(str, 3).setBlockModes("CBC").setEncryptionPaddings("PKCS7Padding").setRandomizedEncryptionRequired(false).setCertificateSerialNumber(BigInteger.ONE).setCertificateSubject(new X500Principal(String.format("CN=%s", str))).build();
    }

    public String encryptString(String str) {
        KeyStore keyStore = this.mKeyStore;
        return keyStore != null ? encryptShaString(keyStore, KEYSTORE_ALIAS, str) : str;
    }

    public String decryptString(String str) {
        KeyStore keyStore = this.mKeyStore;
        return keyStore != null ? decryptShaString(keyStore, KEYSTORE_ALIAS, str) : str;
    }

    protected String encryptShaString(KeyStore keyStore, String str, String str2) {
        String str3 = BuildConfig.FLAVOR;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(1, (SecretKey) keyStore.getKey(str, null));
            str3 = Base64.encodeToString(cipher.doFinal(str2.getBytes(StandardCharsets.UTF_8)), 2);
            String encodeToString = Base64.encodeToString(cipher.getIV(), 2);
            return encodeToString + "@" + str3;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return str3;
        }
    }

    protected String decryptShaString(KeyStore keyStore, String str, String str2) {
        try {
            String[] split = str2.split("@");
            if (split.length != 2) {
                return BuildConfig.FLAVOR;
            }
            String str3 = split[0];
            String str4 = split[1];
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(2, (SecretKey) keyStore.getKey(str, null), new IvParameterSpec(Base64.decode(str3, 2)));
            return new String(cipher.doFinal(Base64.decode(str4, 2)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Logger.e(TAG, e);
            return BuildConfig.FLAVOR;
        }
    }
}
