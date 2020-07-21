package org.meowcat.xposed.mipush;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.UserHandle;

import java.io.IOException;
import java.util.Collections;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.trumeet.mipush.settings.ini.IniConf;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static org.meowcat.xposed.mipush.Constants.APPLICATION_ATTACH;
import static org.meowcat.xposed.mipush.Constants.BRAND;
import static org.meowcat.xposed.mipush.Constants.MODE_BLACK;
import static org.meowcat.xposed.mipush.Constants.MODE_WHITE;
import static org.meowcat.xposed.mipush.Constants.PROPS;
import static top.trumeet.mipush.settings.ini.IniConstants.MODULE_BLACKLIST;
import static top.trumeet.mipush.settings.ini.IniConstants.MODULE_WHITELIST;
import static top.trumeet.mipush.settings.ini.IniConstants.MODULE_WORKING_MODE;

/**
 * MiPush Fake Enhancement Hook for Xposed, EdXposed and
 * other Xposed implementation without malicious behavior
 *
 * @author MlgmXyysd
 * @date 2020/07/21
 */
@SuppressWarnings("unused")
public class Enhancement implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        String packageName = lpparam.packageName;

        if (Utils.inBuiltInBlackList(packageName)) {
            // is in built-in blacklisted package
            return;
        }

        // Hook Application.attach to avoid multidex ClassNotFoundException and get Context
        findAndHookMethod(Application.class, APPLICATION_ATTACH, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                final Context context = (Context) param.args[0];
                final ApplicationInfo applicationInfo = context.getApplicationInfo();
                if (applicationInfo == null) {
                    // null application info, exit
                    return;
                }
                final int userId = UserHandle.getUserHandleForUid(applicationInfo.uid).hashCode();
                final boolean availability = Utils.getParamAvailability(param, Binder.getCallingPid());

                if (packageName.equals(BuildConfig.APPLICATION_ID)) {
                    // hook myself
                    XposedHelpers.findAndHookMethod(Utils.class.getName(), lpparam.classLoader, "isEnhancementEnabled", XC_MethodReplacement.returnConstant(true));
                }

                if ((boolean) callStaticMethod(UserHandle.class, "isCore", Binder.getCallingUid()) || !availability) {
                    // is Android code package
                    return;
                }

                try {
                    final IniConf conf = new IniConf(userId);

                    switch (conf.get(MODULE_WORKING_MODE, "blacklist")) {
                        case MODE_BLACK:
                            if (conf.getAll(MODULE_BLACKLIST, Collections.emptyList()).contains(packageName)) {
                                return;
                            }
                            break;
                        case MODE_WHITE:
                            if (!conf.getAll(MODULE_WHITELIST, Collections.emptyList()).contains(packageName)) {
                                return;
                            }
                            break;
                        default:
//                            Log.e(TAG, "Unknown working mode.");
                            return;
                    }

                    // android.os.SystemProperties.native_get(String,String)
                    findAndHookMethod(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader), "native_get", String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String key = param.args[0].toString();

                            if (PROPS.containsKey(key)) {
                                param.setResult(PROPS.get(key));
                            }
                        }
                    });

                    // android.os.SystemProperties.native_get_int(String,int)
                    findAndHookMethod(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader), "native_get_int", String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String key = param.args[0].toString();

                            if (PROPS.containsKey(key)) {
                                param.setResult(PROPS.get(key));
                            }
                        }
                    });

                    // android.os.SystemProperties.native_get_long(String,long)
                    findAndHookMethod(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader), "native_get_long", String.class, long.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            final String key = param.args[0].toString();

                            if (PROPS.containsKey(key)) {
                                param.setResult(PROPS.get(key));
                            }
                        }
                    });

                    XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", BRAND);
                    XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", BRAND);
                } catch (IOException e) {
                    XposedBridge.log(e);
                }
            }
        });
    }
}
