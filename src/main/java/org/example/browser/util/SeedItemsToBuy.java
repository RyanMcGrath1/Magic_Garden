package org.example.browser.util;

/**
 * Shop item labels to buy — each constant is the substring/text that appears on the page or in extracted shop lines.
 * Add one enum constant per item; match the in-game button or row text.
 */
public enum SeedItemsToBuy implements ShopItemToBuy {

    /* 
    This enum is used to determine which items will actualy be bought.
    The purpose of this enum is to make it easy to add new items to the list of items to buy.
    */
    // CARROT("Carrot Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(1) > div.css-1wufriz > div > div > div > button"),
    // CABBAGE("Cabbage Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(2) > div.css-1wufriz > div > div > div > button"),
    // STRAWBERRY("Strawberry Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(3) > div.css-1wufriz > div > div > div > button"),
    // ALOE("Aloe Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(4) > div.css-1wufriz > div > div > div > button"),
    // BEET("Beet Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(5) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(5) > div.css-1wufriz > div > div > div > button"),
    // FAVA("Fava Bean","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(6) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(6) > div.css-1wufriz > div > div > div > button"),
    // BLUEBERRY("Blueberry Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(7) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(7) > div.css-1wufriz > div > div > div > button"),
    // APPLE("Apple Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(8) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(8) > div.css-1wufriz > div > div > div > button"),
    // TULIP("Tulip Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(9) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(9) > div.css-1wufriz > div > div > div > button"),
    // TOMATO("Tomato Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(10) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(10) > div.css-1wufriz > div > div > div > button"),
    // DAFFODIL("Daffodil Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(11) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(11) > div.css-1wufriz > div > div > div > button"),
    // CORN("Corn Kernel","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(12) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(12) > div.css-1wufriz > div > div > div > button"),
    // WATERMELON("Watermelon Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(13) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(13) > div.css-1wufriz > div > div > div > button"),
    // PUMPKIN("Pumpkin Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(14) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(14) > div.css-1wufriz > div > div > div > button"),
    // ECHEVERIA("Echeveria Cutting","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(15) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(15) > div.css-1wufriz > div > div > div > button"),
    // PEAR("Pear Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(16) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(16) > div.css-1wufriz > div > div > div > button"),
    // GENTIAN("Gentian Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(17) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(17) > div.css-1wufriz > div > div > div > button"),
    // COCONUT("Coconut Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(18) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(18) > div.css-1wufriz > div > div > div > button"),
    // BANANA("Banana Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(19) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(19) > div.css-1wufriz > div > div > div > button"),
    // LILY("Lily Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(20) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(20) > div.css-1wufriz > div > div > div > button"),
    // CAMELLIA("Camellia Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(21) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(21) > div.css-1wufriz > div > div > div > button"),
    // PEACH("Peach Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(22) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(22) > div.css-1wufriz > div > div > div > button"),
    // BURROS("Burro's Tail Cutting","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(23) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(23) > div.css-1wufriz > div > div > div > button"),
    // MUSHROOM("Mushroom Spore","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(24) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(24) > div.css-1wufriz > div > div > div > button"),
    // CACTUS("Cactus Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(25) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(25) > div.css-1wufriz > div > div > div > button"),
    // BAMBOO("Bamboo Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(26) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(26) > div.css-1wufriz > div > div > div > button"),
    // VIOLET("Violet Court Spore","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(27) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(27) > div.css-1wufriz > div > div > div > button"),
    // CHRYSANTHEMUM("Chrysanthemum Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(28) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(28) > div.css-1wufriz > div > div > div > button"),
    // GRAPE("Grape Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(29) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(29) > div.css-1wufriz > div > div > div > button"),
    // PEPPER("Pepper Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(30) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(30) > div.css-1wufriz > div > div > div > button"),
    // LEMON("Lemon Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(31) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(31) > div.css-1wufriz > div > div > div > button"),
    // PASSION_FRUIT("Passion Fruit Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(32) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(32) > div.css-1wufriz > div > div > div > button"),
    // DRAGON_FRUIT("Dragon Fruit Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(33) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(33) > div.css-1wufriz > div > div > div > button"),
    // CACAO("Cacao Bean","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(34) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(34) > div.css-1wufriz > div > div > div > button"),
    // LYCHEE("Lychee Pit","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(35) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(35) > div.css-1wufriz > div > div > div > button"),
    // SUNFLOWER("Sunflower Seed","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(36) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(36) > div.css-1wufriz > div > div > div > button"),
    STARWEAVER("Starweaver Pod","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(37) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(37) > div.css-1wufriz > div > div > div > button"),
    // DAWNBINDER("Dawnbinder Pod","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(38) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(38) > div.css-1wufriz > div > div > div > button"),
    // MOONBINDER("Moonbinder Pod","#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(39) > div.css-79z73n > button", "#App > div > div.GameScreen.css-u59uz2 > div:nth-child(2) > div > div > div.css-brvep9 > div > div:nth-child(39) > div.css-1wufriz > div > div > div > button"),
    ;

    private final String value;
    private final String rowButtonSelector;
    private final String followUpButtonSelector;

    SeedItemsToBuy(String value, String rowButtonSelector, String followUpButtonSelector) {
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
