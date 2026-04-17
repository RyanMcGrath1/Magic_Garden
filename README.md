# Magic Garden

Automation helper for the browser game Magic Garden.  
It focuses the game window in Chrome, opens the in-game shop, reads shop rows from the live tab through Chrome DevTools Protocol (CDP), and attempts purchases for items you configure.

<img width="1024" height="576" alt="image" src="https://github.com/user-attachments/assets/16d67c3b-8989-4b65-b066-261e5ed5646c" />


## Requirements

- Windows (the automation flow is Windows-only; macOS is not implemented yet)
- Java 25 (`pom.xml` uses `maven.compiler.release=25`)
- Maven 3.9+
- Google Chrome

## What this project does

1. Checks OS compatibility.
2. Activates the Chrome window that has Magic Garden open (or logs a warning and retries on the next cycle if the window is missing).
3. Sends keyboard input (`Shift+1`, then `Space`) to open the shop.
4. Reads shop rows from the active tab via CDP (`http://127.0.0.1:9222`).
5. Matches rows against `ItemsToBuy` and clicks the configured row / confirm selectors.
6. **Repeats**: after each full pass, waits **5 minutes**, then runs the flow again until you stop the process.
7. During long waits (5-minute gap, 12-second cooldowns between shop rows, and the short “Chrome ready” pause), **nudges the mouse slightly** to reduce idle disconnects (see `IdleKeepAlive`).
8. Writes **structured logs** to the console via SLF4J (`mg.run` for the main loop, `mg.shop` for purchases).

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

**Behavior:**

- The app **runs until you stop it** (**Ctrl+C** in the terminal, or your IDE’s stop button).
- After each shop pass (even if CDP read fails), it waits **5 minutes**, then focuses Chrome and runs another pass.
- If the Magic Garden window is not found, it **does not exit**: it logs a warning and **retries on the next cycle** after the same 5-minute wait.

## Configuration

### Items to buy

Edit `src/main/java/org/example/browser/util/ItemsToBuy.java`:

- Add/remove enum constants for target items.
- `value` is the text used for matching shop lines.
- `rowButtonSelector` and `followUpButtonSelector` are CSS selectors for the row button and the follow-up confirm control.

The repo ships with **many** example entries (seeds and related shop rows). Trim the enum to only the items you want, or adjust selectors when the game UI changes.

### Shop DOM extraction and selector tuning

Edit `src/main/java/org/example/browser/shop/ShopListDomConfig.java`:

- `DEFAULT_SHOP_LIST_SELECTORS` controls where shop list scanning begins.
- `CLOSE_POPUP_SELECTOR` controls popup close targeting.
- `ANCESTOR_DIV_MAX_HOPS` and `MAX_ELEMENTS_PER_KEYWORD_PER_STEP` tune extraction limits.

### Logging

Logging uses **SLF4J** with **slf4j-simple**. Default format and levels are set in `src/main/resources/simplelogger.properties`. To change verbosity per logger, add lines such as:

```properties
org.slf4j.simpleLogger.log.mg.shop=debug
```

### Idle / mouse movement

Long sleeps use `org.example.input.IdleKeepAlive#sleepWithMouseJiggle`: the cursor is moved by a few pixels periodically. This may help with **mouse-idle** timeouts; it does not guarantee the game will never disconnect if it tracks keyboard or other signals.

## Important behavior notes

- The app reads the already-open browser tab via CDP; it does not fetch the game page directly over HTTP.
- Use the Chrome window started by `chrome-with-debugging.bat`; normal Chrome windows may not expose port `9222`.
- A dedicated profile is intentional so Chrome does not attach to an already-running process that ignores debugging flags.

## Main classes

- `org.example.Main` — Entry point; runs until interrupted.
- `org.example.scripts.MagicGardenAutomation` — OS preflight, Chrome focus, shop hotkey, CDP read, **5-minute repeating loop**.
- `org.example.input.IdleKeepAlive` — Optional mouse nudges during long waits.
- `org.example.MagicGardenOpener` — Finds/focuses/sizes the Magic Garden Chrome window.
- `org.example.browser.shop.ShopListCdpReader` — CDP JavaScript evaluation for reading/clicking shop entries.
- `org.example.browser.shop.ShopListSelector` — Item matching and purchase flow (cooldowns between rows/items).
- `org.example.browser.ChromeDebugPreflight` — CDP endpoint reachability diagnostics.

## Troubleshooting

- **`Could not read shop list via CDP`**
  - Verify `http://127.0.0.1:9222/json` responds.
  - Re-run `chrome-with-debugging.bat`.
  - Ensure the game is open in the same debugging Chrome window.

- **Magic Garden tab not found**
  - Open `https://magicgarden.gg/r/QP9B` in Chrome, load the game UI fully. The next **5-minute** cycle will retry if the window was missing.

- **No automation on macOS/Linux**
  - Expected today. Current window-activation flow uses Win32 APIs via JNA.

