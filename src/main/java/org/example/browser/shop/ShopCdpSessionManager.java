package org.example.browser.shop;

import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;
import java.util.List;
import java.util.Locale;
import org.example.browser.ChromeDebugPreflight;

/**
 * Centralized CDP session lifecycle for shop automation.
 */
final class ShopCdpSessionManager {

    private static final Object SHARED_CDP_LOCK = new Object();

    private static ChromeDevToolsService sharedDevToolsSession;

    private static int sharedSessionPort = -1;

    private ShopCdpSessionManager() {
    }

    /**
     * Returns the single shared {@link ChromeDevToolsService} with {@code Runtime} enabled for {@code debuggingPort}.
     * Creates or reconnects only when missing, closed, or the port changes.
     */
    static ChromeDevToolsService getSharedRuntimeEnabledDevToolsSession(int debuggingPort)
            throws ChromeServiceException {
        synchronized (SHARED_CDP_LOCK) {
            if (sharedDevToolsSession != null
                    && !sharedDevToolsSession.isClosed()
                    && sharedSessionPort == debuggingPort) {
                return sharedDevToolsSession;
            }
            closeSharedCdpSessionLocked();
            MagicGardenChromeConnection conn = connectToMagicGardenChrome(debuggingPort);
            sharedDevToolsSession = conn.chromeService.createDevToolsService(conn.tab);
            sharedDevToolsSession.getRuntime().enable();
            sharedSessionPort = debuggingPort;
            return sharedDevToolsSession;
        }
    }

    /**
     * Closes the shared DevTools session so the next call opens a fresh connection (e.g. after Chrome restart).
     */
    static void closeSharedCdpSession() {
        synchronized (SHARED_CDP_LOCK) {
            closeSharedCdpSessionLocked();
        }
    }

    private static void closeSharedCdpSessionLocked() {
        if (sharedDevToolsSession != null) {
            try {
                sharedDevToolsSession.close();
            } catch (Exception ignored) {
                // best-effort shutdown
            }
            sharedDevToolsSession = null;
        }
        sharedSessionPort = -1;
    }

    /**
     * CDP preflight, {@link ChromeServiceImpl} for {@code debuggingPort}, and the Magic Garden {@link ChromeTab}.
     */
    private static MagicGardenChromeConnection connectToMagicGardenChrome(int debuggingPort) {
        String preflightFailure = ChromeDebugPreflight.checkOrExplainFailure(debuggingPort);
        if (preflightFailure != null) {
            throw new IllegalStateException(
                    ChromeDebugPreflight.unreachableMessage(debuggingPort) + preflightFailure);
        }
        ChromeServiceImpl chromeService = new ChromeServiceImpl(debuggingPort);
        List<ChromeTab> tabs = chromeService.getTabs();
        ChromeTab tab = findMagicGardenTab(tabs);
        if (tab == null) {
            throw new IllegalStateException(
                    "No Magic Garden page tab (type page, URL containing 'magicgarden') with a debugger URL. Page tabs: "
                            + summarizePageTabUrls(tabs, 12, 140));
        }
        return new MagicGardenChromeConnection(chromeService, tab);
    }

    private static final class MagicGardenChromeConnection {
        final ChromeServiceImpl chromeService;
        final ChromeTab tab;

        private MagicGardenChromeConnection(ChromeServiceImpl chromeService, ChromeTab tab) {
            this.chromeService = chromeService;
            this.tab = tab;
        }
    }

    /**
     * Top-level page tab whose URL contains {@code magicgarden} (excludes iframe/worker targets and extension pages).
     */
    private static ChromeTab findMagicGardenTab(List<ChromeTab> tabs) {
        for (ChromeTab t : tabs) {
            if (!t.isPageType()) {
                continue;
            }
            String u = t.getUrl();
            if (u == null || t.getWebSocketDebuggerUrl() == null) {
                continue;
            }
            String lower = u.toLowerCase(Locale.ROOT);
            if (!lower.contains("magicgarden")) {
                continue;
            }
            if (lower.startsWith("chrome-extension:")
                    || lower.startsWith("devtools:")
                    || lower.startsWith("chrome-devtools:")) {
                continue;
            }
            return t;
        }
        return null;
    }

    private static String summarizePageTabUrls(List<ChromeTab> tabs, int maxEntries, int maxUrlLen) {
        if (tabs == null || tabs.isEmpty()) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (ChromeTab t : tabs) {
            if (!t.isPageType()) {
                continue;
            }
            if (n >= maxEntries) {
                sb.append(" | …");
                break;
            }
            if (n > 0) {
                sb.append(" | ");
            }
            String u = t.getUrl();
            String show = u == null ? "(null)" : u;
            if (show.length() > maxUrlLen) {
                show = show.substring(0, maxUrlLen) + "…";
            }
            sb.append(show);
            n++;
        }
        if (n == 0) {
            return "(no page-type tabs)";
        }
        return sb.toString();
    }
}
