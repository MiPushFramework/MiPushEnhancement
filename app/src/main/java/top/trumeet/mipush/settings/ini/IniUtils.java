package top.trumeet.mipush.settings.ini;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.FileUtils;

import androidx.annotation.NonNull;

import java.io.File;

public class IniUtils {
    /**
     * Convert the key of SharedPreference to Ini key.
     * The SP key must be section_whatever_option.
     */
    @NonNull
    public static IniKey convertKeyToIni(@NonNull final String spKey) {
        final String[] keySet = spKey.split("_");
        return new IniKey(keySet[0], spKey.substring(keySet[0].length() + 1));
    }

    /**
     * Convert the key of ini option to SharePreference.
     * The SP key must be section_whatever_option.
     */
    @NonNull
    public static String convertKeyToSP(@NonNull final IniKey key) {
        return String.format("%1$s_%2$s", key.section, key.option);
    }

    /**
     * Change file permission with given permission mode
     *
     * @param file file to change permissions
     * @param mode file permission
     */
    @SuppressWarnings("deprecation")
    @SuppressLint({"WorldReadableFiles", "WorldWriteableFiles"})
    public static void setFilePermissionsFromMode(File file, int mode) {
        int perms = FileUtils.S_IRUSR | FileUtils.S_IWUSR
                | FileUtils.S_IRGRP | FileUtils.S_IWGRP;
        if ((mode & Context.MODE_WORLD_READABLE) != 0) {
            perms |= FileUtils.S_IROTH;
        }
        if ((mode & Context.MODE_WORLD_WRITEABLE) != 0) {
            perms |= FileUtils.S_IWOTH;
        }
        FileUtils.setPermissions(file.getAbsolutePath(), perms, -1, -1);
    }

    /**
     * Get the configuration path
     * The configuration may be deleted, but it can be simply recreated. In such cases,
     * it will return a File with exists() = false.
     *
     * @param context Application context
     * @return Path of the configuration
     */
    public static File getConfigPath(Context context) {
        return new File(context.getApplicationInfo().deviceProtectedDataDir + "/etc/module.conf");
    }
}
