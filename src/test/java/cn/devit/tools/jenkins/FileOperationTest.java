package cn.devit.tools.jenkins;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class FileOperationTest {
    @Test
    public void path_join() throws Exception {
        File root = new File("root");
        File child = new File(root, "/plugin/a.hpi");
        assertThat(child.getAbsolutePath(),
                containsString("/root/plugin/a.hpi"));
    }

    @Test
    public void relative_to() throws Exception {
        File root = new File("/target/cache");
        File child = new File("/target/cache/foo/bar.txt");
        String relative = root.toPath().relativize(child.toPath()).toString();
        assertThat(relative, is("foo/bar.txt"));
    }
}
