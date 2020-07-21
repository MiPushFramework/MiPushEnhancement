package top.trumeet.mipush.settings.ini;

public final class IniConstants {
    public static final String TAG = "MiPushIniConfig";

    // linux permission rwx rwx rwx, OCT is 00777, DEX is 511
    public static final int rwxrwxrwx = 511;

    public static final IniKey MODULE_WORKING_MODE = new IniKey("module", "working_mode");
    public static final IniKey MODULE_BLACKLIST = new IniKey("module", "blacklist");
    public static final IniKey MODULE_WHITELIST = new IniKey("module", "whitelist");

    // For the users which do not have the module installed, we are unable to read from assets.
    // Hence, hardcoding into jar is a "not so good" choice.
    public static final String INI_DEFAULT = "# XMiPushEnhance configuration\n" +
            "# The default configuration will be extracted if /data/user/user id/top.trumeet.mipush.xmipushenhance/etc/module.conf is absent.\n" +
            "#\n" +
            "# This file will be overwritten when you change through the UI. You can change it via the UI or by hand.\n" +
            "#\n" +
            "# For upgrading, the module will fill any absent options that are not in the system but in the default configuration.\n" +
            "# However, it will never automatically delete options.\n" +
            "\n" +
            "# Module settings\n" +
            "[module]\n" +
            "working_mode = blacklist\n" +
            "# blacklist =\n" +
            "# whitelist =\n";
}
