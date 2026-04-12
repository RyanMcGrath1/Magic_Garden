# Magic Garden

## Chrome remote debugging (required for shop list)

The app reads the live page via **Chrome DevTools Protocol (CDP)** on port **9222**. Java connects to the **same** Chrome tab that has the game loaded—it does not re-fetch the URL over HTTP.

### Launch Chrome with debugging

1. Run **`chrome-with-debugging.bat`** from this project directory (uses a dedicated profile so a new Chrome process actually listens on **9222**).
2. In **that** Chrome window, open the game:
   - **`https://magicgarden.gg/r/QP9B`**  
   (same URL as `MagicGardenOpener.MAGIC_GARDEN_URL` in code.)
3. Optional: verify **`http://127.0.0.1:9222/json`** — you should see a **`page`** target with `"url"` containing **`magicgarden`**.
4. Run the Java app.

### Why a separate profile

If Chrome is already running, a second `chrome.exe` launch often **joins** that process and **ignores** `--remote-debugging-port`, so nothing listens on **9222**. The batch file passes `--user-data-dir` so Windows starts a **dedicated** Chrome instance. You can keep normal Chrome open; use the window opened by the batch file for Magic Garden.

### Chrome 111+ WebSocket 403

The batch file adds **`--remote-allow-origins=*`** so the Java CDP client can open the WebSocket. If you start Chrome manually, include that flag too.

## Shop list modes (`ShopListDomConfig`)

- **Keywords non-empty:** Injected JavaScript scrolls the document, finds elements whose text contains each keyword, walks up to a **`div`** / **`section`**, and returns **`outerHTML`** snippets as JSON (parsed in Java with Jackson).
- **Keywords empty:** Scrolls a list container chosen from **`DEFAULT_SHOP_LIST_SELECTORS`** and collects visible **button** / **link** text (JSON from the page).

Tune **`SHOP_KEYWORDS`**, **`ANCESTOR_DIV_MAX_HOPS`**, and **`MAX_ELEMENTS_PER_KEYWORD_PER_STEP`** in `ShopListDomConfig` as needed.

## Troubleshooting

- **`Could not read shop list via CDP`:** Confirm **`http://127.0.0.1:9222/json`** loads and lists a **page** with a **magicgarden** URL. Re-run the batch file if the port is closed.
- **No Magic Garden tab:** Open **`MAGIC_GARDEN_URL`** only in the debugging Chrome window.
