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
    private static final long SHOP_CYCLE_INTERVAL_MS = 5 * 60 * 1000L;

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

    /**
     * Runs the Windows shop automation in a loop: each cycle focuses Chrome, opens the shop, reads the list via CDP,
     * and attempts purchases. Waits {@link #SHOP_CYCLE_INTERVAL_MS} after each cycle before starting the next.
     */
    public void begin(ItemsToBuy[] itemsToBuy) {
        int os = checkForOperatingSystem();
        if (os == 1) {
            System.out.println("Chrome must be running with remote debugging on port "
                    + MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT
                    + " (see chrome-with-debugging.bat and README).");
            System.out.println("In that Chrome window, open the game and leave the tab active:");
            System.out.println("  " + MagicGardenOpener.MAGIC_GARDEN_URL);
            System.out.println(
                    "Repeating shop automation every "
                            + (SHOP_CYCLE_INTERVAL_MS / 60_000)
                            + " minutes after each cycle. Press Ctrl+C in this console to stop.");
            try {
                int cycle = 1;
                while (true) {
                    runWindowsShopCycle(itemsToBuy, cycle);
                    cycle++;
                    System.out.println(
                            "Next shop run in "
                                    + (SHOP_CYCLE_INTERVAL_MS / 60_000)
                                    + " minutes... (Ctrl+C to stop)");
                    Thread.sleep(SHOP_CYCLE_INTERVAL_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Shop automation interrupted; stopping.");
            }
        } else if (os == 2) {
            // TODO: Add macOS support
        }
    }

    private void runWindowsShopCycle(ItemsToBuy[] itemsToBuy, int cycleNumber) throws InterruptedException {
        System.out.println("--- Shop cycle " + cycleNumber + " ---");
        if (!magicGardenOpener.bringChromeToFront()) {
            System.err.println("ERROR: Magic Garden was not found in Chrome.");
            System.err.println("Open this URL in Chrome, load the game; the next cycle will retry:");
            System.err.println("  " + MagicGardenOpener.MAGIC_GARDEN_URL);
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
