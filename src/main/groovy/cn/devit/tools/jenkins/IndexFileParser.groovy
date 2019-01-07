package cn.devit.tools.jenkins

import com.google.common.io.Closer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
 *
 * <p>
 *
 * @author lxb
 */
class IndexFileParser {

    public static final int TimeOutIn30Seconds = 1000 * 30

    List<IndexItem> parseIndexFile(URL url) {
        //what if 302 happened?
        Document document = Jsoup.parse(url, TimeOutIn30Seconds)
        Elements elements = document.select("table tr td:nth-child(2) a");
        Closer closer = Closer.create();
        if (elements.size() > 0) {
            List<IndexItem> list = elements.collect({ it ->
                return new IndexItem(name: it.text(),
                        href: it.attr("href"))
            });
            if (list[0].href.startsWith("/")) {
                return list[1..-1]
            }
            return list;
        }
        return [];
    }

    static class IndexItem {
        String name;
        String href;
    }
}
