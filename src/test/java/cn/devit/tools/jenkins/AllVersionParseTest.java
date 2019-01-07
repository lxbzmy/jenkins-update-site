package cn.devit.tools.jenkins;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * <p>
 *
 * @author lxb
 */
public class AllVersionParseTest {

    @Test
    public void case1() throws Exception {
        File tmp = new File("target/temp");
        tmp.mkdir();
        new AllVersionParse()
                .parseAndDownload(tmp, new URL("http://updates.jenkins-ci.org/"));
    }
}