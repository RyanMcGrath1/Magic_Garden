package org.example.browser;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Extra Win32 bindings missing from {@link com.sun.jna.platform.win32.User32} in JNA 5.13.
 */
interface User32ClientMapping extends StdCallLibrary {

    User32ClientMapping INSTANCE = Native.load("user32", User32ClientMapping.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * Converts client-area coordinates to screen coordinates (mutates {@code lpPoint}).
     */
    boolean ClientToScreen(WinDef.HWND hWnd, WinDef.POINT lpPoint);
}
