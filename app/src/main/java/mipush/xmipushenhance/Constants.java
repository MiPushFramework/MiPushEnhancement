package mipush.xmipushenhance;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    // Fake brand.
    // This will be used in both Build.MANUFACTURER and system prop ro.product.manufacturuer.
    // This will be used in both Build.BRAND and system prop ro.product.brand.
    // This will be used in both Build.PRODUCT and system prop ro.product.name.
    public static final String BRAND = "Xiaomi";

    // This will be used in ro.miui.internal.storage.
    @SuppressLint("SdCardPath")
    public static final String MIUI_INTERNAL_STORAGE = "/sdcard/";

    // This will be used in ro.miui.ui.version.name.
    public static final String MIUI_VERSION_NAME = "V9";

    // This will be used in ro.miui.ui.version.code.
    public static final String MIUI_VERSION_CODE = "7";

    // This will be used in ro.miui.version.code_time.
    public static final String MIUI_VERSION_CODE_TIME = "1527550858";

    // Preset system properties to replace.
    public static final Map<String, String> PROPS = new HashMap<>(7);

    // Application ID Blacklists. The module will never hook these packages
    // due to known compatibility issues.
    public static final String[] BLACKLIST = new String[] {
            "android",
            "com.google.android",
            "de.robv.android.xposed.installer",
            "com.xiaomi.xmsf",
            "com.tencent.mm",
            "top.trumeet.mipush"
    };

    static {
        PROPS.put("ro.miui.ui.version.name", MIUI_VERSION_NAME);
        PROPS.put("ro.miui.ui.version.code", MIUI_VERSION_CODE);
        PROPS.put("ro.miui.version.code_time", MIUI_VERSION_CODE_TIME);
        PROPS.put("ro.miui.internal.storage", MIUI_INTERNAL_STORAGE);
        PROPS.put("ro.product.manufacturer", BRAND);
        PROPS.put("ro.product.brand", BRAND);
        PROPS.put("ro.product.name", BRAND);
    }
}
