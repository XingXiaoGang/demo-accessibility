package fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by xingxiaogang on 2016/5/18.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHelper extends FingerprintManager.AuthenticationCallback {

    private static FingerPrintHelper mInstance;
    private FingerprintManager mFingerprintManager;

    private KeyStore mKeyStore;
    private Cipher mCipher;
    private KeyGenerator mKeyGenerator;
    private CancellationSignal mCancellationSignal;
    private static final String KEY_NAME = "my_key";

    private FingerPrintCallBack mCallBack;

    public enum UseState {
        API_UN_SUPPORT,/*api<23*/
        PERMISSION_DENIED,/*没有权限*/
        NO_HARDWARE,/*没有采集器*/
        NO_FINGER_PRINTS,/*没有录入指纹*/
        NO_SERVICE,/*FingerprintManager null*/
        SUPPORT,/*可用*/
    }

    public synchronized static FingerPrintHelper getInstance(Context context) {
        if (getState(context).equals(UseState.SUPPORT) && mInstance == null) {
            mInstance = new FingerPrintHelper(context);
        }
        return mInstance;
    }

    //判断基础
    public static UseState getState(Context context) {
        //api 限制
        final FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

        if (Build.VERSION.SDK_INT < 23) {
            Log.e("test_finger_print", " error: api<23");
            return UseState.API_UN_SUPPORT;
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("test_finger_print", " error: 没有权限");
            return UseState.PERMISSION_DENIED;
        } else if (fingerprintManager == null) {
            Log.e("test_finger_print", " error: getSystemService(Context.FINGERPRINT_SERVICE) returns null");
            return UseState.NO_SERVICE;
        } else if (!fingerprintManager.isHardwareDetected()) {
            Log.e("test_finger_print", " error: 没有指纹采集硬件");
            return UseState.NO_HARDWARE;
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            Log.e("test_finger_print", " error: 没有已经录入的指纹,请先录入指纹");
            return UseState.NO_FINGER_PRINTS;
        } else {
            return UseState.SUPPORT;
        }
    }

    private FingerPrintHelper(Context context) {
        mFingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        mCancellationSignal = new CancellationSignal();
    }

    public void startListen(Context context, FingerPrintCallBack callBack) {
        //动态检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("test_finger_print", " error: 没有权限");
            return;
        }

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        try {
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }

        createKey();
        initCipher();

        this.mCallBack = callBack;
        mFingerprintManager.authenticate(new FingerprintManager.CryptoObject(mCipher), mCancellationSignal, 0, this, null);
    }

    public void stopListen() {
        mCancellationSignal.cancel();
    }

    public void createKey() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initCipher() {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (mCallBack != null) {
            mCallBack.onError(errorCode, errString);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (mCallBack != null) {
            mCallBack.onFailed();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (mCallBack != null) {
            mCallBack.onSuccess(result);
        }
    }

    public interface FingerPrintCallBack {

        void onCancel(boolean fromUser);

        void onFailed();

        void onError(int errorCode, CharSequence errString);

        void onSuccess(FingerprintManager.AuthenticationResult result);
    }

}
