package org.example.browser.util;

/**
 * Egg shop item labels to buy — each constant is the substring/text that appears on the page or in extracted shop lines.
 * Add one enum constant per item; match the in-game button or row text. Selectors must match the egg shop DOM (often different from {@link SeedItemsToBuy}).
 */
public enum EggItemsToBuy implements ShopItemToBuy {

    /*
    This enum is used to determine which egg shop items will actually be bought.
    The purpose of this enum is to make it easy to add new eggs to the list of items to buy.
    Fill in rowButtonSelector and followUpButtonSelector from Chrome DevTools for the egg shop view.
    */
    COMMON("Common Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div.css-1wufriz > div > div > div > button"),
    UNCOMMON("Uncommon Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div.css-1wufriz > div > div > div > button"),
    RARE("Rare Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div.css-1wufriz > div > div > div > button"),
    LEGENDARY("Legendary Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div.css-1wufriz > div > div > div > button"),
    SNOWBALL("Snow Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(5) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(5) > div.css-1wufriz > div > div > div > button"),
    MYTHICAL("Mythical Egg", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(6) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(6) > div.css-1wufriz > div > div > div > button"),
    ;

    private final String value;
    private final String rowButtonSelector;
    private final String followUpButtonSelector;

    EggItemsToBuy(String value, String rowButtonSelector, String followUpButtonSelector) {
        this.value = value;
        this.rowButtonSelector = rowButtonSelector;
        this.followUpButtonSelector = followUpButtonSelector;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String rowButtonSelector() {
        return rowButtonSelector;
    }

    @Override
    public String followUpButtonSelector() {
        return followUpButtonSelector;
    }

    @Override
    public String itemId() {
        return name();
    }

    /** True if {@code line} contains this item's label. */
    @Override
    public boolean containedIn(String line) {
        return line != null && line.contains(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
