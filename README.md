# Magic Garden

Automation helper for the browser game Magic Garden.  
The app focuses the game window, opens the in-game shop, reads shop entries from the live tab through Chrome DevTools Protocol (CDP), and attempts purchases for configured items.

## Requirements

- Windows (current automation flow is Windows-only; macOS is not implemented yet)
- Java 25 (`pom.xml` uses `maven.compiler.release=25`)
- Maven 3.9+
- Google Chrome

## What this project does

1. Checks OS compatibility.
2. Activates the Chrome window that has Magic Garden open.
3. Sends keyboard input (`Shift+1`, then `Space`) to open the shop.
4. Reads shop rows from the active tab via CDP (`http://127.0.0.1:9222`).
5. Matches rows against `ItemsToBuy` and clicks configured buttons/selectors.

## Quick Start

### 1) Launch Chrome in CDP mode

Run:

```bat
chrome-with-debugging.bat
```

This script starts a dedicated Chrome profile with:

- `--remote-debugging-port=9222`
- `--remote-debugging-address=127.0.0.1`
- `--remote-allow-origins=*`
- `--user-data-dir=.chrome-debug-profile`

Then open the game in that window:

- `https://magicgarden.gg/r/QP9B`

Optional check:

- Open `http://127.0.0.1:9222/json`
- Confirm a `page` target exists whose URL includes `magicgarden`

### 2) Run the automation

From the project root:

```bash
mvn clean compile exec:java -Dexec.mainClass=org.example.Main
```

If your Maven setup does not have the exec plugin available, run from your IDE using `org.example.Main`.

## Configuration

### Items to buy

Edit `src/main/java/org/example/browser/util/ItemsToBuy.java`:

- Add/remove enum constants for target items.
- `value` is the text used for matching shop lines.
- `rowButtonSelector` and `followUpButtonSelector` are used for click automation.

Current defaults include:

- `CARROT` (`Carrot Seed`)
- `CABBAGE` (`Cabbage Seed`)

### Shop DOM extraction and selector tuning

Edit `src/main/java/org/example/browser/shop/ShopListDomConfig.java`:

- `DEFAULT_SHOP_LIST_SELECTORS` controls where shop list scanning begins.
- `CLOSE_POPUP_SELECTOR` controls popup close targeting.
- `ANCESTOR_DIV_MAX_HOPS` and `MAX_ELEMENTS_PER_KEYWORD_PER_STEP` tune extraction limits.

## Important behavior notes

- The app reads the already-open browser tab via CDP; it does not fetch the game page directly over HTTP.
- Use the Chrome window started by `chrome-with-debugging.bat`; normal Chrome windows may not expose port `9222`.
- A dedicated profile is intentional so Chrome does not attach to an already-running process that ignores debugging flags.

## Main classes

- `org.example.Main` - Entry point.
- `org.example.scripts.Scripts` - High-level runtime flow and keyboard automation.
- `org.example.MagicGardenOpener` - Finds/focuses/sizes the Magic Garden Chrome window.
- `org.example.browser.shop.ShopListCdpReader` - CDP JavaScript evaluation for reading/clicking shop entries.
- `org.example.browser.shop.ShopListSelector` - Item matching and interaction flow.
- `org.example.browser.ChromeDebugPreflight` - CDP endpoint reachability diagnostics.

## Troubleshooting

- **`Could not read shop list via CDP`**
  - Verify `http://127.0.0.1:9222/json` responds.
  - Re-run `chrome-with-debugging.bat`.
  - Ensure the game is open in the same debugging Chrome window.

- **Magic Garden tab not found**
  - Open `https://magicgarden.gg/r/QP9B` in Chrome, load the game UI fully, then run again.

- **No automation on macOS/Linux**
  - Expected today. Current window-activation flow uses Win32 APIs via JNA.
