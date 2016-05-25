package com.example.accessibility;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.accessibility.rom.RomInfoMatcher;
import com.example.accessibility.rom.bean.RomInfo;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 判断是否支持辅助
 * 供开发者调用
 */
public class AccessibilityClient {

    private static AccessibilityClient mInstance;
    private Application mApplication;
    private RomInfo mRomInfo;
    private AccessibilityTaskHandlerCallBack mListener;

    public static AccessibilityClient getInstance(Application context) {
        if (mInstance == null) {
            mInstance = new AccessibilityClient(context);
        }
        return mInstance;
    }

    private AccessibilityClient(Application application) {
        this.mApplication = application;

        IntentFilter intentFilter = new IntentFilter(Statics.ACCESSIBILITY_CLIENT_ACTION);
        application.registerReceiver(new AccessibilityBroadCastReceiver(), intentFilter);
    }

    public RomInfo getRomInfo() {
        return mRomInfo;
    }

    //判断是否支持辅助功能 注：方法要异步调用
    public boolean isSupportAccessibility() {
        boolean support = Build.VERSION.SDK_INT >= 16;
        if (mRomInfo != null) {
            support = true;
        } else {
            if (support) {
                mRomInfo = RomInfoMatcher.getInstance().match(mApplication);
                Log.e("test_access", "机型匹配结果:" + (mRomInfo != null ? "支持," + mRomInfo : "不支持"));
                support = mRomInfo != null;
            } else {
                Log.e("test_access", "机型匹配结果:不支持, sdk_int <16");
            }
        }
        return support;
    }

    public void setCallBack(AccessibilityTaskHandlerCallBack callBack) {
        this.mListener = callBack;
    }

    /**
     * 开始执行
     **/
    public void startSettingAccessibility() {
        senBroadCast(R.id.action_start);
    }

    public void finishSettingAccessbility() {
        senBroadCast(R.id.action_finish);
    }

    public void senBroadCast(int action) {
        Intent intent = new Intent(Statics.ACCESSIBILITY_SERVER_ACTION);
        intent.putExtra(Statics.Key.ACTION, action);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mApplication);
        localBroadcastManager.sendBroadcast(intent);
    }

    public interface AccessibilityTaskHandlerCallBack {
        void onErro(int code, String msg);

        void onProgressUpdate(int all, int progress, String description);

        void onFinish(boolean success);
    }

    class AccessibilityBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int code = intent.getIntExtra(Statics.Key.ACTION, -1);
                if (code == R.id.action_error) {
                    if (mListener != null) {
                        int errorCode = intent.getIntExtra(Statics.Key.CODE, -1);
                        String msg = intent.getStringExtra(Statics.Key.MESSAGE);
                        mListener.onErro(errorCode, msg);
                    }
                } else if (code == R.id.action_progress_update) {
                    if (mListener != null) {
                        int all = intent.getIntExtra(Statics.Key.PROGRESS_ALL, -1);
                        int current = intent.getIntExtra(Statics.Key.PROGRESS, -1);
                        String description = intent.getStringExtra(Statics.Key.MESSAGE);
                        mListener.onProgressUpdate(all, current, description);
                    }
                } else if (code == R.id.action_finish) {
                    if (mListener != null) {
                        boolean success = intent.getBooleanExtra(Statics.Key.SUCCESS, false);
                        mListener.onFinish(success);
                    }
                }
            }
        }
    }
}

