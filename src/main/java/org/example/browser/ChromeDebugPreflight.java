package org.example.browser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.example.MagicGardenOpener;

public final class ChromeDebugPreflight {

    private ChromeDebugPreflight() {
    }

    public static boolean isJsonListReachable(int port) {
        return checkOrExplainFailure(port) == null;
    }

    /**
     * @return {@code null} if {@code GET .../json} succeeds with a plausible DevTools JSON body; otherwise a short
     *     diagnostic (connection error, HTTP status, or unexpected body).
     */
    public static String checkOrExplainFailure(int port) {
        String[] hosts = {"127.0.0.1", "localhost"};
        StringBuilder attempts = new StringBuilder();
        for (String host : hosts) {
            String url = "http://" + host + ":" + port + "/json";
            try {
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(Duration.ofSeconds(4))
                                .GET()
                                .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                if (code < 200 || code >= 300) {
                    appendAttempt(attempts, url, "HTTP " + code);
                    continue;
                }
                String body = response.body();
                if (body == null) {
                    appendAttempt(attempts, url, "empty body");
                    continue;
                }
                String trimmed = body.trim();
                if (looksLikeDevtoolsJson(trimmed)) {
                    return null;
                }
                String sample = trimmed.length() > 120 ? trimmed.substring(0, 120) + "…" : trimmed;
                appendAttempt(attempts, url, "unexpected body (starts with): " + sample);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted while probing " + url;
            } catch (Exception e) {
                String msg = e.getMessage();
                appendAttempt(
                        attempts,
                        url,
                        e.getClass().getSimpleName() + (msg != null && !msg.isEmpty() ? ": " + msg : ""));
            }
        }
        return attempts.length() > 0 ? attempts.toString().trim() : "no host attempted";
    }

    private static void appendAttempt(StringBuilder sb, String url, String detail) {
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(url).append(" → ").append(detail);
    }

    private static boolean looksLikeDevtoolsJson(String trimmed) {
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            return true;
        }
        return trimmed.contains("webSocketDebuggerUrl") || trimmed.contains("devtoolsFrontendUrl");
    }

    public static String unreachableMessage(int port) {
        return "Chrome remote debugging is not reachable at http://127.0.0.1:"
                + port
                + "/json. Start Chrome with chrome-with-debugging.bat (or equivalent), then open the game at "
                + MagicGardenOpener.MAGIC_GARDEN_URL
                + " in that window only. Diagnostic: ";
    }
}
