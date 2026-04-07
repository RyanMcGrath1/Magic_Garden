package org.example.browser;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Maps normalized client coordinates (0–1) to global screen pixels for {@link java.awt.Robot} clicks.
 *
 * <p><b>Strategy:</b> Store UI targets as fractions of the Chrome <em>client</em> area measured at a
 * reference layout, then scale to the current client size at runtime. This stays more consistent
 * across screen sizes when the window is resized or DPI differs.</p>
 *
 * <p><b>Assumptions:</b> Chrome zoom is 100% and Windows display scaling is stable; the HWND is the
 * outer Chrome window whose client area matches the page viewport.</p>
 */
public final class ChromeViewportMapper {

    /**
     * Logical width used when you calibrated (nx, ny) — e.g. match {@link org.example.MagicGardenOpener}
     * {@code PREFERRED_OUTER_WIDTH} minus chrome frame.
     */
    public static final int REFERENCE_CLIENT_WIDTH = 1280;

    /** Logical height used when you calibrated normalized coordinates. */
    public static final int REFERENCE_CLIENT_HEIGHT = 720;

    private ChromeViewportMapper() {
    }

    /**
     * Converts normalized client coordinates (0–1) to global screen coordinates for {@code Robot.mouseMove}.
     *
     * @param nx horizontal position in client space, 0 = left edge, 1 = right edge
     * @param ny vertical position in client space, 0 = top edge, 1 = bottom edge
     */
    public static Point screenPointFromNormalizedClient(User32 user32, WinDef.HWND chromeHwnd, double nx, double ny) {
        WinDef.RECT client = new WinDef.RECT();
        if (!user32.GetClientRect(chromeHwnd, client)) {
            throw new IllegalStateException("GetClientRect failed");
        }
        int cw = client.right - client.left;
        int ch = client.bottom - client.top;
        if (cw <= 0 || ch <= 0) {
            throw new IllegalStateException("Invalid client size: " + cw + "x" + ch);
        }
        int cx = (int) Math.round(nx * cw);
        int cy = (int) Math.round(ny * ch);
        WinDef.POINT pt = new WinDef.POINT();
        pt.x = cx;
        pt.y = cy;
        if (!User32ClientMapping.INSTANCE.ClientToScreen(chromeHwnd, pt)) {
            throw new IllegalStateException("ClientToScreen failed");
        }
        return new Point(pt.x, pt.y);
    }

    /**
     * Same as {@link #screenPointFromNormalizedClient} where reference pixel coords were measured on a
     * {@link #REFERENCE_CLIENT_WIDTH}×{@link #REFERENCE_CLIENT_HEIGHT} logical client (e.g. your design mock).
     */
    public static Point screenPointFromReferenceLayout(
            User32 user32, WinDef.HWND chromeHwnd, double refPixelX, double refPixelY) {
        return screenPointFromNormalizedClient(
                user32,
                chromeHwnd,
                refPixelX / REFERENCE_CLIENT_WIDTH,
                refPixelY / REFERENCE_CLIENT_HEIGHT);
    }

    /**
     * Maps a rectangle in reference client pixels (same space as {@link #screenPointFromReferenceLayout}) to a screen
     * {@link Rectangle} for {@link java.awt.Robot#createScreenCapture}.
     */
    public static Rectangle screenRectangleFromReferenceLayout(
            User32 user32, WinDef.HWND chromeHwnd,
            double refLeft, double refTop, double refWidth, double refHeight) {
        Point topLeft = screenPointFromReferenceLayout(user32, chromeHwnd, refLeft, refTop);
        Point bottomRight = screenPointFromReferenceLayout(
                user32, chromeHwnd, refLeft + refWidth, refTop + refHeight);
        int w = Math.max(0, bottomRight.x - topLeft.x);
        int h = Math.max(0, bottomRight.y - topLeft.y);
        return new Rectangle(topLeft.x, topLeft.y, w, h);
    }
}
