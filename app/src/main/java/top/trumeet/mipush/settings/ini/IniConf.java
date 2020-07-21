package top.trumeet.mipush.settings.ini;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.FileUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static top.trumeet.mipush.settings.ini.IniConstants.CONF_FILE;
import static top.trumeet.mipush.settings.ini.IniConstants.CONF_PATH;
import static top.trumeet.mipush.settings.ini.IniConstants.INI_DEFAULT;
import static top.trumeet.mipush.settings.ini.IniConstants.TAG;
import static top.trumeet.mipush.settings.ini.IniConstants.rwxrwxrwx;
import static top.trumeet.mipush.settings.ini.IniUtils.setFilePermissionsFromMode;

/**
 * The configuration that can be read both in module hook context
 * and the configuration UI.
 * <p>
 * The reason of not using SharedPreference directly is that it can hardly be chmod-ed to world readable,
 * since the API is not supported since Android N, and the SharedPreferenceImpl chmods it every time when being constructed.
 * </p>
 * Copying the SharedPreference XML to other places is still hard since the XML structure is not best optimized for our private use.
 * <p>
 * Creating a configuration ourselves is the best solution overall. It will be stored in the data folder for each user.
 * The module will try to read it every time it hooks an app. If the configuration is absent for the user, the module
 * will fallback to the default configuration. Hence, it is important to install the module and configure it every time you
 * create a new user or profile.
 * The reason of choosing ini is that it is simple to read and write for both human and computer. Furthermore, it is
 * the most common format of configuration in *nix systems. Doing so will enable us to create the most *nix-like configuration style.
 * </p>
 * Some paths to use: (They are not hard-coded since the internal path is subject to change on different ROMs)
 * /data/user_de/user_id/top.trumeet.mipush.xmipushenhance/etc/
 * /data/user_de/user_id/top.trumeet.mipush.xmipushenhance/etc/module.conf
 */
public class IniConf {

    private final Ini mIni;

    /**
     * Construct using an ini file.
     * If the file is absent, it will be created.
     * If the file is null, write() will do nothing.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public IniConf(final File file) throws IOException {
        final File conf_path = new File(file, CONF_PATH);
        final File conf_file = new File(conf_path, CONF_FILE);

        FileUtils.setPermissions(file.getAbsolutePath() + "/", rwxrwxrwx, -1, -1);

        if (!conf_path.exists()) {
            conf_path.mkdir();
        }

        FileUtils.setPermissions(conf_path.getAbsolutePath() + "/", rwxrwxrwx, -1, -1);

        mIni = createDefaultConfig(conf_file);
        // TODO: File locking?
    }

    /**
     * Construct using the default path
     */
    public IniConf(int userId) throws IOException {
        this(IniUtils.getConfigPath(userId));
    }

    /**
     * Creates or upgrades the current configuration.
     *
     * @param file The current configuration file.
     * @return The upgraded ini. It will not be stored in file system.
     * @throws IOException In case there's errors.
     */
    private Ini createDefaultConfig(final File file) throws IOException {
        final Ini defaultIni = new Ini();
        defaultIni.load(new ByteArrayInputStream(INI_DEFAULT.getBytes(StandardCharsets.UTF_8)));
        final Ini currentIni = new Ini();
        currentIni.setFile(file);
        if (file.exists()) {
            currentIni.load(file);
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
     *
     * @param parent Parent INI. It will not be changed.
     * @param child  Child INI. It will be changed.
     */
    private void compare(@NonNull Ini parent, @NonNull Ini child) {
        for (final String section : parent.keySet()) {
            if (!child.containsKey(section)) {
//                Log.i(TAG, "Adding section " + section);
                child.put(section, parent.get(section));
                child.putComment(section, parent.getComment(section));
            } else {
                final Profile.Section parentSection = parent.get(section);
                final Profile.Section childSection = child.get(section);
                for (final String option : Objects.requireNonNull(parentSection).keySet()) {
                    if (!Objects.requireNonNull(childSection).containsKey(option)) {
//                        Log.i(TAG, "Adding option " + option);
                        childSection.put(option, Objects.requireNonNull(parentSection.get(option)));
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
        for (final String sectionKey : mIni.keySet()) {
            for (final Profile.Section section : mIni.getAll(sectionKey)) {
                for (final String option : section.keySet()) {
                    map.put(new IniKey(sectionKey, option),
                            Objects.requireNonNull(section.get(option)));
                }
            }
        }
        return map;
    }

    /**
     * Write all changes to the file system. If the file is absent, it will be created.
     */
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation"})
    public void write() throws IOException {
//        Log.d(TAG, "Saving configuration");
        if (mIni.getFile() != null) {
            // Create the parent if needed.
            final File parent = mIni.getFile().getParentFile();
            FileUtils.setPermissions(parent.getParent() + "/", 511, -1, -1);
            if (!Objects.requireNonNull(parent).exists()) {
                parent.mkdir();
            }
            // Write, or create.
            mIni.store();

            setFilePermissionsFromMode(mIni.getFile().getAbsolutePath(), Context.MODE_WORLD_READABLE);
        } else {
            Log.w(TAG, "File is null. Ignoring.");
        }
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
//        Log.d(TAG, "put() " + key + ", " + value);
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
