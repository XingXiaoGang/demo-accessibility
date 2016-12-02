package com.example.xingxiaogang.testfingerprint;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import fingerprint.FingerPrintHelper2;

/**
 * Created by xingxiaogang on 2016/5/19.
 */
public class ConformDialog extends DialogFragment {

    private TextView mTextView;
    private FingerPrintHelper2 mHelper;
    FingerPrintHelper2.UseState useState;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case R.id.close_dialog: {
                    dismiss();
                    break;
                }
            }
            return false;
        }
    });

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fingerprint_dialog_layout, container);
        mTextView = (TextView) view.findViewById(R.id.tips_textview);
        mHelper = new FingerPrintHelper2(getContext());
        useState = FingerPrintHelper2.getState(getContext());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);

        if (mHelper != null) {
            mHelper.listen(new MAuthenticationCallback());
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();

        switch (useState) {
            case API_UN_SUPPORT: {
                mTextView.setText("api<23");
                break;
            }
            case PERMISSION_DENIED: {
                mTextView.setText("没有权限");
                break;
            }
            case NO_HARDWARE: {
                mTextView.setText("没有采集器");
                break;
            }
            case NO_FINGER_PRINTS: {
                mTextView.setText("没有已经录入的指纹,请先录入指纹");
                break;
            }
            case NO_SERVICE: {
                mTextView.setText("FingerprintManager null");
                break;
            }
            case SUPPORT: {
                mTextView.setText("可用");
                break;
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHelper.stop();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private class MAuthenticationCallback extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(getContext(), "错误：验证次数过多 ,请稍后再试", Toast.LENGTH_LONG).show();
            mTextView.setText("onAuthenticationError:" + errorCode + "," + String.valueOf(errString));
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            //指纹验证失败，指纹识别失败，可再验，该指纹不是系统录入的指纹。
            Toast.makeText(getContext(), "onFailed", Toast.LENGTH_LONG).show();
            mTextView.setText("验证失败");
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            //指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
            Toast.makeText(getContext(), "onAuthenticationHelp:", Toast.LENGTH_LONG).show();
            mTextView.setText("onAuthenticationHelp:" + helpCode + "," + String.valueOf(helpString));
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Toast.makeText(getContext(), "验证通过", Toast.LENGTH_LONG).show();
            mTextView.setText("验证通过");
            mHandler.sendEmptyMessageDelayed(R.id.close_dialog, 1000);
        }
    }
}
