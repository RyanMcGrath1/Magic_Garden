package org.example.scripts;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.OsInfo;
import org.example.input.IdleKeepAlive;
import org.example.browser.shop.ShopListSelector;
import org.example.browser.shop.ShopListTextExtractor;
import org.example.browser.util.ItemsToBuy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicGardenAutomation {
    private static final Logger log = LoggerFactory.getLogger("mg.run");

    private static final long SHOP_CYCLE_INTERVAL_MS = 5 * 60 * 1000L;
    private static final int CHROME_READY_PAUSE_MS = 5000;
    private static final int AFTER_SHOP_KEY_MS = 1500;

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
            log.info("Preflight | Windows");
            return 1;
        }
        if (OsInfo.isMacOs()) {
            log.info("Preflight | macOS (shop automation not implemented)");
            return 2;
        }
        log.error("Preflight | Unsupported OS: {}", System.getProperty("os.name"));
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
            log.info(
                    "Setup | Remote debugging port {} · open the game in that Chrome, tab active · {}",
                    MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                    MagicGardenOpener.MAGIC_GARDEN_URL);
            log.info(
                    "Setup | Repeating shop pass every {} min after each pass · Ctrl+C to stop",
                    SHOP_CYCLE_INTERVAL_MS / 60_000L);
            try {
                int cycle = 1;
                while (true) {
                    runWindowsShopCycle(itemsToBuy, cycle);
                    cycle++;
                    log.info("Sleep | Next shop pass in {} min (mouse nudges while waiting)", SHOP_CYCLE_INTERVAL_MS / 60_000L);
                    IdleKeepAlive.sleepWithMouseJiggle(SHOP_CYCLE_INTERVAL_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Stopped | Interrupted");
            }
        } else if (os == 2) {
            // TODO: Add macOS support
        }
    }

    private void runWindowsShopCycle(ItemsToBuy[] itemsToBuy, int cycleNumber) throws InterruptedException {
        log.info("Cycle {} | --- start ---", cycleNumber);
        if (!magicGardenOpener.bringChromeToFront()) {
            log.warn(
                    "Cycle {} | Magic Garden window not found — will retry next cycle · {}",
                    cycleNumber,
                    MagicGardenOpener.MAGIC_GARDEN_URL);
            return;
        }
        log.info("Cycle {} | Chrome ready · waiting {}s (mouse nudges)", cycleNumber, CHROME_READY_PAUSE_MS / 1000);
        IdleKeepAlive.sleepWithMouseJiggle(CHROME_READY_PAUSE_MS);
        clickShopButton(cycleNumber);
        Thread.sleep(AFTER_SHOP_KEY_MS);
        try {
            List<String> shopLines = ShopListTextExtractor.readScrollableListDefaultPort();
            log.info("Cycle {} | CDP shop lines: {} — purchasing", cycleNumber, shopLines.size());
            ShopListSelector.beginInterfacingWithGame(shopLines, itemsToBuy);

        } catch (Exception ex) {
            log.error("Cycle {} | CDP shop list read failed", cycleNumber, ex);
        }
    }

    private void clickShopButton(int cycleNumber) {
        log.info("Cycle {} | Input | Shop hotkey Shift+1, then Space", cycleNumber);
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        pressAndRelease(KeyEvent.VK_SPACE);
    }

    private void pressAndRelease(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }
}
