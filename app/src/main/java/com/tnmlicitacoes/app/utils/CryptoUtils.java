package com.tnmlicitacoes.app.utils;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 * Utility class for encrypt and decrypt sensitive data
 * See for more info:
 * https://devliving.online/securely-store-preference-data-in-android/
 * https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3
 */

public class CryptoUtils {

    /* The logging tag */
    private static final String TAG = "CryptoUtils";

    /* AndroidKeyStore provider */
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";

    /* AndroidOpenSSL provider used to store RSA keys */
    private static final String KEYSTORE_OPENSSL_PROVIDER = "AndroidOpenSSL";

    /* This is the general alias used to generate or get keys from the KeyStore */
    private static final String KEY_ALIAS = "TnmKey";

    /* This is the alias used to generate or get RSA keys from the KeyStore */
    private static final String RSA_KEY_ALIAS = "TnmRSAKey";

    /**
     *  The cipher algorithm mode for encrypting and decrypting data
     *  used only in the Marshmallow and higher devices
     */
    private static final String AES_MODE = "AES/GCM/NoPadding";

    /**
     * The cipher algorithm mode for encrypting and decrypting the RSA keys
     * used only in pre-Marshmallow devices.
     */
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";

    /* This class instance */
    private static CryptoUtils sInstance;

    /*
     * Initialization vector used by cipher. It is recommended to use random IV but if we do that
     * every time we need to refresh the accessToken we would need to get the saved refreshToken, decrypt
     * it with the old IV, encrypt again (generating a new IV), then save both encrypted with
     * the new IV.
     *
     * One string is probably not too much work, but imagine if we have 100 strings encrypted with
     * the old IV, we would need to decrypt all them first and encrypt again.
     *
     * One solution to this would be storing multiple IVs for each type of sensitive data that we
     * want to encrypt and in decryption routine we'd just need to get the IV for the specified type.
     *
     * But, since we are only encrypting and decrypting two tokens we will be using just one IV.
     * */
    private byte[] mIV;

    /* The KeyStore engine object */
    private KeyStore mKeyStore;

    /* Indicates whether the user is using a Marshmallow or higher device or not */
    private boolean mIsMarshmallowOrHigher = false;

    /**
     * Returns an instance of this class
     */
    public static synchronized CryptoUtils getInstance() {
        if (sInstance == null) {
            sInstance = new CryptoUtils();
        }
        return sInstance;
    }

    /**
     * Initializes the KeyStore engine
     */
    private void initializeKeyStore() throws Exception {
        mKeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        mKeyStore.load(null);
    }

