package org.example.browser.shop;

/**
 * CSS selectors to locate the shop list container, tried in order before falling back to
 * {@code document.body} plus shadow-root text. Calibrate in DevTools (Elements) for Magic Garden.
 */
public final class ShopListDomConfig {

    /**
     * Broad heuristics; replace or extend after inspecting the real DOM when the shop panel is open.
     */
    public static final String[] DEFAULT_SHOP_LIST_SELECTORS = new String[] {
        "[role=\"dialog\"]",
        "[class*=\"shop\"]",
        "[class*=\"Shop\"]",
        "[class*=\"store\"]",
        "[data-testid*=\"shop\"]",
        "aside",
        "main",
    };

    private ShopListDomConfig() {
    }
}
