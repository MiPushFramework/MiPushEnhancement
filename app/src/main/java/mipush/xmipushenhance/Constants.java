package mipush.xmipushenhance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.oasisfeng.hack.Hack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Constants {
    private static final String TAG = "XMPushXposed_Data";

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
            "com\\.android.*",
            "com\\.google\\.android.*",
            "de\\.robv\\.android\\.xposed\\.installer",
            "com\\.xiaomi\\.xmsf",
            "com\\.tencent\\.mm",
            "top\\.trumeet\\.mipush"
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

    /**
     * Get the configuration path for the given user.
     * The app may not be installed for the give user, so the reading or writing would be impossible. In such cases,
     * it will return null.
     * The configuration may be deleted, but it can be simply recreated. In such cases,
     * it will return a File with exists() = false.
     * @param userId Android User ID
     * @return Path of the configuration
     */
    @Nullable
    public static File getConfigPath(int userId, Context context) {
        try {
            // Construct the target Context of the module.
            // This will throw NameNotFoundException if the package is not found, or the calling
            // user does not have permissions to view it.
            final Context targetContext;
            // We only need to do so if we are not in the target package and the same user
            if (!context.getPackageName().equals(BuildConfig.APPLICATION_ID) ||
            userId != Process.myUserHandle().hashCode()) {
                targetContext = Hack.into(Context.class)
                        .method("createPackageContextAsUser")
                        .returning(Context.class)
                        .fallbackReturning(null)
                        .throwing(PackageManager.NameNotFoundException.class)
                        .withParams(String.class, int.class, UserHandle.class)
                        .invoke(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY, HiddenApiHelper.createUserHandle(userId))
                        .on(context);
            } else {
                targetContext = context;
            }
            final File dataDir = ContextCompat.getDataDir(targetContext);
            final File etcDir = new File(dataDir, "etc");
            return new File(etcDir, "module.conf");
        } catch (Exception e) {
            if (e instanceof PackageManager.NameNotFoundException) {
                // This typically happens when:
                // 1. The calling user does not have permissions to enumerate the packages in the target user, or
                // 2. The module is not installed in the target user.
                // In both cases, we can safely fallback to the default configuration,
                // since the Xposed hook runs inside the target application's uid.
                Log.e(TAG, String.format("The module is not found for user %1$s. Falling back to the default configuration.", userId));
                return null;
            }
            // Unexpected exception happens.
            Log.e(TAG, "Error while reading the configuration file.", e);
            throw new RuntimeException(e);
        }
    }
}
