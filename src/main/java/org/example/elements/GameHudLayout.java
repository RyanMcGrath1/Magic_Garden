package org.example.elements;

import org.example.browser.ChromeViewportMapper;

/**
 * Reference coordinates for HUD controls in the Chrome <em>client</em> area, in the same logical
 * space as {@link ChromeViewportMapper#REFERENCE_CLIENT_WIDTH}×{@link ChromeViewportMapper#REFERENCE_CLIENT_HEIGHT}
 * (1280×720). Used with {@link org.example.scripts.Scripts#clickReferenceLayoutInChromeClient}.
 *
 * <p>Initial SHOP values scale the legacy {@link ScreenPoint#SHOP_BUTTON} (766, 159) from a 1920×1080
 * layout to 1280×720; adjust if the click misses after window chrome / in-game UI changes.</p>
 */
public final class GameHudLayout {

    /** Approximate center of the SHOP control; calibrate in-game if needed. */
    public static final double SHOP_BUTTON_REF_X = 766.0 * ChromeViewportMapper.REFERENCE_CLIENT_WIDTH / 1920.0;

    public static final double SHOP_BUTTON_REF_Y = 159.0 * ChromeViewportMapper.REFERENCE_CLIENT_HEIGHT / 1080.0;

    private GameHudLayout() {
    }
}
