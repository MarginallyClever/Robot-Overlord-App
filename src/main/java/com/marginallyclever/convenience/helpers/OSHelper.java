package com.marginallyclever.convenience.helpers;

import java.util.Locale;

public class OSHelper {
    private static String os = "";
    private static boolean isMacOS;
    private static boolean isWindows;
    private static boolean isLinux;

    {
        os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        isWindows = os.contains("win");
        isMacOS = os.contains("mac") || os.contains("darwin");
        isLinux = !isWindows && !isMacOS;
    }

    public static String getOS() {
        return os;
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isMac() {
        return isMacOS;
    }

    public static boolean isLinux() {
        return isLinux;
    }
}
