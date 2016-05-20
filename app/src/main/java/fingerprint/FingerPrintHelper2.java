package fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by xingxiaogang on 2016/5/18.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHelper2 {

    private CancellationSignal mCancellationSignal;
    private Context mContext;

    public enum UseState {
        API_UN_SUPPORT,/*api<23*/
        PERMISSION_DENIED,/*没有权限*/
        NO_HARDWARE,/*没有采集器*/
        NO_FINGER_PRINTS,/*没有录入指纹*/
        NO_SERVICE,/*FingerprintManager null*/
        SUPPORT,/*可用*/
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

    public FingerPrintHelper2(Context context) {
        this.mContext = context;
    }

    //开始监听指纹
    public boolean listen(FingerprintManager.AuthenticationCallback callBack) {
        final Context context = mContext;
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        //动态检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, mCancellationSignal, 0, callBack, null);
        return true;
    }

    public void stop() {
        if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
            mCancellationSignal.cancel();
        }
    }
}
