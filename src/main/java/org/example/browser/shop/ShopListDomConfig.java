package org.example.browser.shop;

import java.util.Arrays;

/**
 * Shop list extraction via CDP: injected JavaScript in the live Magic Garden tab. When {@link #SHOP_KEYWORDS} has at
 * least one non-blank entry, the reader collects ancestor {@code div}/{@code section} {@code outerHTML} snippets around
 * text matches; otherwise it scrolls a CSS anchor and collects visible {@code <button>} element labels only.
 */
public final class ShopListDomConfig {

    /**
     * CSS selectors for the shop list region (order matters). The CDP script uses {@code document.querySelector} on the
     * first match, then finds a scrollable ancestor and collects visible {@code button} text inside it. First entry is a
     * DevTools path to a representative shop row button; hashed {@code css-*} classes may change when the game
     * rebuilds.
     */
    public static final String[] DEFAULT_SHOP_LIST_SELECTORS = {
        "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(3) > div > div > div.css-brvep9 > div > "
                + "div:nth-child(1) > div > button"
    };

    /**
     * Substrings to search for in the live page text. When non-empty, keyword/snippet mode is used (injected JS).
     */
    public static final String[] SHOP_KEYWORDS = {""};

    /**
     * When walking up from a node that contains a keyword, stop at the first {@code div} or {@code section} within this
     * many parent hops; that element's {@code outerHTML} is captured.
     */
    public static final int ANCESTOR_DIV_MAX_HOPS = 12;

    /**
     * Max elements per keyword per scroll step (injected JS caps matches).
     */
    public static final int MAX_ELEMENTS_PER_KEYWORD_PER_STEP = 24;

    private ShopListDomConfig() {
    }

    public static boolean hasShopKeywords() {
        return Arrays.stream(SHOP_KEYWORDS)
                .filter(s -> s != null)
                .map(String::trim)
                .anyMatch(s -> !s.isEmpty());
    }
}
