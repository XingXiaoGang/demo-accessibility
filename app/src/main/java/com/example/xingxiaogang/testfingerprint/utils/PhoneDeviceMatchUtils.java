package com.example.xingxiaogang.testfingerprint.utils;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;


public class PhoneDeviceMatchUtils {

    private static final boolean DEBUG = true;

    private static final boolean bPantech;
    private static final boolean bMotorola;
    private static final boolean bAsus;
    private static final boolean bHuaWei;
    private static final boolean bSumSang;
    private static final boolean bMiui;
    private static final int mMiuiVersion;
    private static final boolean bMeizu;
    private static final boolean bOppo;
    private static final boolean bOppoV3;
    protected static final boolean bVivo;

    /**
     * 华为mate8 的四种型号： HUAWEI NXT-AL10 HUAWEI NXT-CL00 HUAWEI NXT-DL00 HUAWEI NXT-TL00
     * http://consumer.huawei.com/cn/mobile-phones/mate8/conf.html
     */
    private static final boolean bHuaWeiMate8;
    private static boolean bDangerous;
    private static String display;

    private static final String TAG = "PhoneDeviceMatchUtils";

    static {
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase(Locale.US);
        bPantech = manufacturer.contains("pantech");
        bMotorola = manufacturer.contains("motorola");
        bHuaWei = manufacturer.contains("huawei");
        bAsus = manufacturer.contains("asus");
        bSumSang = manufacturer.contains("samsung");
        bHuaWeiMate8 = Build.MODEL.toUpperCase().contains("HUAWEI NXT-");
        bMeizu = "meizu".equalsIgnoreCase(manufacturer);
        bOppo = "oppo".equalsIgnoreCase(manufacturer);
        bVivo = "vivo".equalsIgnoreCase(manufacturer);

        if (Build.MODEL != null) {
            String model = Build.MODEL.toLowerCase(Locale.US);
            if (bHuaWei) {
                if (model.contains("g7-") || model.contains("h60-") || model.contains("t1-701") || model.contains("hi6210sft") || model.contains("y635")) {
                    bDangerous = true;
                }
            } else if (bAsus) {
                if (model.contains("t00")) {
                    bDangerous = true;
                }
            }
        }

        String colorOsVersion = DropzoneHelper.getSystemProperty("ro.build.version.opporom");
        bOppoV3 = colorOsVersion != null && colorOsVersion.equalsIgnoreCase("V3.0.0");

        String miuiProp = DropzoneHelper.getSystemProperty("ro.miui.ui.version.name");
        if (!TextUtils.isEmpty(miuiProp)) {
            bMiui = true;
            if (miuiProp.toLowerCase(Locale.US).contains("v8")) {
                mMiuiVersion = 8;
            } else if (miuiProp.equalsIgnoreCase("V5")) {
                mMiuiVersion = 5;
            } else if (miuiProp.equalsIgnoreCase("V6")) {
                mMiuiVersion = 6;
            } else if (miuiProp.equalsIgnoreCase("V7")) {
                mMiuiVersion = 7;
            } else {
                mMiuiVersion = 0;//未知版本
            }
        } else {
            bMiui = false;
            mMiuiVersion = -1;
        }

        display = (Build.DISPLAY == null ? "" : Build.DISPLAY.toLowerCase(Locale.US));

        if (DEBUG) {
            Log.i(TAG, "static initializer() called with " + manufacturer + ", model = " + Build.MODEL);
        }

    }

    public static boolean isDangerousDevice() {
        return bDangerous;
    }

    /**
     * 欧盛
     */
    public static final boolean isPantech() {
        return bPantech;
    }

    /**
     * MOTO
     */
    public static final boolean isMotorola() {
        return bMotorola;
    }

    public static boolean isHuaWei() {
        return bHuaWei;
    }

    public static boolean isHuaWeiMate8() {
        return bHuaWeiMate8;
    }

    public static boolean isSumsang() {
        return bSumSang;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isOppoV3() {
        return bOppoV3;
    }

    public static boolean isMeizu() {
        return bMeizu;
    }

    public static boolean isOppo() {
        return bOppo;
    }

    public static boolean isMiui() {
        return bMiui;
    }

    public static boolean isVivo() {
        return bVivo;
    }

    public static int getVivoFuntouchOsVersion() {
        int version = 0;
        String versionStr = DropzoneHelper.getSystemProperty("ro.vivo.os.version");
        if (DEBUG) {
            Log.d(TAG, "getVivoFuntouchOsVersion: " + versionStr);
        }
        if (versionStr != null) {
            versionStr = versionStr.replace(".", "");
            if (versionStr.length() > 2) {
                versionStr = versionStr.substring(0, 2);
            }
            try {
                version = Integer.parseInt(versionStr);
            } catch (Exception ignore) {
            }
        }
        return version;
    }

    public static int getMiuiVersion() {
        return mMiuiVersion;
    }

    public static final boolean fitEmuiTypeAForDropzone() {
        String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
        if ("EmotionUI_3.1".equals(emuiVersion) || display.startsWith("EMUI3.1")) {
            return true;
        }
        if ("EmotionUI_4.0".equals(emuiVersion) || display.startsWith("EMUI4.0")) {
            return true;
        }
        if ("EmotionUI_4.1".equals(emuiVersion) || display.startsWith("EMUI4.1")) {
            return true;
        }
        return false;
    }

    public static final boolean fitEmuiTypeBForDropzone() {
        String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
        if ("EmotionUI_2.3".equals(emuiVersion) || display.startsWith("EMUI2.3")) {
            return true;
        }
        return false;
    }
}
