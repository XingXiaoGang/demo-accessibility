package com.example.accessibility;

import android.content.Context;

import com.example.accessibility.rom.RomInfoMatcher;
import com.example.accessibility.rom.bean.RomInfo;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
public class AccessibilityHelper {

    private static AccessibilityHelper mInstance;
    private Context mContext;
    private RomInfo mRomInfo;

    public static AccessibilityHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AccessibilityHelper(context);
        }
        return mInstance;
    }

    private AccessibilityHelper(Context context) {
        this.mContext = context;
    }

    //判断是否支持辅助功能 注：方法要异步调用
    public boolean supportAccessibility() {
        mRomInfo = RomInfoMatcher.getInstance().match(mContext);
        //todo 逻辑判断
        return true;
    }
}

