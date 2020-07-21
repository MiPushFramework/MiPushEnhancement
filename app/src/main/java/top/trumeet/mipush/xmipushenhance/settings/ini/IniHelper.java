package top.trumeet.mipush.xmipushenhance.settings.ini;

import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public class IniHelper {
    /**
     * Convert the key of SharedPreference to Ini key.
     *
     * The SP key must be section_whatever_option.
     */
    @NonNull
    public static IniKey convertKeyToIni(@NonNull final String spKey) {
        final String[] keySet = spKey.split("_");
        return new IniKey(keySet[0], spKey.substring(keySet[0].length() + 1));
    }

    /**
     * Convert the key of ini option to SharePreference.
     *
     * The SP key must be section_whatever_option.
     */
    @NonNull
    public static String convertKeyToSP(@NonNull final IniKey key) {
        return String.format("%1$s_%2$s", key.section, key.option);
    }

    public static boolean chmod(@NonNull File file, int mode) throws IOException {
        if (!file.exists()) return false;
        try {
            // Set the default permission, so the module could read it.
            final StructStat stat = Os.stat(file.getAbsolutePath());
            if (stat.st_uid != Process.myUid()) {
                // We cannot chmod()
                return false;
            }
            Os.chmod(file.getAbsolutePath(), mode);
            return true;
        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }

    public static boolean addmod(@NonNull File file, int mode) throws IOException {
        if (!file.exists()) return false;
        try {
            // Set the default permission, so the module could read it.
            final StructStat stat = Os.stat(file.getAbsolutePath());
            if (stat.st_uid != Process.myUid()) {
                // We cannot chmod()
                return false;
            }
            Os.chmod(file.getAbsolutePath(), stat.st_mode | mode);
            return true;
        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }
}
