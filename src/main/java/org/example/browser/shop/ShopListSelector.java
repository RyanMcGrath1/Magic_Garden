package org.example.browser.shop;

import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.browser.util.ItemsToBuy;

public final class ShopListSelector {
    private static final int ROW_CLICK_RETRIES = 5;
    private static final int FOLLOW_UP_CLICK_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 150L;

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
                    int stockCount = ShopListCdpReader.fetchStockCount(line);
                    try {
                        String rowButtonSelector = item.rowButtonSelector();
                        String followUpButtonSelector = item.followUpButtonSelector();
                        if (rowButtonSelector == null
                                || rowButtonSelector.isBlank()
                                || followUpButtonSelector == null
                                || followUpButtonSelector.isBlank()) {
                            System.out.println("CDP: selectors not configured for " + item.value() + "; skipping.");
                            break;
                        }
                        if (stockCount <= 0) {
                            System.out.println("CDP: stockCount is 0; skipping purchases for " + item.value());
                            break;
                        }

                        int purchasedCount = 0;
                        for (int i = 0; i < stockCount; i++) {
                            boolean clicked = false;
                            for (int r = 0; r < ROW_CLICK_RETRIES; r++) {
                                clicked =
                                        ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                rowButtonSelector);
                                if (clicked) {
                                    boolean followUpClicked = false;
                                    for (int r1 = 0; r1 < FOLLOW_UP_CLICK_RETRIES; r1++) {
                                        followUpClicked =
                                                ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                        MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                        followUpButtonSelector);
                                        if (followUpClicked) {
                                            System.out.println("Purhased " + item.value() + " " + (i + 1) + " times");
                                            Thread.sleep(300);
                                            ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                    MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                    ShopListDomConfig.CLOSE_POPUP_SELECTOR);
                                            continue;
                                        }
                                        if (!followUpClicked) {
                                            System.out.println(
                                                    "CDP: follow-up button not found/clickable on attempt "
                                                            + (i + 1)
                                                            + " for "
                                                            + item.value());
                                            break;
                                        }
                                        Thread.sleep(RETRY_DELAY_MS);
                                    }
                                }
                                if (!clicked) {
                                    System.out.println(
                                            "CDP: row button not found/clickable on attempt "
                                                    + (i + 1)
                                                    + " for "
                                                    + item.value());
                                    break;
                                }
                                
                            } 
                            purchasedCount++;
                            System.out.println("Waiting 12 seconds before next purchase");
                            Thread.sleep(12000);
                        }

                        System.out.println(
                                "CDP: purchased "
                                        + purchasedCount
                                        + "/"
                                        + stockCount
                                        + " for "
                                        + item.value());
                    } catch (ChromeServiceException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        System.err.println("CDP click failed: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
}
