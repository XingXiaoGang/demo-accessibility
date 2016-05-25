package com.example.xingxiaogang.testfingerprint;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.accessibility.AccessibilityClient;

import fingerprint.FingerPrintHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AccessibilityClient.AccessibilityTaskHandlerCallBack {

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private ConformDialog mConformDialog;
    private TextView mInfoTextView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.state_text_view);
        mConformDialog = new ConformDialog();
        mButton = (Button) findViewById(R.id.open_dialog);
        mButton.setOnClickListener(this);
        findViewById(R.id.start_accessibility).setOnClickListener(this);
        findViewById(R.id.open_accessibility_permission).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            FingerPrintHelper.UseState state = FingerPrintHelper.getState(getApplicationContext());
            switch (state) {
                case PERMISSION_DENIED: {
                    mInfoTextView.setText("不可用：没有权限");
                    break;
                }
                case NO_HARDWARE: {
                    mInfoTextView.setText("不可用：没有采集器");
                    break;
                }
                case NO_FINGER_PRINTS: {
                    mInfoTextView.setText("不可用：没有已经录入的指纹,请先录入指纹");
                    break;
                }
                case NO_SERVICE: {
                    mInfoTextView.setText("不可用：FingerprintManager is null");
                    break;
                }
                case SUPPORT: {
                    mInfoTextView.setText("状态：可用");
                    mButton.setEnabled(true);
                    break;
                }
            }
        } else {
            mInfoTextView.setText("不可用：api<23");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_dialog: {
                mConformDialog.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                break;
            }
            case R.id.open_accessibility_permission: {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
            }
            case R.id.start_accessibility: {
                AccessibilityClient client = AccessibilityClient.getInstance(getApplication());
                if (client.isSupportAccessibility()) {
                    client.startSettingAccessibility();
                    client.setCallBack(this);
                }
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (mConformDialog != null && mConformDialog.isVisible()) {
                    mConformDialog.dismiss();
                }
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onErro(int code, String msg) {
        Log.d("test_access", "====onErro====" + code + ",,," + msg);
    }

    @Override
    public void onProgressUpdate(int all, int progress, String description) {
        Log.d("test_access", "====onProgressUpdate====" + all + ",,," + progress + ":" + description);

    }

    @Override
    public void onFinish(boolean success) {
        Log.d("test_access", "====onFinish====" + success);
    }
}
