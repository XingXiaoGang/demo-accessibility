package com.example.xingxiaogang.testfingerprint;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import fingerprint.FingerPrintHelper;

/**
 * Created by xingxiaogang on 2016/5/19.
 */
public class ConformDialog extends DialogFragment implements FingerPrintHelper.FingerPrintCallBack {

    private TextView mTextView;
    private FingerPrintHelper mHelper;
    FingerPrintHelper.UseState useState;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fingerprint_dialog_layout, container);
        mTextView = (TextView) view.findViewById(R.id.tips_textview);
        mHelper = FingerPrintHelper.getInstance(getContext());
        useState = FingerPrintHelper.getState(getContext());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        if (mHelper != null) {
            mHelper.startListen(getContext(), ConformDialog.this);
        }
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
        mHelper.stopListen();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCancel(boolean fromUser) {
        Toast.makeText(getContext(), "onCancel", Toast.LENGTH_LONG).show();
        mTextView.setText("已取消");
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onFailed() {
        Toast.makeText(getContext(), "onFailed", Toast.LENGTH_LONG).show();
        mTextView.setText("验证失败");
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onError(int errorCode, CharSequence errString) {
        Toast.makeText(getContext(), "onError", Toast.LENGTH_LONG).show();
        mTextView.setText("error:" + errorCode + "," + errString);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onSuccess(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(getContext(), "onSuccess", Toast.LENGTH_LONG).show();
        mTextView.setText("验证通过");
    }
}
