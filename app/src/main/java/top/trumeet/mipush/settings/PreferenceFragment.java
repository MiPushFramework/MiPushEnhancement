package top.trumeet.mipush.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Html;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.meowcat.xposed.mipush.R;
import org.meowcat.xposed.mipush.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import moe.shizuku.preference.MultiSelectListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.SimpleMenuPreference;
import moe.shizuku.preference.SwitchPreference;
import top.trumeet.mipush.settings.ini.IniConf;
import top.trumeet.mipush.settings.ini.IniConstants;
import top.trumeet.mipush.settings.ini.IniUtils;

import static org.meowcat.xposed.mipush.Utils.hideIcon;

public class PreferenceFragment extends moe.shizuku.preference.PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private IniConf mConf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            try {
                mConf = new IniConf(requireContext());
            } catch (IOException e) {
                // We have encountered an unrecoverable error.
                throw new RuntimeException(e);
            }
            render();

            SwitchPreference hide_icon = (SwitchPreference) findPreference("hide_icon");
            hide_icon.setChecked(mConf.get(IniUtils.convertKeyToIni("hide_icon"), "false").equalsIgnoreCase("true"));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.preferences);

        Preference device_status = findPreference("device_status");
        String status = Utils.isEnhancementEnabled() ? getString(R.string.pref_enhancement_status_success) : getString(R.string.pref_enhancement_status_failed);
        status = String.format("%s\n\n%s", status, String.format(getString(R.string.pref_enhancement_status_summary), Build.MANUFACTURER, SystemProperties.get("ro.product.manufacturer", "failed"), SystemProperties.get("ro.product.vendor.manufacturer", "failed"), Build.BRAND, SystemProperties.get("ro.product.brand", "failed"), SystemProperties.get("ro.product.vendor.brand", "failed"), SystemProperties.get("ro.miui.ui.version.name", "failed"), SystemProperties.get("ro.miui.ui.version.code", "failed"), SystemProperties.get("ro.miui.version.code_time", "failed")));
        device_status.setSummary(status);
    }

    /**
     * Render the content of ini to preferences.
     * <p>
     * A set of well-known keys will be defined in preferences.xml.
     * For non-known keys, we will not show them through UI.
     */
    private void render() {
        ((SimpleMenuPreference) getPreferenceScreen().findPreference(IniConstants.MODULE_WORKING_MODE.toSP()))
                .setValue(mConf.get(IniConstants.MODULE_WORKING_MODE));
        // TODO: Add a UI
        final MultiSelectListPreference blacklist = ((MultiSelectListPreference) getPreferenceScreen().findPreference(IniConstants.MODULE_BLACKLIST.toSP()));
        final List<String> bl = mConf.getAll(IniConstants.MODULE_BLACKLIST, Collections.emptyList());
        fillPrefWithAppList(blacklist, bl.toArray(new String[]{}));
        if (!bl.isEmpty()) {
            blacklist.setValues(new HashSet<>(bl));
        }

        final MultiSelectListPreference whitelist = ((MultiSelectListPreference) getPreferenceScreen().findPreference(IniConstants.MODULE_WHITELIST.toSP()));
        final List<String> wl = mConf.getAll(IniConstants.MODULE_WHITELIST, Collections.emptyList());
        fillPrefWithAppList(whitelist, wl.toArray(new String[]{}));
        if (!wl.isEmpty()) {
            whitelist.setValues(new HashSet<>(wl));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Log.d(TAG, "onSharedPreferenceChanged(): " + key);
        if (key.equals("module_blacklist") || key.equals("module_whitelist")) {
            mConf.putAll(IniUtils.convertKeyToIni(key), new ArrayList<>(Objects.requireNonNull(sharedPreferences.getStringSet(key, Collections.emptySet()))));
        } else if (key.equals("hide_icon")) {
            boolean isChecked = sharedPreferences.getBoolean(key, false);
            mConf.put(IniUtils.convertKeyToIni(key), isChecked ? "true" : "false");
            hideIcon(requireContext().getPackageManager(), new ComponentName(requireContext(), PreferenceActivity.class.getName()), isChecked);
        } else {
            mConf.put(IniUtils.convertKeyToIni(key), sharedPreferences.getString(key, null));
        }

        try {
            mConf.write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 0, 0, R.string.pref_menu_get_conf_path);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_menu_get_conf_path)
                    .setMessage(Html.fromHtml(String.format("<code>%1$s</code>",
                            mConf.getRawConfigurationFile().getAbsolutePath()), 0))
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillPrefWithAppList(@NonNull MultiSelectListPreference preference, @Nullable String[] additional) {
        // TODO: Add package names and icons
        final List<Pair<String, String>> apps = requireContext().getPackageManager().getInstalledApplications(0).stream()
                .map(pkg -> new Pair<>(pkg.packageName, pkg.packageName))
                .collect(Collectors.toList());

        if (additional != null && additional.length > 0) {
            final List<String> tempPkgs = apps.stream()
                    .map(pkg -> pkg.second)
                    .collect(Collectors.toList());

            apps.addAll(Stream.of(additional)
                    .filter(pkg -> !tempPkgs.contains(pkg))
                    .map(pkg -> new Pair<>(pkg, pkg))
                    .collect(Collectors.toList()));
        }

        apps.sort(Comparator.comparing(p -> p.second));

        preference.setEntries(apps.stream()
                .map(pkg -> pkg.second)
                .collect(Collectors.toList())
                .toArray(new String[]{}));
        preference.setEntryValues(apps.stream()
                .map(pkg -> pkg.first)
                .collect(Collectors.toList())
                .toArray(new String[]{}));
    }
}
