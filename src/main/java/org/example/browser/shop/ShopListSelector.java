package org.example.browser.shop;

import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.browser.util.ItemsToBuy;

public final class ShopListSelector {

    private ShopListSelector() {
    }

    /**
     * For each shop line, if it matches an {@link ItemsToBuy} and is in stock, clicks that row's button via the shared
     * CDP session (same connection as list reads — no new socket per click).
     */
    public static void beginInterfacingWithGame(List<String> shopList, ItemsToBuy[] itemsToBuy) {
        if (shopList == null || itemsToBuy == null) {
            return;
        }

        for (String line : shopList) {
            for (ItemsToBuy item : itemsToBuy) {
                if (item.containedIn(line) && !line.contains("NO STOCK")) {
                    System.out.println("Matched " + item.name() + " (" + item.value() + "): " + line);
                    try {
                        boolean clicked =
                                ShopListCdpReader.clickMatchingShopButtonSharedSession(
                                        MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                        ShopListDomConfig.DEFAULT_SHOP_LIST_SELECTORS,
                                        line,
                                        item.value());
                        if (clicked) {
                            System.out.println("CDP: clicked button for " + item.value());
                        } else {
                            System.out.println("CDP: no visible button matched for " + item.value());
                        }
                    } catch (ChromeServiceException e) {
                        System.err.println("CDP click failed: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
}
