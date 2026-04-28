package org.example;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MagicGardenOpener {

    /**
     * Game URL — open this in Chrome yourself before running automation ({@link #bringChromeToFront()} does not launch the browser).
     */
    public static final String MAGIC_GARDEN_URL = "https://magicgarden.gg/r/QP9B";

    /** Port for Chrome remote debugging (CDP). Start Chrome with {@code --remote-debugging-port=9222} (see README). */
    public static final int CHROME_REMOTE_DEBUGGING_PORT = 9222;

    /**
     * Outer window size aimed at typical 13-inch laptop viewports (e.g. 1280x800 class panels);
     * clamped to the current monitor work area so it always fits above the taskbar.
     */
    private static final int PREFERRED_OUTER_WIDTH = 1280;
    private static final int PREFERRED_OUTER_HEIGHT = 720;

    private static final int WINDOW_TO_WORK_AREA_MARGIN_PX = 32;

    private static final String MAC_ACTIVATE_SCRIPT =
            ""
                    + "tell application \"Finder\"\n"
                    + "\ttry\n"
                    + "\t\tset deskBounds to bounds of window of desktop\n"
                    + "\ton error\n"
                    + "\t\tset deskBounds to {0, 0, 1920, 1080}\n"
                    + "\tend try\n"
                    + "end tell\n"
                    + "set workLeft to item 1 of deskBounds\n"
                    + "set workTop to item 2 of deskBounds\n"
                    + "set workRight to item 3 of deskBounds\n"
                    + "set workBottom to item 4 of deskBounds\n"
                    + "set workW to workRight - workLeft\n"
                    + "set workH to workBottom - workTop\n"
                    + "set winW to 1280\n"
                    + "set winH to 720\n"
                    + "set margin to "
                    + WINDOW_TO_WORK_AREA_MARGIN_PX
                    + "\n"
                    + "set maxW to workW - margin\n"
                    + "set maxH to workH - margin\n"
                    + "if winW > maxW then set winW to maxW\n"
                    + "if winH > maxH then set winH to maxH\n"
                    + "set cx to workLeft + (workW - winW) div 2\n"
                    + "set cy to workTop + (workH - winH) div 2\n"
                    + "tell application \"Google Chrome\"\n"
                    + "\tif not running then\n"
                    + "\t\treturn \"false\"\n"
                    + "\tend if\n"
                    + "\tif (count windows) < 1 then\n"
                    + "\t\treturn \"false\"\n"
                    + "\tend if\n"
                    + "\trepeat with wi from 1 to (count windows)\n"
                    + "\t\tset w to window wi\n"
                    + "\t\trepeat with ti from 1 to (count tabs of w)\n"
                    + "\t\t\ttell tab ti of w\n"
                    + "\t\t\t\tset tabTitle to title\n"
                    + "\t\t\t\tset tabUrl to URL\n"
                    + "\t\t\tend tell\n"
                    + "\t\t\tif tabTitle contains \"Magic Garden\" or tabUrl contains \"magicgarden.gg\" then\n"
					+ "\t\t\t\tset index of w to 1\n"
					+ "\t\t\t\tset active tab index of w to ti\n"
					+ "\t\t\t\tset bounds of w to {cx, cy, cx + winW, cy + winH}\n"
					+ "\t\t\t\tactivate\n"
					+ "\t\t\t\ttry\n"
					+ "\t\t\t\t\tdelay 0.25\n"
					+ "\t\t\t\t\ttell application \"System Events\"\n"
					+ "\t\t\t\t\t\ttell process \"Google Chrome\"\n"
					+ "\t\t\t\t\t\t\tset frontmost to true\n"
					+ "\t\t\t\t\t\t\tset clickX to cx + (winW div 2)\n"
					+ "\t\t\t\t\t\t\tset clickY to cy + (winH div 2) + 40\n"
					+ "\t\t\t\t\t\t\tclick at {clickX, clickY}\n"
					+ "\t\t\t\t\t\tend tell\n"
					+ "\t\t\t\t\tend tell\n"
					+ "\t\t\t\tend try\n"
					+ "\t\t\t\treturn \"true\"\n"
                    + "\t\t\tend if\n"
                    + "\t\tend repeat\n"
                    + "\tend repeat\n"
                    + "end tell\n"
                    + "return \"false\"\n";

    public MagicGardenOpener() {
        // Do not touch User32 on macOS — JNA Win32 load can fail outside Windows.
    }

    private static User32 win32() {
        return User32.INSTANCE;
    }

    /**
     * Finds an already-open Magic Garden tab in Google Chrome (by window title / process), sizes and focuses it.
     * Does not start Chrome; the user must open {@link #MAGIC_GARDEN_URL} in Chrome first.
     *
     * @return true if a matching window was found and activated, false otherwise
     */
    public boolean bringChromeToFront() {
        if (OsInfo.isMacOs()) {
            return tryActivateMagicGardenInChromeMac();
        }
        return tryActivateMagicGardenInChrome();
    }

    /**
     * True only for a visible top-level window whose process is {@code chrome.exe} and whose title
     * suggests the Magic Garden tab (page title or URL shown in the caption).
     */
    private boolean isMagicGardenOpenInChrome(String windowTitle, String exeName) {
        if (exeName == null || !exeName.equalsIgnoreCase("chrome.exe")) {
            return false;
        }
        if (windowTitle.contains("Magic Garden")) {
            return true;
        }
        return windowTitle.toLowerCase(Locale.ROOT).contains("magicgarden.gg");
    }

    private boolean tryActivateMagicGardenInChromeMac() {
        ProcessBuilder pb = new ProcessBuilder("osascript", "-");
        try {
            Process p = pb.start();
            try (Writer w = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8)) {
                w.write(MAC_ACTIVATE_SCRIPT);
            }
            String out;
            try (java.io.InputStream is = p.getInputStream()) {
                out = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            if (!p.waitFor(30, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return false;
            }
            return "true".equals(out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean tryActivateMagicGardenInChrome() {
        User32 user32 = win32();
        final boolean[] found = { false };

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

            String exeName = getProcessExeName(pid);

            if (isMagicGardenOpenInChrome(title, exeName)) {
                bringToFrontWithLaptopFriendlySize(hWnd);
                found[0] = true;
                return false;
            }

            return true;
        }, Pointer.NULL);

        return found[0];
    }

    /**
     * Restores if maximized, sizes the window to {@link #PREFERRED_OUTER_WIDTH}×{@link #PREFERRED_OUTER_HEIGHT}
     * (clamped to the primary display's fullscreen-client size), centered, then foreground.
     * Uses {@link WinUser#SM_CXFULLSCREEN}/{@link WinUser#SM_CYFULLSCREEN} so we do not need
     * {@code SystemParametersInfo} (not exposed on {@link User32} in JNA 5.13).
     */
    private void bringToFrontWithLaptopFriendlySize(WinDef.HWND hWnd) {
        User32 user32 = win32();
        user32.ShowWindow(hWnd, WinUser.SW_RESTORE);

        WinDef.RECT workArea = new WinDef.RECT();
        workArea.left = 0;
        workArea.top = 0;
        workArea.right = user32.GetSystemMetrics(WinUser.SM_CXFULLSCREEN);
        workArea.bottom = user32.GetSystemMetrics(WinUser.SM_CYFULLSCREEN);

        int workW = workArea.right - workArea.left;
        int workH = workArea.bottom - workArea.top;
        int maxW = Math.max(320, workW - WINDOW_TO_WORK_AREA_MARGIN_PX);
        int maxH = Math.max(240, workH - WINDOW_TO_WORK_AREA_MARGIN_PX);
        int w = Math.min(PREFERRED_OUTER_WIDTH, maxW);
        int h = Math.min(PREFERRED_OUTER_HEIGHT, maxH);
        int x = workArea.left + (workW - w) / 2;
        int y = workArea.top + (workH - h) / 2;

        user32.SetWindowPos(
                hWnd,
                new WinDef.HWND(Pointer.NULL),
                x,
                y,
                w,
                h,
                WinUser.SWP_SHOWWINDOW);

        int currentThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
        IntByReference pidRef = new IntByReference();
        int targetThreadId = user32.GetWindowThreadProcessId(hWnd, pidRef);

        boolean attached = false;
        try {
            attached = user32.AttachThreadInput(new WinDef.DWORD(currentThreadId), new WinDef.DWORD(targetThreadId), true);
            user32.BringWindowToTop(hWnd);
            user32.SetForegroundWindow(hWnd);
        } finally {
            if (attached) {
                user32.AttachThreadInput(new WinDef.DWORD(currentThreadId), new WinDef.DWORD(targetThreadId), false);
            }
        }
    }

    private String getProcessExeName(int pid) {
        if (pid == 0) {
            return null;
        }

        int rights = WinNT.PROCESS_QUERY_LIMITED_INFORMATION | WinNT.PROCESS_VM_READ;
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(rights, false, pid);
        if (process == null) {
            return null;
        }

        try {
            char[] buffer = new char[1024];
            IntByReference lpdwSize = new IntByReference(buffer.length);
            boolean ok = Kernel32.INSTANCE.QueryFullProcessImageName(process, 0, buffer, lpdwSize);
            if (!ok) {
                return null;
            }
            String fullPath = Native.toString(buffer);
            if (fullPath.isEmpty()) {
                return null;
            }
            return new File(fullPath).getName();
        } finally {
            Kernel32.INSTANCE.CloseHandle(process);
        }
    }
}
