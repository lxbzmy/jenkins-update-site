package cn.devit.tools.jenkins;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * <p>
 *
 * @author lxb
 */
public class CollectPluginsListDemo {

  @Test
  public void case1() throws Exception {
    File tmp = new File("target/temp");
    tmp.mkdir();
    Config config = new Config();
    config.setWorkingDir(tmp);
    config.setCacheDir(tmp);
    new CollectPluginsList().collectAndSaveFile(config);
  }

}