package org.example.browser.shop;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.browser.ChromeViewportMapper;

/**
 * OCR fallback for shop list text when the game draws the list on canvas or DOM extraction is unreliable.
 * Requires a Tesseract installation discoverable by Tess4J on the host machine.
 */
public final class ShopListOcrReader {

    private ShopListOcrReader() {
    }

    /**
     * Captures the given reference-client rectangle (see {@link ShopListViewport}) and runs OCR on it.
     */
    public static String readVisibleList(
            Robot robot,
            User32 user32,
            WinDef.HWND chromeHwnd,
            double refLeft,
            double refTop,
            double refWidth,
            double refHeight)
            throws TesseractException {
        Rectangle screen =
                ChromeViewportMapper.screenRectangleFromReferenceLayout(
                        user32, chromeHwnd, refLeft, refTop, refWidth, refHeight);
        BufferedImage img = robot.createScreenCapture(screen);
        ITesseract tesseract = new Tesseract();
        return tesseract.doOCR(img);
    }

    /**
     * Uses default {@link ShopListViewport} bounds.
     */
    public static String readVisibleListDefaultRegion(Robot robot, User32 user32, WinDef.HWND chromeHwnd)
            throws TesseractException {
        return readVisibleList(
                robot,
                user32,
                chromeHwnd,
                ShopListViewport.LIST_REF_LEFT,
                ShopListViewport.LIST_REF_TOP,
                ShopListViewport.LIST_REF_WIDTH,
                ShopListViewport.LIST_REF_HEIGHT);
    }

    /**
     * Moves the cursor to the center of the default list region and scrolls the mouse wheel (negative = down).
     * Pair with repeated {@link #readVisibleListDefaultRegion} calls to OCR a long list step by step.
     */
    public static void scrollListRegion(Robot robot, User32 user32, WinDef.HWND chromeHwnd, int wheelNotches) {
        Rectangle screen =
                ChromeViewportMapper.screenRectangleFromReferenceLayout(
                        user32,
                        chromeHwnd,
                        ShopListViewport.LIST_REF_LEFT,
                        ShopListViewport.LIST_REF_TOP,
                        ShopListViewport.LIST_REF_WIDTH,
                        ShopListViewport.LIST_REF_HEIGHT);
        robot.mouseMove(screen.x + screen.width / 2, screen.y + screen.height / 2);
        robot.mouseWheel(wheelNotches);
    }
}
