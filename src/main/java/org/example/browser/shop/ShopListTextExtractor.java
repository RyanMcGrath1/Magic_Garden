package org.example.browser.shop;

import com.github.kklisura.cdt.services.exceptions.ChromeServiceException;
import java.util.List;
import org.example.MagicGardenOpener;

public final class ShopListTextExtractor {

    private ShopListTextExtractor() {
    }

    public static List<String> readScrollableListDefaultPort() throws ChromeServiceException {
        if (ShopListDomConfig.hasShopKeywords()) {
            return ShopListCdpReader.readScrollableShopListByKeywordsDefault(
                    MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT);
        }
        return ShopListCdpReader.readScrollableShopListLinesDefaultSelectors(
                MagicGardenOpener.CHROME_REMOTE_DEBUGGING_PORT);
    }
}
