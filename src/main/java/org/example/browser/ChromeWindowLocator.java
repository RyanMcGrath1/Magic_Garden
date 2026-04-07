package org.example.browser;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import java.util.Locale;

/**
 * Locates the top-level Magic Garden Chrome window (same heuristics as {@link org.example.MagicGardenOpener}).
 */
public final class ChromeWindowLocator {

    private ChromeWindowLocator() {
    }

    public static boolean isMagicGardenChromeWindow(String windowTitle, String exeName) {
        if (exeName == null || !exeName.equalsIgnoreCase("chrome.exe")) {
            return false;
        }
        if (windowTitle.contains("Magic Garden")) {
            return true;
        }
        return windowTitle.toLowerCase(Locale.ROOT).contains("magicgarden.gg");
    }

    /**
     * @return HWND of the first matching visible window, or {@code null}
     */
    public static WinDef.HWND findMagicGardenChromeHwnd(User32 user32) {
        final WinDef.HWND[] found = { null };
        user32.EnumWindows((hWnd, ignored) -> {
            if (!user32.IsWindowVisible(hWnd)) {
                return true;
            }
            char[] windowText = new char[512];
            user32.GetWindowText(hWnd, windowText, 512);
            String title = Native.toString(windowText);
            IntByReference pidRef = new IntByReference();
            user32.GetWindowThreadProcessId(hWnd, pidRef);
            int pid = pidRef.getValue();
            String exeName = processExeName(pid);
            if (isMagicGardenChromeWindow(title, exeName)) {
                found[0] = hWnd;
                return false;
            }
            return true;
        }, Pointer.NULL);
        return found[0];
    }

    private static String processExeName(int pid) {
        if (pid == 0) {
            return null;
        }
        int rights = com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_LIMITED_INFORMATION
                | com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ;
        com.sun.jna.platform.win32.WinNT.HANDLE process =
                com.sun.jna.platform.win32.Kernel32.INSTANCE.OpenProcess(rights, false, pid);
        if (process == null) {
            return null;
        }
        try {
            char[] buffer = new char[1024];
            IntByReference lpdwSize = new IntByReference(buffer.length);
            boolean ok = com.sun.jna.platform.win32.Kernel32.INSTANCE.QueryFullProcessImageName(process, 0, buffer, lpdwSize);
            if (!ok) {
                return null;
            }
            String fullPath = Native.toString(buffer);
            if (fullPath.isEmpty()) {
                return null;
            }
            return new java.io.File(fullPath).getName();
        } finally {
            com.sun.jna.platform.win32.Kernel32.INSTANCE.CloseHandle(process);
        }
    }
}
