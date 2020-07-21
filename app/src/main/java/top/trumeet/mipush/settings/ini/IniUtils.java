package top.trumeet.mipush.settings.ini;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.FileUtils;

import androidx.annotation.NonNull;

import org.meowcat.xposed.mipush.BuildConfig;

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
     * @param name file to change permissions
     * @param mode file permission
     */
    @SuppressWarnings("deprecation")
    @SuppressLint({"WorldReadableFiles", "WorldWriteableFiles"})
    public static void setFilePermissionsFromMode(String name, int mode) {
        int perms = FileUtils.S_IRUSR | FileUtils.S_IWUSR
                | FileUtils.S_IRGRP | FileUtils.S_IWGRP;
        if ((mode & Context.MODE_WORLD_READABLE) != 0) {
            perms |= FileUtils.S_IROTH;
        }
        if ((mode & Context.MODE_WORLD_WRITEABLE) != 0) {
            perms |= FileUtils.S_IWOTH;
        }
        FileUtils.setPermissions(name, perms, -1, -1);
    }

    /**
     * Get the configuration path
     * The configuration may be deleted, but it can be simply recreated. In such cases,
     * it will return a File with exists() = false.
     *
     * @param userId userId
     * @return Path of the configuration
     */
    public static File getConfigPath(int userId) {
        if (userId < 0) {
            // system user, try to load main user's conf
            userId = 0;
        }
        return new File(String.format("/data/user_de/%s/%s/", userId, BuildConfig.APPLICATION_ID));
    }
}
