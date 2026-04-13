package org.example.browser.shop;

import java.util.List;

import org.example.browser.util.ItemsToBuy;

public final class ShopListSelector {

    private ShopListSelector() {
    }

    /**
     * For each shop line, checks whether it contains any of the {@link ItemsToBuy} labels (substring match).
     */
    public static void beginInterfacingWithGame(List<String> shopList, ItemsToBuy[] itemsToBuy) {
        if (shopList == null || itemsToBuy == null) {
            return;
        }

        
        for (String line : shopList) {
            for (ItemsToBuy item : itemsToBuy) {
                if (item.containedIn(line) && !line.contains("NO STOCK")) {
                    System.out.println("Matched " + item.name() + " (" + item.value() + "): " + line);
                    clickButton(line);
                }
            }
        }
    }

    private static void clickButton(String buttonText) {
        // ShopListCdpReader.clickButton(buttonText);
    }
}
