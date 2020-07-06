package mipush.xmipushenhance;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static mipush.xmipushenhance.Constants.BRAND;

/**
 * XiaoMi fake build hook for xposed
 * @author zts1993
 * @date 2018/3/20
 */
@SuppressWarnings("unused")
public class MiPushEnhanceHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiPushEnhanceHook";

    private boolean inBlackList(String pkgName) {
        for (final String b : Constants.BLACKLIST) {
            if (pkgName.contains(b)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            String packageName = lpparam.packageName;
            if (inBlackList(packageName)) {
                Log.d(TAG, "hit blacklist when fake build for " + packageName);
                return;
            }

            // TODO: Do we need to hook getInt(String, int), get(String, String), etc as well?

            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader),
                    "get", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (Constants.PROPS.containsKey(param.args[0].toString())) {
                                param.setResult(Constants.PROPS.get(param.args[0].toString()));
                            }
                        }
                    });

            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "PRODUCT", BRAND);
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + ": hook: " + throwable);
        }
    }
}
