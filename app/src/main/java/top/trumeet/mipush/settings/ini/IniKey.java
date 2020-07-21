package top.trumeet.mipush.settings.ini;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Describes a key in an ini
 */
public final class IniKey {
    public final String section;
    public final String option;

    public IniKey(@Nullable String section, @NonNull String option) {
        this.section = section;
        this.option = option;
    }

    @NonNull
    public static IniKey of(@Nullable String section, @NonNull String option) {
        return new IniKey(section, option);
    }

    @NonNull
    public static IniKey of(@NonNull String spKey) {
        return IniUtils.convertKeyToIni(spKey);
    }

    @NonNull
    public String toSP() {
        return IniUtils.convertKeyToSP(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IniKey iniKey = (IniKey) o;
        return Objects.equals(section, iniKey.section) &&
                Objects.equals(option, iniKey.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, option);
    }

    @Override
    public String toString() {
        return String.format("[%1$s] %2$s", section, option);
    }
}