    /**
     * Gets a KeyGenerator object for AES algorithm and AndroidKeyStore provider
     * @return a KeyGenerator object
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private KeyGenerator getKeyGenerator() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
    }

    private KeyPairGenerator getKeyPairGenerator() throws Exception {
        KeyPairGenerator result = null;
        try {
            result = KeyPairGenerator.getInstance("RSA", KEYSTORE_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Gets a SecretKey from the KeyStore or generate one if it doesn't exists.
     * @return a SecretKey
     */
    private SecretKey getKey(Context context) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getAESKeyMarshmallowAndHigher();
        } else {
            return new SecretKeySpec(getAESKey(context), "AES");
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private SecretKey getAESKeyMarshmallowAndHigher() throws Exception {
        SecretKey result = null;
        KeyGenerator keyGenerator = null;

        // Check first if we already has a key with the KEY_ALIAS stored
        if (mKeyStore.containsAlias(KEY_ALIAS) && mKeyStore.entryInstanceOf(KEY_ALIAS,
                KeyStore.SecretKeyEntry.class)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry)
                    mKeyStore.getEntry(KEY_ALIAS, null);
            result = entry.getSecretKey();
            LOG_DEBUG(TAG, "Found a key!");
        } else {
            keyGenerator = getKeyGenerator();
            if (keyGenerator != null) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build());
                result = keyGenerator.generateKey();
                LOG_DEBUG(TAG, "Generated a new key!");
            }
        }

        return result;
    }

    /**
     * Gets RSA keys from store or generate new ones
     * @return the KeyPair object with publicKey and privateKey
     */
    private KeyPair getRSAKeys(Context context) throws Exception {
        KeyPair result = null;
        KeyPairGenerator keyPairGenerator = null;

        // Check first if we already has a key with the RSA_KEY_ALIAS stored
        if (mKeyStore.containsAlias(RSA_KEY_ALIAS) && mKeyStore.entryInstanceOf(RSA_KEY_ALIAS,
                KeyStore.PrivateKeyEntry.class)) {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(RSA_KEY_ALIAS, null);
            result = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        } else {
            keyPairGenerator = getKeyPairGenerator();
            if (keyPairGenerator != null) {

                Calendar start = Calendar.getInstance();
                Calendar end   = Calendar.getInstance();
                // Keys is valid for 30 years
                end.add(Calendar.YEAR, 30);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(RSA_KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + RSA_KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                keyPairGenerator.initialize(spec);

                result = keyPairGenerator.generateKeyPair();
            }
        }

        return result;
    }

    /**
     * Encrypt the given input byte array
     */
    public byte[] encrypt(Context context, byte[] input) throws Exception {
        byte[] result = null;

        LOG_DEBUG(TAG, "(encrypt) Input: " + new String(input));

        Cipher cipher = Cipher.getInstance(AES_MODE);

        mIV = getSavedIV(context);
        if (mIV == null) {
            cipher.init(Cipher.ENCRYPT_MODE, getKey(context));
            mIV = cipher.getIV();
            saveIV(context, mIV);
        } else {
            if (mIsMarshmallowOrHigher) {
                cipher.init(Cipher.ENCRYPT_MODE, getKey(context), new GCMParameterSpec(128, mIV));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, getKey(context), new IvParameterSpec(mIV));
            }
        }

        result = cipher.doFinal(input);

        LOG_DEBUG(TAG, "(encrypt) Output: " + new String(result));

        return result;
    }

    /**
     * Decrypts the given byte array input
     */
    public byte[] decrypt(Context context, byte[] input) throws Exception {
        byte[] result = null;

        LOG_DEBUG(TAG, "(decrypt) Input: " + new String(input));

        mIV = getSavedIV(context);
        if (mIV == null) {
            return null;
        }

        Cipher cipher = Cipher.getInstance(AES_MODE);
        if (mIsMarshmallowOrHigher) {
            cipher.init(Cipher.DECRYPT_MODE, getKey(context), new GCMParameterSpec(128, mIV));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, getKey(context), new IvParameterSpec(mIV));
        }
        result = cipher.doFinal(input);

        LOG_DEBUG(TAG, "(decrypt) Output: " + new String(result));

        return result;
    }

    /**
     * This method is used to encrypt the AES key used to encrypt the data in pre-Marshmallow devices
     * @param input The AES key to be encrypted
     * @return returns the encrypted key in bytes
     */
    private byte[] RSAEncrypt(byte[] input, PublicKey publicKey) throws Exception {
        byte[] result;

        Cipher cipher = Cipher.getInstance(RSA_MODE, KEYSTORE_OPENSSL_PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
        cipherOutputStream.write(input);
        cipherOutputStream.close();

        result = outputStream.toByteArray();

        return result;
    }

    /**
     * This method is used to decrypt the encrypted AES key used to encrypt the data in pre-Marshmallow devices
     * @param encryptedInput the AES encrypted key to be decrypted
     * @return returns the decrypted key in bytes
     */
    private byte[] RSADecrypt(byte[] encryptedInput, PrivateKey privateKey) throws Exception {
        byte[] result;

        Cipher cipher = Cipher.getInstance(RSA_MODE, KEYSTORE_OPENSSL_PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encryptedInput), cipher
        );

        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        result = new byte[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i);
        }

        cipherInputStream.close();

        return result;
    }


    /**
     * Generates a new key or get one from store
     */
    private byte[] getAESKey(Context context) throws Exception {
        byte[] result = null;

        // Load persisted encrypted key in preferences as Base64 string
        String encryptedBase64 = SettingsUtils.getAesKey(context);
        KeyPair pair = getRSAKeys(context);
        if (encryptedBase64 != null) {
            byte[] decoded = Base64.decode(encryptedBase64, Base64.DEFAULT);
            result = RSADecrypt(decoded, pair.getPrivate());
            LOG_DEBUG(TAG, "Found key: " + encryptedBase64);
        } else {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            result = keyGen.generateKey().getEncoded();

            // Persist encrypted key in preferences as Base64 string
            byte[] encrypted = RSAEncrypt(result, pair.getPublic());
            SettingsUtils.putString(context, "aes_key",
                    Base64.encodeToString(encrypted, Base64.DEFAULT));

            LOG_DEBUG(TAG, "Generated key: " + Base64.encodeToString(encrypted, Base64.DEFAULT));
        }
        return result;
    }

    /**
     * Get the saved IV
     */
    private byte[] getSavedIV(Context context) {
        byte[] result = null;
        String encodedBase64IV = SettingsUtils.getIV(context);
        if (encodedBase64IV != null) {
            result = Base64.decode(encodedBase64IV, Base64.DEFAULT);
            LOG_DEBUG(TAG, "Encoded Base64 IV: " + encodedBase64IV);
        }
        return result;
    }

    /**
     * Saves the IV
     */
    private void saveIV(Context context, byte[] IV) {
        SettingsUtils.putString(context, "iv",
                Base64.encodeToString(IV, Base64.DEFAULT));
    }

    private CryptoUtils() {
        if (mKeyStore == null) {
            try {
                initializeKeyStore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mIsMarshmallowOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
