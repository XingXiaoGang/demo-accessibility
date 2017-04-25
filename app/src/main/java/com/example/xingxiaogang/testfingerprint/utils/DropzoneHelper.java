package com.example.xingxiaogang.testfingerprint.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Locale;


/**
 * Created by Potter on 2016/4/27.
 * Where there is a will there is a way.
 */
public class DropzoneHelper {

    private static final boolean DEBUG = true;
    private static final String TAG = "DropzoneHelper";
    private static final String display;

    public interface DropzonePermissionManagePageType {
        /**
         * TYPE_A : Miui v6 or v7,Meizu sdk_int >= 21
         * TYPE_B : EMUI with advanced function
         * TYPE_C : 应用详情页-->权限管理-->开启浮窗权限
         */
        int TYPE_A = 1;
        int TYPE_B = 2;
        int TYPE_C = 3;
    }

    static {
        String tmp = Build.DISPLAY;
        display = (tmp == null ? "" : tmp.toLowerCase(Locale.US));
    }

    /**
     * 一些比较老的华为手机使用非EMUI,无高级管理功能
     */
    private static boolean isHuaweiWithAdvancedFunc(@NonNull Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                PackageInfo packageInfo = packageManager.getPackageInfo("com.huawei.systemmanager", 0);
                if (packageInfo != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * EMUI 3.1 LITE 版,获取不到EMUI版本,暂时使用此方法判定,此种特殊情况放在最后考虑
     */
    private static final boolean fitEmuiType3dot1LiteForDropzone() {
        String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
        String emuiApiLevel = DropzoneHelper.getSystemProperty("ro.build.hw_emui_api_level");
        if (TextUtils.isEmpty(emuiVersion) && "8".equals(emuiApiLevel)) {
            return true;
        }
        return false;
    }

    /**
     * 开启华为手机"悬浮窗权限管理"窗口
     */
    private static boolean launchHuaweiDropzoneManager(@NonNull Context context) {
        try {
            Intent intent = new Intent();
            String className = PhoneDeviceMatchUtils.fitEmuiTypeAForDropzone() ? "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"
                    : PhoneDeviceMatchUtils.fitEmuiTypeBForDropzone() ? "com.huawei.systemmanager.SystemManagerMainActivity"
                    : fitEmuiType3dot1LiteForDropzone() ? "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity" : "com.huawei.notificationmanager.ui.NotificationManagmentActivity";
            intent.setClassName("com.huawei.systemmanager", className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "Failed to launch HuaWei FloatIconManager activity ", e);
            }
            return false;
        }
    }

    /**
     * 开启华为手机"受保护应用"窗口
     */
    private static boolean launchHuaweiProtectionManager(@NonNull Context context) {
        try {
            String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
            if (emuiVersion != null && emuiVersion.contains("EmotionUI_4.0") || display.startsWith("EMUI4.0") || ("EmotionUI_4.1".equals(emuiVersion) || display.startsWith("EMUI4.1"))) {
                Intent intent = new Intent();
                intent.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "Failed to launch HuaWei ProtectionManagerActivity", e);
            }
            return false;
        }
        return false;
    }

