/*
 * Copyright 2017-2019 lxb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.devit.tools.jenkins

import com.google.common.io.Closer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
 * Parse html page act as file list.
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
