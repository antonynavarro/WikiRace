import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WikiPageScrapper {

    private static final String WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/";


    public List<String> getLinksFromPage(String pageTitle) throws IOException {
        String url = WIKIPEDIA_URL + pageTitle;
        List<String> links = new ArrayList<>();

        Document doc = Jsoup.connect(url).get();
        Elements linkElements = doc.select("a[href^='/wiki/']");

        for (Element linkElement : linkElements) {
            String link = linkElement.attr("href");
            // Avoid certain irrelevant pages, like "Special:" or "Help:" pages
            if (!link.contains(":")) {
                links.add(link.substring(6)); // Remove "/wiki/" part
            }
        }

        return links;
    }
}
