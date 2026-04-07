package org.example.scripts;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.example.MagicGardenOpener;
import org.example.OsInfo;
import org.example.ScreenSample;
import org.example.browser.ChromeViewportMapper;
import org.example.browser.ChromeWindowLocator;
import org.example.browser.shop.ShopListTextExtractor;
import org.example.elements.ScreenPoint;

public class Scripts {
    private static Scripts instance;

    private Robot robot;
    private MagicGardenOpener magicGardenOpener;

    public Scripts() throws AWTException {
        this.robot = new Robot();
        this.magicGardenOpener = new MagicGardenOpener();
    }

    public static Scripts getInstance() throws AWTException {
        if (instance == null) {
            instance = new Scripts();
        }
        return instance;
    }

    public void moveMouseToPoint(ScreenPoint point) {
        robot.mouseMove(point.x(), point.y());
    }

    public void clickPoint(ScreenPoint point) {
        robot.mouseMove(point.x(), point.y());
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Clicks a point in the Chrome client using normalized coordinates (0–1), mapped via
     * {@link ChromeViewportMapper} to screen pixels for {@link Robot}.
     *
     * @param nx horizontal fraction of client width (0 = left)
     * @param ny vertical fraction of client height (0 = top)
     */
    public void clickNormalizedInChromeClient(double nx, double ny) {
        WinDef.HWND hwnd = ChromeWindowLocator.findMagicGardenChromeHwnd(User32.INSTANCE);
        if (hwnd == null) {
            throw new IllegalStateException("Magic Garden Chrome window not found");
        }
        Point p = ChromeViewportMapper.screenPointFromNormalizedClient(User32.INSTANCE, hwnd, nx, ny);
        robot.mouseMove(p.x, p.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Clicks using pixel coordinates from a reference {@link ChromeViewportMapper#REFERENCE_CLIENT_WIDTH}×
     * {@link ChromeViewportMapper#REFERENCE_CLIENT_HEIGHT} layout.
     */
    public void clickReferenceLayoutInChromeClient(double refPixelX, double refPixelY) {
        WinDef.HWND hwnd = ChromeWindowLocator.findMagicGardenChromeHwnd(User32.INSTANCE);
        if (hwnd == null) {
            throw new IllegalStateException("Magic Garden Chrome window not found");
        }
        Point p = ChromeViewportMapper.screenPointFromReferenceLayout(User32.INSTANCE, hwnd, refPixelX, refPixelY);
        robot.mouseMove(p.x, p.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public int checkForOperatingSystem() {
        if (OsInfo.isWindows()) {
            System.out.println("Preflight: Windows detected.");
            return 1;
        }
        if (OsInfo.isMacOs()) {
            System.out.println("Preflight: macOS derivative detected.");
            return 2;
        }
        System.err.println("Preflight: unsupported OS (os.name=" + System.getProperty("os.name") + ").");
        System.exit(1);
        throw new IllegalStateException("unreachable");
    }

    public void begin() {
        int os = checkForOperatingSystem();
        if (os == 1) {
            System.out.println("Open Magic Garden in Google Chrome first, then leave that tab active:");
            System.out.println("  " + MagicGardenOpener.MAGIC_GARDEN_URL);
            System.out.println("Beginning startup sequence — locating the Chrome window...");
            try {
                if (!magicGardenOpener.bringChromeToFront()) {
                    System.err.println("ERROR: Magic Garden was not found in Chrome.");
                    System.err.println("Open this URL in Chrome, load the game, then run this program again:");
                    System.err.println("  " + MagicGardenOpener.MAGIC_GARDEN_URL);
                    System.exit(1);
                    return;
                }
                System.out.println("Google Chrome is ready. Waiting 5 seconds before continuing...");
                Thread.sleep(5000);
                clickShopButton();
                ShopListTextExtractor.inspectDefaultPort();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException(e);
            }
        } else if (os == 2) {
            // TODO: Add macOS support
        }
    }

    /** Opens SHOP via Shift + 1 (digit row), then sends Spacebar. */
    private void clickShopButton() {
        System.out.println("Opening SHOP (Shift+1)...");
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        System.out.println("Pressing Spacebar...");
        pressAndRelease(KeyEvent.VK_SPACE);
    }

    private void navigateToStartingPoint(boolean isGardenAbove) {
        if (isGardenAbove) {
            for (int i = 0; i < 10; i++) {
                pressAndRelease(KeyEvent.VK_UP);
            }
            for (int i = 0; i < 10; i++) {
                pressAndRelease(KeyEvent.VK_LEFT);
            }
        } else {
            pressAndRelease(KeyEvent.VK_DOWN);
            for (int i = 0; i < 10; i++) {
                pressAndRelease(KeyEvent.VK_LEFT);
            }
        }
    }

    private void pressAndRelease(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }
}
