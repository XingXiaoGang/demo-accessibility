package com.example.accessibility;

import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by xingxiaogang on 2016/5/20.
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {

    }
}