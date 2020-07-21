package org.meowcat.xposed.mipush;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    public static final String TAG = "MiPushEnhancement";

    // Fake Xiaomi brand
    // This will be used in both Build.MANUFACTURER and system prop ro.product.manufacturuer.
    // This will be used in both Build.BRAND and system prop ro.product.brand.
    // This will be used in both Build.PRODUCT and system prop ro.product.name.
    public static final String BRAND = "Xiaomi";

    public static final String MODE_WHITE = "whitelist";
    public static final String MODE_BLACK = "blacklist";

    public static final String APPLICATION_ATTACH = "attach";

    // Preset system properties to replace.
    public static final Map<String, String> PROPS = new HashMap<>(6);

    // Built-in package name Blacklists. The module will never
    // hook these packages due to known compatibility issues.
    // regExp required
    public static final String[] BUILT_IN_BLACKLIST = new String[]{ // TODO: more built-in blacklist packages
            "android", // Android system
            "com\\.android.*", // Android system components
            "com\\.google\\.android.*", // Google apps
            "de\\.robv\\.android\\.xposed\\.installer", // Xposed Installer
            "org\\.meowcat\\.edxposed\\.manager", // EdXposed Manager
            "com\\.xiaomi\\.xmsf", // MiPush Framework
            "com\\.tencent\\.mm", // WeChat
            "top\\.trumeet\\.mipush" // MiPush Manager
    }; // UserHandle.isCore(uid) will auto pass

    static {
        // Fake MIUI 12 props
        PROPS.put("ro.miui.ui.version.name", "V12");
        PROPS.put("ro.miui.ui.version.code", "10");
        PROPS.put("ro.miui.version.code_time", "1592409600");
        // Fake Xiaomi brand
        PROPS.put("ro.product.manufacturer", BRAND);
        PROPS.put("ro.product.vendor.manufacturer", BRAND);
        PROPS.put("ro.product.brand", BRAND);
        PROPS.put("ro.product.vendor.brand", BRAND);
    }
}
