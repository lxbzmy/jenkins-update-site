package cn.devit.tools.jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 *
 * @author lxb
 */
public class ParseOneVersionTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();


    @Test
    public void case1() throws Exception {
        File tmp = new File("target/temp");
        tmp.mkdir();
        new ParseOneVersion()
                .parseAndSave(tmp, new URL("http://updates.jenkins-ci.org/current/"));
    }

    @Test
    public void url_path_append() throws Exception {
        URL url1 = new URL("http://updates.jenkins-ci.org/current/");
        URL url2 = new URL(url1, "plugin-versions.json");

        assertEquals(url2.toString(), "http://updates.jenkins-ci.org/current/plugin-versions.json");

    }


}