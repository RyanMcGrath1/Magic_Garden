package org.example.browser.shop;

/**
 * Reference client rectangle for the visible shop list area (same 1280×720 logical space as
 * {@link org.example.browser.ChromeViewportMapper}). Used with {@link ShopListOcrReader}; tune if OCR misses.
 */
public final class ShopListViewport {

    public static final double LIST_REF_LEFT = 320;
    public static final double LIST_REF_TOP = 140;
    public static final double LIST_REF_WIDTH = 640;
    public static final double LIST_REF_HEIGHT = 480;

    private ShopListViewport() {
    }
}
