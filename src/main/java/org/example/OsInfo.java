package org.example;

import java.util.Locale;

/**
 * Detects the host OS using {@link System#getProperty(String) System property} {@code os.name}.
 */
public final class OsInfo {

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

    private OsInfo() {
    }

    public static boolean isWindows() {
        return OS_NAME.startsWith("windows");
    }

    /** True for macOS (reports as {@code Mac OS X} / {@code Mac OS} on the JVM). */
    public static boolean isMacOs() {
        return OS_NAME.startsWith("mac os") || OS_NAME.contains("darwin");
    }
}
