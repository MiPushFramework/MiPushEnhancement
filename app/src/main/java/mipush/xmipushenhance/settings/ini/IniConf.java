package mipush.xmipushenhance.settings.ini;

import android.content.Context;
import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import mipush.xmipushenhance.Constants;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mipush.xmipushenhance.settings.ini.IniConstants.*;

/**
 * The configuration that can be read both in module hook context
 * and the configuration UI.
 *
 * The reason of not using SharedPreference directly is that it can hardly be chmod-ed to world readable,
 * since the API is not supported since Android N, and the SharedPreferenceImpl chmods it every time when being constructed.
 *
 * Copying the SharedPreference XML to other places is still hard since the XML structure is not best optimized for our private use.
 *
 * Creating a configuration ourselves is the best solution overall. It will be stored in the data folder for each user.
 * The module will try to read it every time it hooks an app. If the configuration is absent for the user, the module
 * will fallback to the default configuration. Hence, it is important to install the module and configure it every time you
 * create a new user or profile.
 *
 * The reason of choosing ini is that it is simple to read and write for both human and computer. Furthermore, it is
 * the most common format of configuration in *nix systems. Doing so will enable us to create the most *nix-like configuration style.
 *
 * Some paths to use: (They are not hard-coded since the internal path is subject to change on different ROMs)
 * /data/user/user_id/mipush.xmipushenhance/etc/
 * /data/user/user_id/mipush.xmipushenhance/etc/module.conf
 */
public class IniConf {
    private static final String TAG = "IniConf";

    private final Ini mIni;

    /**
     * Construct using an ini file.
     * If the file is absent, it will be created.
     * If the file is null, write() will do nothing.
     */
    public IniConf(@Nullable final File file) throws IOException {
        Log.d(TAG, "IniConf() in uid " + Process.myUid() + ", pid " + Process.myPid());
        if (file != null) ensurePermission(file);
        Log.d(TAG, "Using configuration " + file);
        mIni = createDefaultConfig(file);
        // TODO: File locking?
    }

    /**
     * Construct using the default path
     */
    public IniConf(int userId, @NonNull Context context) throws IOException {
        this(Constants.getConfigPath(userId, context));
    }

    /**
     * Construct using the default path (with current userId)
     */
    public IniConf(@NonNull Context context) throws IOException {
        this(Constants.getConfigPath(Process.myUserHandle().hashCode(), context));
    }

    /**
     * Creates or upgrades the current configuration.
     * @param file The current configuration file.
     * @return The upgraded ini. It will not be stored in file system.
     * @throws IOException In case there's errors.
     */
    private Ini createDefaultConfig(@Nullable final File file) throws IOException {
        final Ini defaultIni = new Ini();
        defaultIni.load(new ByteArrayInputStream(INI_DEFAULT.getBytes(StandardCharsets.UTF_8)));
        final Ini currentIni = new Ini();
        if (file != null) {
            currentIni.setFile(file);
            if (file.exists()) {
                currentIni.load(file);
            }
        }
        // Instead of just copying, we compare and proceed the diffs.
        // Check for absent options to be added.
        // We do not remove deprecated options, since
        // we cannot automatically transform them to the new keys, we
        // still need them for backward compatibility.
        compare(defaultIni, currentIni);
        return currentIni;
    }

    /**
     * Compare parent > child. Copy missing sections or options from the parent to child.
     * This method does not support nested sections.
     * @param parent Parent INI. It will not be changed.
     * @param child Child INI. It will be changed.
     */
    private void compare(@NonNull Ini parent, @NonNull Ini child) {
        for (final String section : parent.keySet()) {
            if (!child.containsKey(section)) {
                Log.i(TAG, "Adding section " + section);
                child.put(section, parent.get(section));
                child.putComment(section, parent.getComment(section));
            } else {
                final Profile.Section parentSection = parent.get(section);
                final Profile.Section childSection = child.get(section);
                for (final String option : parentSection.keySet()) {
                    if (!childSection.containsKey(option)) {
                        Log.i(TAG, "Adding option " + option);
                        childSection.put(option, parentSection.get(option));
                        childSection.putComment(option, parentSection.getComment(option));
                    }
                }
            }
        }
    }

    /**
     * Get all options
     */
    public Map<IniKey, Object> getAll() {
        Map<IniKey, Object> map = new HashMap<>(mIni.keySet().size() * 3);
        for(final String sectionKey : mIni.keySet()) {
            for (final Profile.Section section : mIni.getAll(sectionKey)) {
                for (final String option : section.keySet()) {
                    map.put(new IniKey(sectionKey, option),
                            section.get(option));
                }
            }
        }
        return map;
    }

    /**
     * Write all changes to the file system. If the file is absent, it will be created.
     */
    public void write() throws IOException {
        Log.d(TAG, "Saving configuration");
        if (mIni.getFile() != null) {
            // Create the parent if needed.
            final File parent = mIni.getFile().getParentFile();
            if (!parent.exists()) {
                parent.mkdir();
            }
            // Write, or create.
            mIni.store();

            ensurePermission(mIni.getFile());
        } else {
            Log.w(TAG, "File is null. Ignoring.");
        }
    }

    private void ensurePermission(@NonNull final File iniFile) throws IOException {
        try {
            if (!iniFile.exists()) return;
            final StructStat stat = Os.stat(iniFile.getAbsolutePath());
            if (stat.st_uid != Process.myUid()) {
                // Do not run at all
                return;
            }
        } catch (ErrnoException ignored) {
            return;
        }

        // Apply other +x to each parent folders.
        // Typically /, /data, /data/data, /data/user should have this bit set
        File current = iniFile;
        while(true) {
            final File parent = current.getParentFile();
            if (parent == null) break;
            // It will return false if we can not chmod.
            // From then on, we will be reaching the furthest folder we can chmod.
            if (IniHelper.addmod(parent, DESIRED_PERMISSION_PARENT)) {
                Log.d(TAG, String.format("Successfully added S_IXOTH to parent %1$s", parent.getAbsolutePath()));
                current = parent;
            } else {
                break;
            }
        }
        IniHelper.chmod(iniFile, DESIRED_PERMISSION_INI);
    }

    /**
     * Get a value
     */
    @Nullable
    public String get(@NonNull IniKey key) {
        return mIni.get(key.section, key.option);
    }

    /**
     * Get a value with default value
     */
    public String get(@NonNull IniKey key, String defaultValue) {
        final String value = get(key);
        if (value == null) return defaultValue;
        return value;
    }

    /**
     * Get an array
     */
    @Nullable
    public List<String> getAll(@NonNull IniKey key) {
        final Profile.Section section = mIni.get(key.section);
        if (section == null) return null;
        return section.getAll(key.option);
    }

    /**
     * Get an array with default value
     */
    public List<String> getAll(@NonNull IniKey key, List<String> defaultValue) {
        final List<String> value = getAll(key);
        if (value == null) return defaultValue;
        return value;
    }

    /**
     * Put a value
     */
    public void put(@NonNull IniKey key, @Nullable Object value) {
        Log.d(TAG, "put() " + key + ", " + value);
        mIni.put(key.section, key.option, value);
    }

    /**
     * Put an array
     */
    public void putAll(@NonNull IniKey key, @Nullable List<String> value) {
        final Profile.Section section = mIni.get(key.section);
        if (section == null) return;
        section.putAll(key.option, value);
    }

    public File getRawConfigurationFile() {
        return mIni.getFile();
    }
}
