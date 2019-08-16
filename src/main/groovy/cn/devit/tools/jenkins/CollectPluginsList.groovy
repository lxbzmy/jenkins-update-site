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

import groovy.io.FileVisitResult
import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * After download finish, collectAndSaveFile plugins url in to one text file.
 *
 * <p> 
 *
 *
 * @author lxb
 *
 */
public class CollectPluginsList {

  static final Logger logger = LoggerFactory.getLogger(CollectPluginsList.class);

  public void collectAndSaveFile(Config config) {
    TreeMap<String, String> urlToSha1 = collectPlugins(config)
    File pluginsListFile = new File(config.workingDir, "plugins.txt");
    pluginsListFile.text = urlToSha1.keySet().join("\n");

    logger.info("save {}, contains {} urls", pluginsListFile, urlToSha1.size());

    File toSaveSha1 = new File(config.workingDir, "sha1.txt");
    toSaveSha1.text = urlToSha1.collect { it ->
      String path = new URL(it.key).getPath().substring(1);
      return "${it.value}  ${path}";
    }.join("\n");

    logger.info("save {}, contains {} hashes", pluginsListFile, urlToSha1.size());
  }

  /**key is plugin url, value is sha1 hex lowercase.*/
  public TreeMap<String, String> collectPlugins(Config config) {
    TreeMap<String, String> urlToSha1 = new TreeMap<>();

    config.getCacheDir().traverse { File it ->
      if (it.name == 'update-center.actual.json') {
        logger.info("reading {}", it);
        def json = new JsonSlurper().parseText(it.text);
        //for jenkins war
        urlToSha1.put(json.core.url, from_base64_to_sha1hex(json.core.sha1));
        //
        Collection<Object> plugins = (json.plugins.values());
        plugins.each { plugin ->
          urlToSha1.put(plugin.url, from_base64_to_sha1hex(plugin.sha1));
        }

      }
      return FileVisitResult.CONTINUE;
    }
    urlToSha1
  }

  /**Delete plugins that not in any update-center.json*/
  public void discardOutDatePlugins(Config config) {
    def pluginsPathSet = collectPlugins(config)
            .keySet()
            .collect { it -> return new URL(it).getPath() };
    def root = config.getDownloadDir();
    def base = root.getParentFile().getPath();
    int count1 = 0;
    int outDateCount = 0;
    long byteLength = 0;
    root.traverse { File it ->
//      println it.getPath();
      if (it.isFile()) {
        int len = base.length();
        String rel = it.getPath().substring(len);
        logger.debug("check {}", rel)
        if (pluginsPathSet.contains(rel)) {
          count1++
          //we can speed up check by remove already check item.
          pluginsPathSet.remove(rel);
        } else {
          outDateCount++;
          byteLength += it.length();
          deletePlugin(it);
        }

      }
    }
    logger.info("删除{}个过期插件，保留{}个插件，节约空间{}。",
            outDateCount, count1,
            FileUtils.byteCountToDisplaySize(byteLength))

  }

  def deletePlugin(File file) {
    logger.info("delete {}", file)
    File dir = file.getParentFile();
    file.delete();
    if (dir.list().length == 0) {
      dir.delete();
    }
  }

  public String from_base64_to_sha1hex(base64EncodedSha1) {
    return Hex.encodeHexString(Base64.decoder.decode(base64EncodedSha1))
  };
}
