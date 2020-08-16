package org.meowcat.xposed.mipush;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.system.Os;
import android.system.OsConstants;
import android.util.Base64;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Utils {

    /**
     * Check package name in built-in blacklist
     *
     * @param pkgName package name to check
     * @return is in built-in blacklist
     */
    static boolean inBuiltInBlackList(String pkgName) {
        for (final String b : Constants.BUILT_IN_BLACKLIST) {
            if (pkgName.matches(b)) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Check framework implementation's compatibility and security
//     * To avoid compatibility or magic issues, must
//     * call this method after got any MethodHookParam
//     *
//     * @param methodHookParam Xposed hook param
//     * @param callingPid      Process Pid
//     * @return true
//     */
//    public static boolean getParamAvailability(final XC_MethodHook.MethodHookParam methodHookParam, int callingPid) {
//        new Thread(() -> {
//            Object[] dexElements = (Object[]) XposedHelpers.getObjectField(XposedHelpers.getObjectField(XposedBridge.class.getClassLoader(), "pathList"), "dexElements");
//            for (Object entry : dexElements) {
//                Enumeration<String> entries = ((DexFile) XposedHelpers.getObjectField(entry, "dexFile")).entries();
//                while (entries.hasMoreElements()) {
//                    if (entries.nextElement().matches(".+?(epic|weishu).+")) {
//                        String message = new String(Base64.decode("RG8gTk9UIHVzZSBUYWlDaGkgYW55d2F5XG7or7fkuI3opoHkvb/nlKjlpKrmnoHmiJbml6DmnoE=".getBytes(StandardCharsets.UTF_8), Base64.DEFAULT));
//                        try {
//                            if (methodHookParam.args[0] instanceof Application) {
//                                Toast.makeText((Context) methodHookParam.args[0], message, Toast.LENGTH_LONG).show();
//                            }
//                        } catch (Exception ignored) {
//                        }
////                      Os.kill(callingPid, OsConstants.SIGKILL);
//                        XposedBridge.log(message);
//                    }
//                }
//            }
//        }).start();
//        return true;
//    }

    /**
     * Check malware
     *
     * @param context Module context
     * @return If installed or use malware to activate the module
     */
    public static boolean isExpModuleActive(Context context) {
        boolean isExp = false;
        if (context == null) {
            return isExp;
        }
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (PackageInfo info: packageInfoList) {
            if (info.packageName.equals("me.weishu.exp")) {
                return true;
            }
        }
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = null;
            try {
                result = contentResolver.call(uri, "active", null, null);
            } catch (RuntimeException e) {
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable e1) {
                    return isExp;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }

            if (result == null) {
                return isExp;
            }
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    /**
     * Check Xposed module status
     *
     * @return true if hooked
     */
    public static boolean isEnhancementEnabled() {
        return false;
    }

    /**
     * Hide or show app icon
     *
     * @param packageManager packageNamager
     * @param componentName  compoentName
     * @param hide           hide or show icon
     */
    public static void hideIcon(PackageManager packageManager, ComponentName componentName, boolean hide) {
        packageManager.getComponentEnabledSetting(componentName);
        if (hide) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }
    }
}
