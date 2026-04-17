package org.example.browser.util;

/**
 * Shop item labels to buy — each constant is the substring/text that appears on the page or in extracted shop lines.
 * Add one enum constant per item; match the in-game button or row text.
 */
public enum ItemsToBuy {

    /* 
    This enum is used to determine which items will actualy be bought.
    The purpose of this enum is to make it easy to add new items to the list of items to buy.
    */
    // CARROT("Carrot Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div.css-1wufriz > div > div > div > button"),
    CABBAGE("Cabbage Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div.css-1wufriz > div > div > div > button"),
    // STRAWBERRY("Strawberry Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div.css-1wufriz > div > div > div > button"),
    ALOE("Aloe Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div.css-1wufriz > div > div > div > button"),
    // SUNFLOWER("Sunflower Seed","h","h"),
    // STARWEAVER("Starweaver Pod"),
    // DAWNBINDER("Dawnbinder Pod"),
    // MOONBINDER("Moonbinder Pod")
    ;

    private final String value;
    private final String rowButtonSelector;
    private final String followUpButtonSelector;

    ItemsToBuy(String value, String rowButtonSelector, String followUpButtonSelector) {
        this.value = value;
        this.rowButtonSelector = rowButtonSelector;
        this.followUpButtonSelector = followUpButtonSelector;
    }

    public String value() {
        return value;
    }

    public String rowButtonSelector() {
        return rowButtonSelector;
    }

    public String followUpButtonSelector() {
        return followUpButtonSelector;
    }

    /** True if {@code line} contains this item's label. */
    public boolean containedIn(String line) {
        return line != null && line.contains(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
