package org.example.browser.util;

/**
 * Shared contract for shop rows the automation can match and purchase (e.g. {@link SeedItemsToBuy}, {@link EggItemsToBuy}).
 */
public interface ShopItemToBuy {

    /** In-game label substring used to match extracted shop lines. */
    String value();

    String rowButtonSelector();

    String followUpButtonSelector();

    /** True if {@code line} contains this item's label. */
    boolean containedIn(String line);

    /** Stable id for logs (typically the enum constant name). */
    String itemId();
}
