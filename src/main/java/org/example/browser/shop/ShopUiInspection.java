package org.example.browser.shop;

/**
 * Result of inspecting the Magic Garden page over CDP to decide whether DOM text extraction is viable
 * vs. canvas rendering (OCR).
 */
public record ShopUiInspection(
        boolean cdpAvailable,
        String cdpFailureMessage,
        int canvasElementCount,
        int bodyTextLength,
        String bodyTextSample,
        Recommendation recommendation) {

    public enum Recommendation {
        /** Prefer {@link ShopListCdpReader#readDomTextLines(int)} / innerText-style reads. */
        DOM_TEXT_LIKELY,
        /** Prefer {@link ShopListOcrReader} or network inspection; little selectable DOM text. */
        CANVAS_LIKELY,
        /** Chrome not reachable on the debugging port or Magic Garden tab not found. */
        CDP_UNAVAILABLE,
        /** Heuristic unclear; try CDP lines first, then OCR if needed. */
        UNCLEAR
    }
}
