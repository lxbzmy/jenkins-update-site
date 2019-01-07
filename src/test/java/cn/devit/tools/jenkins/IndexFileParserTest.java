package cn.devit.tools.jenkins;

import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 *
 * @author lxb
 */
public class IndexFileParserTest {
    @Test
    public void parseIndexFile() throws Exception {
        final List<IndexFileParser.IndexItem> list = new IndexFileParser()
                .parseIndexFile(
                        new URL("http://updates.jenkins-ci.org/2.89/"));
        assertEquals(list.size(), 7);
    }

}