    /**
     * 启动华为手机'自启动'管理页面
     *
     * @param context
     * @return
     */
    private static boolean launchHuaweiAutoStartManager(@NonNull Context context) {
        try {
            Intent intent = new Intent();
            //EMUI 3.1 Lite com.huawei.systemmanager/.optimize.bootstart.BootStartActivity
            //EMUI 4.0/4.1
            intent.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to launch HuaWei AutoStartManager Activity", e);
            }
            return false;
        }
    }

    /**
     * 开启魅族手机"悬浮窗权限管理"窗口
     */
    private static boolean launchMeizuDropzoneManager(@NonNull Context context) {
        String packageName = context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            try {
                Intent intent;
                if (Build.VERSION.SDK_INT >= 21) {
                    intent = new Intent("com.meizu.safe.security.SHOW_APPSEC").putExtra("packageName", packageName);
                } else {
                    intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setData(Uri.fromParts("package", packageName, null));
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to launch Meizu FloatIconManager Activity", e);
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 开启小米手机"悬浮窗权限管理"窗口
     */
    private static boolean launchXiaomiDropzoneManager(@NonNull Context context) {
        String packageName = context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            try {
                Intent intent;
                if (PhoneDeviceMatchUtils.getMiuiVersion() == 6 || PhoneDeviceMatchUtils.getMiuiVersion() == 7) {
                    intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", packageName);
                } else {
                    intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setData(Uri.fromParts("package", packageName, null));
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to launch Xiaomi FloatIconManager Activity", e);
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 启动小米手机'自启动'管理页面
     *
     * @param context
     */
    private static boolean launchXiaomiAutoStartManager(@NonNull Context context) {
        try {
            Intent intent;
            if (PhoneDeviceMatchUtils.getMiuiVersion() == 7) {
                intent = new Intent();
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to launch Xiaomi AutoStartManager Activity", e);
            }
            return false;
        }
        return false;
    }

    /**
     * 开启Oppo手机"悬浮窗权限管理"窗口
     */
    private static boolean launchOppoDropzoneManager(@NonNull Context context) {
        try {
            Intent intent;
            if (PhoneDeviceMatchUtils.isOppoV3()) {
                intent = new Intent("action.coloros.safecenter.FloatWindowListActivity");
                intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity");
            } else {
                intent = new Intent().setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionTopActivity");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "Failed to launch Oppo FloatIconManager Activity", e);
            }
            return false;
        }
    }

    private static boolean launchVivoDropZoneManager(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage("com.iqoo.secure");
            if (intent == null) {
                if (DEBUG) {
                    Log.d(TAG, "startPackage: pm.getLaunchIntentForPackage return null： " + "com.iqoo.secure");
                }
                return false;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "launchVivoDropZoneManager: Failed to launch FloatIconManager Activity", e);
            }
        }
        return false;
    }


    /**
     * 开启手机"悬浮窗权限管理"窗口,非特殊机型时使用此方法
     */
    private static boolean launchGeneralDropzoneManager(@NonNull Context context) {
        String packageName = context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setData(Uri.fromParts("package", packageName, null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to launch general FloatIconManager Activity", e);
                }
                return false;
            }
        }
        return false;
    }

    /**
     * @return value, 0:失败  1:通过6.0方法引导， 2:通过常规方法引导
     **/
    public static int launchSystemDropzoneManager(@NonNull Context context) {
        int res = 0;
        //6.0上优先使用这种方法 add by g (小米手机用这种方法授权以后,重启会自动禁止掉权限)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PhoneDeviceMatchUtils.isMiui()) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:com.apusapps.tools.unreadtips"));
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
                res = 1;
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "launchSystemDropzoneManager: ", e);
                }
            }
        }

        if (res == 0) {
            if (PhoneDeviceMatchUtils.isMiui()) {
                res = DropzoneHelper.launchXiaomiDropzoneManager(context) ? 2 : res;
            } else if (PhoneDeviceMatchUtils.isHuaWei() && DropzoneHelper.isHuaweiWithAdvancedFunc(context)) {
                res = DropzoneHelper.launchHuaweiDropzoneManager(context) ? 2 : res;
            } else if (PhoneDeviceMatchUtils.isMeizu()) {
                res = DropzoneHelper.launchMeizuDropzoneManager(context) ? 2 : res;
            } else if (PhoneDeviceMatchUtils.isOppo()) {
                res = DropzoneHelper.launchOppoDropzoneManager(context) ? 2 : res;
            } else if (PhoneDeviceMatchUtils.isVivo() && PhoneDeviceMatchUtils.getVivoFuntouchOsVersion() >= 25) {
                res = launchVivoDropZoneManager(context) ? 2 : res;
            } else {
                res = DropzoneHelper.launchGeneralDropzoneManager(context) ? 2 : res;
            }
        }
        return res;
    }

    /**
     * 获取浮窗权限设置页面的类型
     */
    public static int getDropzonePermissionManagePageType(@NonNull Context context) {
        /**
         * TYPE_A : Miui v6 or v7,Meizu sdk_int >= 21
         * TYPE_B : EMUI with advanced function
         * TYPE_C : 应用详情页-->权限管理-->开启浮窗权限
         */
        if (PhoneDeviceMatchUtils.isMiui()) {
            if (PhoneDeviceMatchUtils.getMiuiVersion() == 6 || PhoneDeviceMatchUtils.getMiuiVersion() == 7) {
                return DropzonePermissionManagePageType.TYPE_A;
            } else {
                return DropzonePermissionManagePageType.TYPE_C;
            }
        } else if (PhoneDeviceMatchUtils.isHuaWei() && isHuaweiWithAdvancedFunc(context)) {
            return DropzonePermissionManagePageType.TYPE_B;
        } else if (PhoneDeviceMatchUtils.isMeizu()) {
            if (Build.VERSION.SDK_INT >= 21) {
                return DropzonePermissionManagePageType.TYPE_A;
            } else {
                return DropzonePermissionManagePageType.TYPE_C;
            }
        } else if (PhoneDeviceMatchUtils.isOppo()) {
            return DropzonePermissionManagePageType.TYPE_B;
        } else {
            return DropzonePermissionManagePageType.TYPE_C;
        }
    }

    @Nullable
    public static String getSystemProperty(@NonNull String propertyName) {
        String propertyValue = null;
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method getProperty = ReflectUtils.getClassMethod(systemProperties, "get", new Class[]{String.class});
            if (getProperty != null) {
                propertyValue = (String) ReflectUtils.invokeClassMethod(null, getProperty, propertyName);
            }
        } catch (Exception e) {
            return null;
        }
        return propertyValue;
    }

    public static boolean isDropzonePermissionAllowed(@NonNull Context context, boolean service) {
        if (Build.VERSION.SDK_INT <= 18) {
            return true;
        }
        String prop = DropzoneHelper.getSystemProperty("ro.miui.ui.version.name");
        //miui上，直接禁止了Toast级别的浮窗，  api>25上，显示成功但过几秒会自动消失
        if (Build.VERSION.SDK_INT >= 25 || prop != null && prop.toLowerCase().contains("v8")) {
            return dropzonePermissionAllowed(context);
        } else {
            //todo 更准确的判断有没有Toast级别的浮窗权限
            return true;
        }
    }

    @TargetApi(19)
    public static boolean dropzonePermissionAllowed(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 25) {
            return Settings.canDrawOverlays(context);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            return AppOpsManagerUtils.isOpsAllowed(context, 24);
        }
        int flags = context.getApplicationInfo().flags;
        String flagsBinaryString = Integer.toBinaryString(flags);
        int length = flagsBinaryString.length() - 28;
        return length >= 0 && flagsBinaryString.charAt(length) == '1';
    }

    /**
     * 是否可开启'受保护'
     *
     * @param context
     * @return
     */
    public static boolean isAskingForProtectionEnable(@NonNull Context context) {
        do {
            if (PhoneDeviceMatchUtils.isHuaWei() && DropzoneHelper.isHuaweiWithAdvancedFunc(context)) {
                String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
                if ("EmotionUI_4.0".equals(emuiVersion) || display.startsWith("EMUI4.0")) {
                    return true;
                }
                if ("EmotionUI_4.1".equals(emuiVersion) || display.startsWith("EMUI4.1")) {
                    return true;
                }
                break;
            }
        } while (false);

        return false;
    }

    /**
     * 开启'受保护'应用管理页面
     *
     * @param context
     * @return
     */
    public static boolean launchProtectionManager(@NonNull Context context) {
        return DropzoneHelper.launchHuaweiProtectionManager(context);
    }

    /**
     * 是否可开启'自启动'
     *
     * @param context
     * @return
     */
    public static boolean isAskingForAutoStartEnable(@NonNull Context context) {
        do {
            if (PhoneDeviceMatchUtils.isHuaWei() && DropzoneHelper.isHuaweiWithAdvancedFunc(context)) {
                String emuiVersion = DropzoneHelper.getSystemProperty("ro.build.version.emui");
                if ("EmotionUI_4.0".equals(emuiVersion) || display.startsWith("EMUI4.0")) {
                    return true;
                }
                if ("EmotionUI_4.1".equals(emuiVersion) || display.startsWith("EMUI4.1")) {
                    return true;
                }
                break;
            }
            if (PhoneDeviceMatchUtils.isMiui()) {
                if (PhoneDeviceMatchUtils.getMiuiVersion() == 7) {
                    return true;
                }
                break;
            }
        } while (false);

        return false;
    }

    /**
     * 启动'自启动'管理页面
     *
     * @param context
     * @return
     */
    public static boolean launchAutoStartManager(@NonNull Context context) {
        if (PhoneDeviceMatchUtils.isMiui()) {
            return DropzoneHelper.launchXiaomiAutoStartManager(context);
        }
        if (PhoneDeviceMatchUtils.isHuaWei()) {
            return DropzoneHelper.launchHuaweiAutoStartManager(context);
        }
        return false;
    }
}
