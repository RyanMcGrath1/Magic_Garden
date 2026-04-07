package org.example.browser.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kklisura.cdt.protocol.types.accessibility.AXNode;
import com.github.kklisura.cdt.protocol.types.accessibility.AXValue;
import com.github.kklisura.cdt.protocol.types.runtime.Evaluate;
import com.github.kklisura.cdt.protocol.types.runtime.RemoteObject;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.example.MagicGardenOpener;

/**
 * Reads shop-related text from the Magic Garden tab using Chrome DevTools Protocol. Requires Chrome
 * started with {@code --remote-debugging-port=}&lt;port&gt; (see {@link MagicGardenOpener#CHROME_REMOTE_DEBUGGING_PORT}).
 */
public final class ShopListCdpReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String INSPECT_SCRIPT =
            "(function(){var c=document.querySelectorAll('canvas').length;var b=document.body?document.body.innerText:'';"
                    + "return JSON.stringify({canvasCount:c,bodyTextLength:b.length,bodyTextSample:b.substring(0,Math.min(1500,b.length))});})()";

    private static final String DOM_LINES_SCRIPT =
            "(function(){var t=document.body?document.body.innerText:'';return JSON.stringify(t.split(/\\r?\\n/).map(function(s){return s.trim();}).filter(function(s){return s.length>0;}));})()";

    private static final String LIST_FROM_CONTAINERS_AND_SHADOW_SCRIPT_PREFIX = "(function(){var selectors=";

    private static final String LIST_FROM_CONTAINERS_AND_SHADOW_SCRIPT_SUFFIX =
            ";function trimLines(t){return t.split(/\\r?\\n/).map(function(s){return s.trim();}).filter(function(s){return s.length>0;});}"
                    + "function walkShadow(el,d){if(d>12)return'';var o='';if(!el)return o;if(el.shadowRoot){var r=el.shadowRoot;"
                    + "o+=(r.innerText||r.textContent||'');var c=r.querySelectorAll('*');for(var i=0;i<c.length;i++){o+=walkShadow(c[i],d+1);}}return o;}"
                    + "for(var si=0;si<selectors.length;si++){var q=document.querySelector(selectors[si]);if(q){"
                    + "var tx=(q.innerText||q.textContent||'').trim();if(tx.length>0){return JSON.stringify({mode:'selector',selector:selectors[si],lines:trimLines(tx)});}}}"
                    + "var b=document.body?(document.body.innerText||document.body.textContent||''):'';var sh='';"
                    + "var all=document.querySelectorAll('*');for(var j=0;j<all.length;j++){sh+=walkShadow(all[j],0);}"
                    + "var comb=(b+'\\n'+sh);return JSON.stringify({mode:'fallback',selector:'',lines:trimLines(comb)});})()";

    private ShopListCdpReader() {
    }

    /**
     * Inspects the page: canvas count, body text length/sample, and a heuristic recommendation.
     */
    public static ShopUiInspection inspect(int debuggingPort) {
        try {
            ChromeServiceImpl chromeService = new ChromeServiceImpl(debuggingPort);
            ChromeTab tab = findMagicGardenTab(chromeService.getTabs());
            if (tab == null) {
                return new ShopUiInspection(
                        false,
                        "No Magic Garden tab (URL containing magicgarden) with a debugger URL.",
                        0,
                        0,
                        "",
                        ShopUiInspection.Recommendation.CDP_UNAVAILABLE);
            }
            try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
                dts.getRuntime().enable();
                Evaluate ev = dts.getRuntime().evaluate(INSPECT_SCRIPT);
                String json = stringFromEvaluate(ev);
                JsonNode node = MAPPER.readTree(json);
                int canvasCount = node.path("canvasCount").asInt();
                int bodyLen = node.path("bodyTextLength").asInt();
                String sample = node.path("bodyTextSample").asText("");
                ShopUiInspection.Recommendation rec = recommend(canvasCount, bodyLen);
                return new ShopUiInspection(true, "", canvasCount, bodyLen, sample, rec);
            }
        } catch (ChromeServiceException e) {
            return new ShopUiInspection(
                    false,
                    formatCdpFailure(e),
                    0,
                    0,
                    "",
                    ShopUiInspection.Recommendation.CDP_UNAVAILABLE);
        } catch (Exception e) {
            return new ShopUiInspection(
                    false,
                    e.getMessage(),
                    0,
                    0,
                    "",
                    ShopUiInspection.Recommendation.CDP_UNAVAILABLE);
        }
    }

    /**
     * Returns non-empty text lines: tries {@link ShopListDomConfig#DEFAULT_SHOP_LIST_SELECTORS} for
     * {@code innerText}/{@code textContent}, then falls back to body plus shadow-root traversal.
     */
    public static List<String> readDomTextLines(int debuggingPort) throws ChromeServiceException {
        return readDomTextLines(debuggingPort, ShopListDomConfig.DEFAULT_SHOP_LIST_SELECTORS);
    }

    /**
     * Same as {@link #readDomTextLines(int)} with custom container selectors (order matters).
     */
    public static List<String> readDomTextLines(int debuggingPort, String[] listContainerSelectors)
            throws ChromeServiceException {
        ChromeServiceImpl chromeService = new ChromeServiceImpl(debuggingPort);
        ChromeTab tab = findMagicGardenTab(chromeService.getTabs());
        if (tab == null) {
            return Collections.emptyList();
        }
        String script = buildListFromContainersScript(listContainerSelectors);
        try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
            dts.getRuntime().enable();
            Evaluate ev = dts.getRuntime().evaluate(script);
            String json = stringFromEvaluate(ev);
            return parseListExtractionJson(json);
        }
    }

    /**
     * Legacy: whole {@code document.body.innerText} split into lines (no container/shadow logic).
     */
    public static List<String> readDomTextLinesBodyOnly(int debuggingPort) throws ChromeServiceException {
        ChromeServiceImpl chromeService = new ChromeServiceImpl(debuggingPort);
        ChromeTab tab = findMagicGardenTab(chromeService.getTabs());
        if (tab == null) {
            return Collections.emptyList();
        }
        try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
            dts.getRuntime().enable();
            Evaluate ev = dts.getRuntime().evaluate(DOM_LINES_SCRIPT);
            String json = stringFromEvaluate(ev);
            try {
                return MAPPER.readValue(json, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Invalid JSON from DOM_LINES_SCRIPT", e);
            }
        }
    }

    /**
     * Non-empty accessibility {@link AXNode#getName()} values from {@code Accessibility.getFullAXTree}
     * (deduplicated, insertion order preserved).
     */
    public static List<String> readAccessibilityNames(int debuggingPort) throws ChromeServiceException {
        ChromeServiceImpl chromeService = new ChromeServiceImpl(debuggingPort);
        ChromeTab tab = findMagicGardenTab(chromeService.getTabs());
        if (tab == null) {
            return Collections.emptyList();
        }
        try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
            dts.getAccessibility().enable();
            List<AXNode> nodes = dts.getAccessibility().getFullAXTree();
            Set<String> unique = new LinkedHashSet<>();
            for (AXNode n : nodes) {
                if (Boolean.TRUE.equals(n.getIgnored())) {
                    continue;
                }
                AXValue nameVal = n.getName();
                if (nameVal == null || nameVal.getValue() == null) {
                    continue;
                }
                String s = String.valueOf(nameVal.getValue()).trim();
                if (!s.isEmpty()) {
                    unique.add(s);
                }
            }
            return new ArrayList<>(unique);
        }
    }

    private static String buildListFromContainersScript(String[] selectors) {
        try {
            String json =
                    MAPPER.writeValueAsString(
                            selectors == null || selectors.length == 0
                                    ? Collections.emptyList()
                                    : Arrays.asList(selectors));
            return LIST_FROM_CONTAINERS_AND_SHADOW_SCRIPT_PREFIX + json + LIST_FROM_CONTAINERS_AND_SHADOW_SCRIPT_SUFFIX;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<String> parseListExtractionJson(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode lines = root.get("lines");
            if (lines != null && lines.isArray()) {
                return MAPPER.convertValue(lines, new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON from list extraction script", e);
        }
        return Collections.emptyList();
    }

    private static String formatCdpFailure(ChromeServiceException e) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(e.getMessage(), "Chrome debugging unavailable"));
        Throwable c = e.getCause();
        if (c != null) {
            sb.append(" — ").append(c.getMessage());
        }
        sb.append(" — Start Chrome with --remote-debugging-port=")
                .append(MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT)
                .append(" (see MagicGardenOpener).");
        return sb.toString();
    }

    private static ShopUiInspection.Recommendation recommend(int canvasCount, int bodyTextLength) {
        if (canvasCount >= 1 && bodyTextLength < 300) {
            return ShopUiInspection.Recommendation.CANVAS_LIKELY;
        }
        if (bodyTextLength > 400) {
            return ShopUiInspection.Recommendation.DOM_TEXT_LIKELY;
        }
        return ShopUiInspection.Recommendation.UNCLEAR;
    }

    private static ChromeTab findMagicGardenTab(List<ChromeTab> tabs) {
        for (ChromeTab t : tabs) {
            if (!t.isPageType()) {
                continue;
            }
            String u = t.getUrl();
            if (u != null && u.contains("magicgarden") && t.getWebSocketDebuggerUrl() != null) {
                return t;
            }
        }
        return null;
    }

    private static String stringFromEvaluate(Evaluate evaluate) {
        if (evaluate.getExceptionDetails() != null) {
            throw new IllegalStateException(evaluate.getExceptionDetails().getText());
        }
        RemoteObject ro = evaluate.getResult();
        if (ro == null) {
            return "";
        }
        Object v = ro.getValue();
        return v == null ? "" : String.valueOf(v);
    }
}
