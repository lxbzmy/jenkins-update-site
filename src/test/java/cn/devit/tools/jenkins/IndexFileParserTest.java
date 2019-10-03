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
                        new URL("http://updates.jenkins-ci.org/current/"));
        assertEquals(list.size(), 9);
    }

    @Test
    public void tsinghua() throws Exception {
        final List<IndexFileParser.IndexItem> list = new IndexFileParser()
                .parseIndexFile(
                        new URL("https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/current"));
        assertEquals(list.size(), 7);
    }
    @Test
    public void ustc() throws Exception {
        final List<IndexFileParser.IndexItem> list = new IndexFileParser()
                .parseIndexFile(
                        new URL("https://mirrors.ustc.edu.cn/jenkins/updates/current/"));
        assertEquals(list.size(), 7);
    }

}