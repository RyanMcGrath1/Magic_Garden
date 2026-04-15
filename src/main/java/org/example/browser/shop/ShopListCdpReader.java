package org.example.browser.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kklisura.cdt.protocol.types.runtime.Evaluate;
import com.github.kklisura.cdt.protocol.types.runtime.RemoteObject;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Reads shop content from the live Magic Garden tab via CDP {@code Runtime.evaluate}. Injected JavaScript returns
 * JSON; no HTML parser on the JVM. Chrome session lifecycle is delegated to
 * {@link ShopCdpSessionManager}.
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

    public static ChromeDevToolsService getSharedRuntimeEnabledDevToolsSession(int debuggingPort)
            throws ChromeServiceException {
        return ShopCdpSessionManager.getSharedRuntimeEnabledDevToolsSession(debuggingPort);
    }

    public static void closeSharedCdpSession() {
        ShopCdpSessionManager.closeSharedCdpSession();
    }

    public static List<String> readScrollableShopListLines(
            int debuggingPort,
            String[] listContainerSelectors,
            int maxSteps,
            int stableRoundsWithNoNewLines)
            throws ChromeServiceException {
        ChromeDevToolsService dts = getSharedRuntimeEnabledDevToolsSession(debuggingPort);
        String script = buildScrollStepScript(listContainerSelectors);
        LinkedHashSet<String> accumulated = new LinkedHashSet<>();
        int noNewStreak = 0;
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
        return new ArrayList<>(accumulated);
    }

    /**
     * Finds a visible shop {@code button} matching {@code shopLine} / {@code itemLabel} and clicks it using the shared
     * session.
     */
    public static boolean clickMatchingShopButtonSharedSession(
            int debuggingPort, String[] listContainerSelectors, String shopLine, String itemLabel)
            throws ChromeServiceException {
        ChromeDevToolsService dts = getSharedRuntimeEnabledDevToolsSession(debuggingPort);
        String script = buildClickMatchingShopButtonScript(listContainerSelectors, shopLine, itemLabel);
        Evaluate ev = dts.getRuntime().evaluate(script);
        String json = stringFromEvaluate(ev);
        return parseClickResult(json);
    }

    public static List<String> readScrollableShopListLinesDefaultSelectors(int debuggingPort)
            throws ChromeServiceException {
        return readScrollableShopListLines(
                debuggingPort, ShopListDomConfig.DEFAULT_SHOP_LIST_SELECTORS, 48, 3);
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

    private static String buildClickMatchingShopButtonScript(String[] selectors, String shopLine, String itemLabel) {
        try {
            String selJson =
                    MAPPER.writeValueAsString(
                            selectors == null || selectors.length == 0
                                    ? Collections.emptyList()
                                    : Arrays.asList(selectors));
            String lineJson = MAPPER.writeValueAsString(shopLine == null ? "" : shopLine);
            String labelJson = MAPPER.writeValueAsString(itemLabel == null ? "" : itemLabel);
            return "(function(){var selectors="
                    + selJson
                    + ";var targetLine="
                    + lineJson
                    + ";var targetLabel="
                    + labelJson
                    + ";function trimOnly(t){return (t||'').trim();}"
                    + "function findScrollable(el){var e=el;while(e&&e!==document.body){var st=window.getComputedStyle(e);"
                    + "if(e.scrollHeight>e.clientHeight+2&&/(auto|scroll|overlay)/.test(st.overflowY))return e;"
                    + "e=e.parentElement;}return el;}"
                    + "function visibleInContainer(el,c){var r=el.getBoundingClientRect(),cr=c.getBoundingClientRect();"
                    + "return r.height>0&&r.width>0&&r.bottom>cr.top&&r.top<cr.bottom&&r.right>cr.left&&r.left<cr.right;}"
                    + "function forEachButton(root,c,fn){var sel='button';var btns=root.querySelectorAll(sel);"
                    + "for(var i=0;i<btns.length;i++){fn(btns[i]);}"
                    + "var all=root.querySelectorAll('*');for(var j=0;j<all.length;j++){"
                    + "if(all[j].shadowRoot){var inner=all[j].shadowRoot.querySelectorAll(sel);"
                    + "for(var k=0;k<inner.length;k++){fn(inner[k]);}}}}"
                    + "function tryClick(btn,c,wantL,wantB,mode){if(!visibleInContainer(btn,c))return false;"
                    + "var raw=trimOnly((btn.innerText||btn.textContent||''));if(!raw)return false;"
                    + "if(mode===1){if(!wantL||raw!==wantL)return false;}"
                    + "else{if(!wantB||raw.indexOf(wantB)<0)return false;}"
                    + "try{btn.scrollIntoView({block:'center',inline:'nearest'});btn.click();return true;}catch(e){return false;}}"
                    + "var anchor=null;for(var si=0;si<selectors.length&&!anchor;si++){anchor=document.querySelector(selectors[si]);}"
                    + "if(!anchor){anchor=document.body;}"
                    + "if(!anchor){return JSON.stringify({ok:false});}"
                    + "var container=findScrollable(anchor)||anchor;"
                    + "var wantL=trimOnly(targetLine);var wantB=trimOnly(targetLabel);var clicked=false;"
                    + "if(wantL){forEachButton(container,container,function(btn){if(clicked)return;"
                    + "if(tryClick(btn,container,wantL,wantB,1))clicked=true;});}"
                    + "if(!clicked&&wantB){forEachButton(container,container,function(btn){if(clicked)return;"
                    + "if(tryClick(btn,container,wantL,wantB,2))clicked=true;});}"
                    + "return JSON.stringify({ok:clicked});})()";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean parseClickResult(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            return root.path("ok").asBoolean(false);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON from click shop button script", e);
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
