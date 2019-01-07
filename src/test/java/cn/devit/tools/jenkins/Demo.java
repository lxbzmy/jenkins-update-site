package cn.devit.tools.jenkins;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * <p>
 *
 * @author lxb
 */
public class Demo {

  private Config config;

  @Test
  public void plugins_download_text() {
    new CollectPluginsList().collectAndSaveFile(config);
  }

  @Test
  public void tools_text_file() {
    new CollectToolsList().collect(config);
  }

  @Test
  public void discard_old_hpi(){
    new CollectPluginsList().discardOutDatePlugins(config);
  }

  @Before
  public void setup() {
    Config config = new Config();
    config.setWorkingDir(new File("target"));
    config.setCacheDir(new File("target/cache"));
    config.setTools(JsonFileOutput.getToolsDownload());
    this.config = config;
  }
  
  @Test
  public void test1(){
      new JsonFileOutput().saveToFile();
  }
}
