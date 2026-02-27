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

public class EdgeActivator {

    private final User32 user32;

    public EdgeActivator() {
        this.user32 = User32.INSTANCE;
    }

    /**
     * Searches for a Microsoft Edge window/tab and brings it to the foreground.
     * It first tries to match the process executable name (e.g. msedge.exe). If
     * that isn't decisive it falls back to checking the window title for a
     * user-supplied substring (e.g. "Magic Garden").
     *
     * @return true if Edge (or a matching window) was found and activated, false otherwise
     */
    public boolean bringEdgeToFront() {
        final boolean[] found = { false };

        user32.EnumWindows((hWnd, ignored) -> {

            if (!user32.IsWindowVisible(hWnd)) {
                return true; // continue enumeration
            }

            // Get the window title
            char[] windowText = new char[512];
            user32.GetWindowText(hWnd, windowText, 512);
            String title = Native.toString(windowText);

            // Get the process id that owns this window
            IntByReference pidRef = new IntByReference();
            user32.GetWindowThreadProcessId(hWnd, pidRef);
            int pid = pidRef.getValue();

            String exeName = getProcessExeName(pid);

            // Match by executable name (recommended): msedge.exe for Chromium Edge
            boolean matchesExe = exeName != null && exeName.equalsIgnoreCase("msedge.exe");

            // Fallback: match by title substring (e.g. "Magic Garden")
            boolean matchesTitle = title.contains("Magic Garden");

            if (matchesExe || matchesTitle) {

                // Maximize the window to fill the screen and bring it to the foreground
                bringToFrontAndMaximize(hWnd);

                found[0] = true;
                return false; // stop enumeration
            }

            return true; // continue searching
        }, Pointer.NULL);

        return found[0];
    }

    // Attempt to reliably maximize and foreground the given top-level window.
    private void bringToFrontAndMaximize(WinDef.HWND hWnd) {
        // First try maximizing (works across threads)
        user32.ShowWindow(hWnd, WinUser.SW_MAXIMIZE);

        // Try to set foreground; if Windows blocks it, use AttachThreadInput trick
        int currentThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
        IntByReference pidRef = new IntByReference();
        int targetThreadId = user32.GetWindowThreadProcessId(hWnd, pidRef);

        boolean attached = false;
        try {
            // Attach input queues of current thread and target thread so we can set foreground
            attached = user32.AttachThreadInput(new WinDef.DWORD(currentThreadId), new WinDef.DWORD(targetThreadId), true);

            // Now bring to front and activate
            user32.BringWindowToTop(hWnd);
            user32.SetForegroundWindow(hWnd);
        } finally {
            if (attached) {
                user32.AttachThreadInput(new WinDef.DWORD(currentThreadId), new WinDef.DWORD(targetThreadId), false);
            }
        }
    }

    // Helper: get the process executable name for a PID (e.g. "msedge.exe").
    private String getProcessExeName(int pid) {
        if (pid == 0) {
            return null;
        }

        // Request minimal rights to query the process image name
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
