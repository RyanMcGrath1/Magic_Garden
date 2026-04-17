package org.example.browser.shop;

import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.browser.util.ItemsToBuy;

public final class ShopListSelector {
    private static final int ROW_CLICK_RETRIES = 5;
    private static final int FOLLOW_UP_CLICK_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 150L;
    /** Pause after finishing one shop row before moving to the next line (e.g. strawberry row → aloe row). */
    private static final long BETWEEN_SHOP_ROWS_MS = 12_000L;

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

        // Walk every extracted shop line; for each, see if it is a row we want to buy from.
        for (int lineIndex = 0; lineIndex < shopList.size(); lineIndex++) {
            String line = shopList.get(lineIndex);
            for (ItemsToBuy item : itemsToBuy) {
                // Text match + skip lines explicitly marked unavailable in the string.
                if (item.containedIn(line) && !line.contains("NO STOCK")) {
                    System.out.println("Matched " + item.name() + " (" + item.value() + "): " + line);
                    // Parse "… x N" style stock from the line text (same snapshot as the list read).
                    int stockCount = ShopListCdpReader.fetchStockCount(line);
                    try {
                        String rowButtonSelector = item.rowButtonSelector();
                        String followUpButtonSelector = item.followUpButtonSelector();
                        // Both selectors are required: row opens the buy flow, follow-up confirms it.
                        if (rowButtonSelector == null
                                || rowButtonSelector.isBlank()
                                || followUpButtonSelector == null
                                || followUpButtonSelector.isBlank()) {
                            System.out.println("CDP: selectors not configured for " + item.value() + "; skipping.");
                        } else if (stockCount <= 0) {
                            System.out.println("CDP: stockCount is 0; skipping purchases for " + item.value());
                        } else {
                        int purchasedCount = 0;
                        // Aim for up to stockCount purchases; stop early if a round cannot complete (e.g. out of stock).
                        for (int i = 0; i < stockCount; i++) {
                            boolean roundSucceeded = false;
                            // Retry the shop row click until it succeeds or we give up (DOM timing / focus).
                            for (int r = 0; r < ROW_CLICK_RETRIES && !roundSucceeded; r++) {
                                boolean clicked =
                                        ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                rowButtonSelector);
                                if (!clicked) {
                                    if (r == ROW_CLICK_RETRIES - 1) {
                                        System.out.println(
                                                "CDP: row button not found/clickable after "
                                                        + ROW_CLICK_RETRIES
                                                        + " tries for purchase "
                                                        + (i + 1)
                                                        + " of "
                                                        + stockCount
                                                        + " ("
                                                        + item.value()
                                                        + ")");
                                    }
                                    continue;
                                }
                                // After the row opens, the game shows a confirm (or similar); retry until it clicks.
                                for (int r1 = 0; r1 < FOLLOW_UP_CLICK_RETRIES; r1++) {
                                    boolean followUpClicked =
                                            ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                    MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                    followUpButtonSelector);
                                    if (followUpClicked) {
                                        // One successful row + follow-up = one purchase; close any popup, then exit retries.
                                        System.out.println(
                                                "Purchased "
                                                        + item.value()
                                                        + " ("
                                                        + (purchasedCount + 1)
                                                        + "/"
                                                        + stockCount
                                                        + ")");
                                        Thread.sleep(300);
                                        ShopListCdpReader.clickButtonBySelectorSharedSession(
                                                MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT,
                                                ShopListDomConfig.CLOSE_POPUP_SELECTOR);
                                        roundSucceeded = true;
                                        break;
                                    }
                                    // Brief pause between follow-up attempts so the UI can catch up.
                                    Thread.sleep(RETRY_DELAY_MS);
                                }
                                // Only log follow-up exhaustion on the last row retry to avoid spam.
                                if (!roundSucceeded && r == ROW_CLICK_RETRIES - 1) {
                                    System.out.println(
                                            "CDP: follow-up button not found/clickable after retries for purchase "
                                                    + (i + 1)
                                                    + " of "
                                                    + stockCount
                                                    + " ("
                                                    + item.value()
                                                    + ")");
                                }
                            }
                            // Do not keep looping on stale stockCount if this round never completed (UI / stock changed).
                            if (!roundSucceeded) {
                                System.out.println(
                                        "CDP: stopping — could not complete purchase "
                                                + (i + 1)
                                                + "/"
                                                + stockCount
                                                + " for "
                                                + item.value()
                                                + " (likely out of stock or UI changed).");
                                break;
                            }
                            purchasedCount++;
                            // Cooldown between buys; skip after the last one so we do not sleep unnecessarily.
                            if (i < stockCount - 1) {
                                System.out.println(
                                        "Waiting "
                                                + (BETWEEN_SHOP_ROWS_MS / 1000)
                                                + " seconds before next purchase (same item)");
                                Thread.sleep(BETWEEN_SHOP_ROWS_MS);
                            }
                        }

                        System.out.println(
                                "CDP: purchased "
                                        + purchasedCount
                                        + "/"
                                        + stockCount
                                        + " for "
                                        + item.value());
                        }
                    } catch (ChromeServiceException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        System.err.println("CDP click failed: " + e.getMessage());
                    }
                    // Cooldown before the next shop line / next product type (e.g. after strawberry, before aloe).
                    if (lineIndex < shopList.size() - 1) {
                        try {
                            System.out.println(
                                    "Waiting "
                                            + (BETWEEN_SHOP_ROWS_MS / 1000)
                                            + " seconds before next shop row / item...");
                            Thread.sleep(BETWEEN_SHOP_ROWS_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    // First matching line wins for this item; remaining lines for the same item are not processed twice here.
                    break;
                }
            }
        }
    }
}
