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
    CARROT("Carrot Seed"),

    ;

    private final String value;

    ItemsToBuy(String value) {
        this.value = value;
    }

    public String value() {
        return value;
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
