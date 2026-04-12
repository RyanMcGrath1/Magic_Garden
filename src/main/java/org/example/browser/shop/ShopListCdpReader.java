package org.example.browser.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kklisura.cdt.protocol.types.runtime.Evaluate;
import com.github.kklisura.cdt.protocol.types.runtime.RemoteObject;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.example.browser.ChromeDebugPreflight;

/**
 * Reads shop content from the live Magic Garden tab via CDP {@code Runtime.evaluate}. Injected JavaScript returns
 * JSON; no HTML parser on the JVM. One {@link ChromeDevToolsService} is held for the entire scroll loop.
 */
public final class ShopListCdpReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SCROLL_STEP_SCRIPT_PREFIX = "(function(){var selectors=";

    private static final String SCROLL_STEP_SCRIPT_SUFFIX =
            ";function findScrollable(el){var e=el;while(e&&e!==document.body){var st=window.getComputedStyle(e);"
                    + "if(e.scrollHeight>e.clientHeight+2&&/(auto|scroll|overlay)/.test(st.overflowY))return e;"
                    + "e=e.parentElement;}return el;}"
                    + "function visibleInContainer(el,c){var r=el.getBoundingClientRect(),cr=c.getBoundingClientRect();"
                    + "return r.height>0&&r.width>0&&r.bottom>cr.top&&r.top<cr.bottom&&r.right>cr.left&&r.left<cr.right;}"
                    + "function collectVisible(root,c){var lines=[],seen={},add=function(t){if(t&&!seen[t]){seen[t]=1;lines.push(t);}};"
                    + "var sel='button';var btns=root.querySelectorAll(sel);"
                    + "for(var i=0;i<btns.length;i++){if(visibleInContainer(btns[i],c)){"
                    + "var tx=(btns[i].innerText||btns[i].textContent||'').trim();if(tx)add(tx);}}"
                    + "var all=root.querySelectorAll('*');for(var j=0;j<all.length;j++){"
                    + "if(all[j].shadowRoot){var inner=all[j].shadowRoot.querySelectorAll(sel);"
                    + "for(var k=0;k<inner.length;k++){if(visibleInContainer(inner[k],c)){"
                    + "var t2=(inner[k].innerText||inner[k].textContent||'').trim();if(t2)add(t2);}}}}return lines;}"
                    + "var anchor=null;for(var si=0;si<selectors.length&&!anchor;si++){anchor=document.querySelector(selectors[si]);}"
                    + "if(!anchor){anchor=document.body;}"
                    + "if(!anchor){return JSON.stringify({error:'no_document',lines:[],atEnd:true});}"
                    + "var container=findScrollable(anchor)||anchor;"
                    + "var lines=collectVisible(container,container);"
                    + "var maxScroll=Math.max(0,container.scrollHeight-container.clientHeight);"
                    + "var step=Math.max(48,Math.floor(container.clientHeight*0.8));"
                    + "var before=container.scrollTop;var next=Math.min(maxScroll,before+step);"
                    + "container.scrollTop=next;var atEnd=next>=maxScroll-1;"
                    + "return JSON.stringify({lines:lines,atEnd:atEnd,scrollTop:container.scrollTop,maxScroll:maxScroll});})()";

    private ShopListCdpReader() {
    }

    public static List<String> readScrollableShopListLines(
            int debuggingPort,
            String[] listContainerSelectors,
            int maxSteps,
            int stableRoundsWithNoNewLines)
            throws ChromeServiceException {
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
        String script = buildScrollStepScript(listContainerSelectors);
        LinkedHashSet<String> accumulated = new LinkedHashSet<>();
        int noNewStreak = 0;
        try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
            dts.getRuntime().enable();
            for (int step = 0; step < maxSteps; step++) {
                Evaluate ev = dts.getRuntime().evaluate(script);
                String json = stringFromEvaluate(ev);
                ScrollStepResult result = parseScrollStep(json);
                if (result.error != null) {
                    if (step == 0) {
                        throw new IllegalStateException("Shop list script: " + result.error);
                    }
                    break;
                }
                int sizeBefore = accumulated.size();
                for (String line : result.lines) {
                    accumulated.add(line);
                }
                if (accumulated.size() == sizeBefore) {
                    noNewStreak++;
                } else {
                    noNewStreak = 0;
                }
                if (noNewStreak >= stableRoundsWithNoNewLines) {
                    break;
                }
                if (result.atEnd && accumulated.size() == sizeBefore) {
                    break;
                }
            }
        }
        return new ArrayList<>(accumulated);
    }

    public static List<String> readScrollableShopListLinesDefaultSelectors(int debuggingPort)
            throws ChromeServiceException {
        return readScrollableShopListLines(
                debuggingPort, ShopListDomConfig.DEFAULT_SHOP_LIST_SELECTORS, 48, 3);
    }

    public static List<String> readScrollableShopListByKeywords(
            int debuggingPort,
            String[] keywords,
            int ancestorMaxHops,
            int maxElementsPerKeywordPerStep,
            int maxSteps,
            int stableRoundsWithNoNewLines)
            throws ChromeServiceException {
        String[] kw = normalizedKeywords(keywords);
        if (kw.length == 0) {
            throw new IllegalStateException("keywords must be non-empty for keyword mode");
        }
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
        String script = buildKeywordScrollScriptOrThrow(kw, ancestorMaxHops, maxElementsPerKeywordPerStep);
        LinkedHashSet<String> accumulated = new LinkedHashSet<>();
        int noNewStreak = 0;
        try (ChromeDevToolsService dts = chromeService.createDevToolsService(tab)) {
            dts.getRuntime().enable();
            for (int step = 0; step < maxSteps; step++) {
                Evaluate ev = dts.getRuntime().evaluate(script);
                String json = stringFromEvaluate(ev);
                KeywordStepResult result = parseKeywordStep(json);
                if (result.error != null) {
                    if (step == 0) {
                        throw new IllegalStateException("Shop keyword script: " + result.error);
                    }
                    break;
                }
                int sizeBefore = accumulated.size();
                for (String snippet : result.snippets) {
                    accumulated.add(snippet);
                }
                if (accumulated.size() == sizeBefore) {
                    noNewStreak++;
                } else {
                    noNewStreak = 0;
                }
                if (noNewStreak >= stableRoundsWithNoNewLines) {
                    break;
                }
                if (result.atEnd && accumulated.size() == sizeBefore) {
                    break;
                }
            }
        }
        return new ArrayList<>(accumulated);
    }

    public static List<String> readScrollableShopListByKeywordsDefault(int debuggingPort)
            throws ChromeServiceException {
        String[] kw = normalizedKeywords(ShopListDomConfig.SHOP_KEYWORDS);
        if (kw.length == 0) {
            throw new IllegalStateException("ShopListDomConfig.SHOP_KEYWORDS is empty");
        }
        return readScrollableShopListByKeywords(
                debuggingPort,
                kw,
                ShopListDomConfig.ANCESTOR_DIV_MAX_HOPS,
                ShopListDomConfig.MAX_ELEMENTS_PER_KEYWORD_PER_STEP,
                48,
                3);
    }

    private static String buildKeywordScrollScriptOrThrow(String[] keywords, int maxHops, int maxPerKeyword) {
        String cfgJson;
        try {
            Map<String, Object> cfg = new HashMap<>();
            cfg.put("keywords", Arrays.asList(keywords));
            cfg.put("maxHops", maxHops);
            cfg.put("maxPerKeyword", maxPerKeyword);
            cfgJson = MAPPER.writeValueAsString(cfg);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return "(function(){var cfg="
                + cfgJson
                + ";var keywords=cfg.keywords;var maxHops=cfg.maxHops;var maxPerKeyword=cfg.maxPerKeyword;"
                + "function enclosingDivOrSection(start){var e=start;var h=0;"
                + "while(e&&h<maxHops){var tag=e.tagName&&e.tagName.toLowerCase();"
                + "if(tag==='div'||tag==='section')return e;e=e.parentElement;h++;}return start;}"
                + "var el=document.documentElement;"
                + "var maxScroll=Math.max(0,el.scrollHeight-el.clientHeight);"
                + "var step=Math.max(48,Math.floor(el.clientHeight*0.8));"
                + "var next=Math.min(maxScroll,el.scrollTop+step);"
                + "el.scrollTop=next;"
                + "var atEnd=next>=maxScroll-1;"
                + "var snippets=[];var seen={};"
                + "for(var ki=0;ki<keywords.length;ki++){var kw=keywords[ki];if(!kw)continue;var n=0;"
                + "var all=document.getElementsByTagName('*');"
                + "for(var i=0;i<all.length&&n<maxPerKeyword;i++){var node=all[i];"
                + "var text=node.textContent||'';if(text.indexOf(kw)<0)continue;"
                + "var enc=enclosingDivOrSection(node);var html=enc.outerHTML;"
                + "if(!seen[html]){seen[html]=1;snippets.push(html);}n++;}}"
                + "return JSON.stringify({snippets:snippets,atEnd:atEnd});})()";
    }


    private static String[] normalizedKeywords(String[] raw) {
        if (raw == null) {
            return new String[0];
        }
        return Arrays.stream(raw)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static KeywordStepResult parseKeywordStep(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("error") && root.get("error").isTextual()) {
                String err = root.get("error").asText();
                if (!err.isEmpty()) {
                    return new KeywordStepResult(err, Collections.emptyList(), true);
                }
            }
            boolean atEnd = root.path("atEnd").asBoolean(false);
            JsonNode sn = root.get("snippets");
            List<String> snippets =
                    sn != null && sn.isArray()
                            ? MAPPER.convertValue(sn, new TypeReference<List<String>>() {})
                            : Collections.emptyList();
            return new KeywordStepResult(null, snippets, atEnd);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON from keyword scroll script", e);
        }
    }

    private static final class KeywordStepResult {
        final String error;
        final List<String> snippets;
        final boolean atEnd;

        KeywordStepResult(String error, List<String> snippets, boolean atEnd) {
            this.error = error;
            this.snippets = snippets;
            this.atEnd = atEnd;
        }
    }

    private static String buildScrollStepScript(String[] selectors) {
        try {
            String json =
                    MAPPER.writeValueAsString(
                            selectors == null || selectors.length == 0
                                    ? Collections.emptyList()
                                    : Arrays.asList(selectors));
            return SCROLL_STEP_SCRIPT_PREFIX + json + SCROLL_STEP_SCRIPT_SUFFIX;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ScrollStepResult parseScrollStep(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("error") && root.get("error").isTextual()) {
                String err = root.get("error").asText();
                if (!err.isEmpty()) {
                    return new ScrollStepResult(err, Collections.emptyList(), true);
                }
            }
            boolean atEnd = root.path("atEnd").asBoolean(false);
            JsonNode linesNode = root.get("lines");
            List<String> lines =
                    linesNode != null && linesNode.isArray()
                            ? MAPPER.convertValue(linesNode, new TypeReference<List<String>>() {})
                            : Collections.emptyList();
            return new ScrollStepResult(null, lines, atEnd);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON from scroll step script", e);
        }
    }

    private static final class ScrollStepResult {
        final String error;
        final List<String> lines;
        final boolean atEnd;

        ScrollStepResult(String error, List<String> lines, boolean atEnd) {
            this.error = error;
            this.lines = lines;
            this.atEnd = atEnd;
        }
    }

    /**
     * Top-level page tab whose URL contains {@code magicgarden} (excludes iframe/worker targets and extension pages).
     */
    static ChromeTab findMagicGardenTab(List<ChromeTab> tabs) {
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

    private static String stringFromEvaluate(Evaluate evaluate) {
        if (evaluate.getExceptionDetails() != null) {
            throw new IllegalStateException(
                    Objects.toString(evaluate.getExceptionDetails().getText(), "evaluate failed"));
        }
        RemoteObject ro = evaluate.getResult();
        if (ro == null) {
            return "";
        }
        Object v = ro.getValue();
        return v == null ? "" : String.valueOf(v);
    }
}
