package org.example.scripts;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.OsInfo;
import org.example.browser.shop.ShopListSelector;
import org.example.browser.shop.ShopListTextExtractor;
import org.example.browser.util.ItemsToBuy;

public class MagicGardenAutomation {
    private static MagicGardenAutomation instance;

    private Robot robot;
    private MagicGardenOpener magicGardenOpener;

    public MagicGardenAutomation() throws AWTException {
        this.robot = new Robot();
        this.magicGardenOpener = new MagicGardenOpener();
    }

    public static MagicGardenAutomation getInstance() throws AWTException {
        if (instance == null) {
            instance = new MagicGardenAutomation();
        }
        return instance;
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

    public void begin(ItemsToBuy[] itemsToBuy) {
        int os = checkForOperatingSystem();
        if (os == 1) {
            System.out.println("Chrome must be running with remote debugging on port "
                    + MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT
                    + " (see chrome-with-debugging.bat and README).");
            System.out.println("In that Chrome window, open the game and leave the tab active:");
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
                Thread.sleep(1500);
                try {
                    List<String> shopLines = ShopListTextExtractor.readScrollableListDefaultPort();
                    System.out.println("Shop list (size: " + shopLines.size() + ") - Beginning interfacing with game...");
                    ShopListSelector.beginInterfacingWithGame(shopLines, itemsToBuy);

                } catch (Exception ex) {
                    System.err.println("Could not read shop list via CDP: " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
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

    private void clickShopButton() {
        System.out.println("Opening SHOP (Shift+1)...");
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        System.out.println("Pressing Spacebar...");
        pressAndRelease(KeyEvent.VK_SPACE);
    }

    private void pressAndRelease(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }
}
