package org.example.browser.shop;

import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import org.example.MagicGardenOpener;
import org.example.browser.util.ItemsToBuy;
import org.example.input.IdleKeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShopListSelector {
    private static final Logger log = LoggerFactory.getLogger("mg.shop");

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
                    log.info("Row | {} ({}) | {}", item.name(), item.value(), line);
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
                            log.warn("Skip | {} | selectors not configured", item.value());
                        } else if (stockCount <= 0) {
                            log.info("Skip | {} | stock 0 in line text", item.value());
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
                                            log.warn(
                                                    "CDP | {} | row button not clickable after {} tries · purchase {}/{}",
                                                    item.value(),
                                                    ROW_CLICK_RETRIES,
                                                    i + 1,
                                                    stockCount);
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
                                            log.info(
                                                    "Buy | {} · {}/{}",
                                                    item.value(),
                                                    purchasedCount + 1,
                                                    stockCount);
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
                                        log.warn(
                                                "CDP | {} | follow-up not clickable after retries · purchase {}/{}",
                                                item.value(),
                                                i + 1,
                                                stockCount);
                                    }
                                }
                                // Do not keep looping on stale stockCount if this round never completed (UI / stock changed).
                                if (!roundSucceeded) {
                                    log.warn(
                                            "Stop | {} | purchase {}/{} failed (out of stock or UI changed)",
                                            item.value(),
                                            i + 1,
                                            stockCount);
                                    break;
                                }
                                purchasedCount++;
                                // Cooldown between buys; skip after the last one so we do not sleep unnecessarily.
                                if (i < stockCount - 1) {
                                    log.info(
                                            "Wait | {}s · next unit same item (mouse nudges)",
                                            BETWEEN_SHOP_ROWS_MS / 1000);
                                    IdleKeepAlive.sleepWithMouseJiggle(BETWEEN_SHOP_ROWS_MS);
                                }
                            }

                            log.info("Done | {} · purchased {}/{}", item.value(), purchasedCount, stockCount);
                        }
                    } catch (ChromeServiceException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        log.error("CDP | click failed: {}", e.toString(), e);
                    }
                    // Cooldown before the next shop line / next product type (e.g. after strawberry, before aloe).
                    if (lineIndex < shopList.size() - 1) {
                        try {
                            log.info(
                                    "Wait | {}s · next shop row (mouse nudges)",
                                    BETWEEN_SHOP_ROWS_MS / 1000);
                            IdleKeepAlive.sleepWithMouseJiggle(BETWEEN_SHOP_ROWS_MS);
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
