package org.example.browser.shop;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.Robot;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import net.sourceforge.tess4j.TesseractException;
import org.example.MagicGardenOpener;

/**
 * Facade for shop list text extraction: CDP DOM lines when remote debugging is enabled, OCR as fallback.
 *
 * <p>Typical flow: call {@link #inspectDefaultPort()} after the shop UI is open; if {@link ShopUiInspection#recommendation()}
 * suggests canvas, use {@link ShopListOcrReader}; otherwise use {@link #readDomLinesDefaultPort()} (container + shadow DOM)
 * or {@link #readAccessibilityNamesDefaultPort()} for AX tree names.</p>
 */
public final class ShopListTextExtractor {

    private ShopListTextExtractor() {
    }

    /** Uses {@link MagicGardenOpener#CHROME_REMOTE_DEBUGGING_PORT}. */
    public static ShopUiInspection inspectDefaultPort() {
        return ShopListCdpReader.inspect(MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT);
    }

    /** Uses {@link MagicGardenOpener#CHROME_REMOTE_DEBUGGING_PORT}. */
    public static List<String> readDomLinesDefaultPort() throws ChromeServiceException {
        return ShopListCdpReader.readDomTextLines(MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT);
    }

    /** Container selectors + shadow fallback; {@link ShopListDomConfig#DEFAULT_SHOP_LIST_SELECTORS} by default. */
    public static List<String> readDomLinesDefaultPort(String[] listContainerSelectors)
            throws ChromeServiceException {
        return ShopListCdpReader.readDomTextLines(MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT, listContainerSelectors);
    }

    /** Uses {@link MagicGardenOpener#CHROME_REMOTE_DEBUGGING_PORT}. */
    public static List<String> readAccessibilityNamesDefaultPort() throws ChromeServiceException {
        return ShopListCdpReader.readAccessibilityNames(MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT);
    }

    public static String readOcrDefaultRegion(Robot robot, User32 user32, WinDef.HWND chromeHwnd)
            throws TesseractException {
        return ShopListOcrReader.readVisibleListDefaultRegion(robot, user32, chromeHwnd);
    }
}
