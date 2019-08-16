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

import com.google.common.io.Files
import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Hex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Read update/*.json from cache, save as wget link text.
 *
 * <p> 
 *
 *
 * @author lxb
 *
 */
public class CollectToolsList {

  static final Logger logger = LoggerFactory.getLogger(CollectToolsList.class);

  public void collect(Config config) {
    TreeSet<String> idSet = new TreeSet();
    File updateFolder = new File(config.getCacheDir(), "updates");

    List<String> todo;
    if (config.getTools()) {
      todo = config.getTools();
    } else {
      logger.info("No tools configured to download.")
      return;
    }
    updateFolder.listFiles().each { File it ->
      String name = it.name;
      String toolId = Files.getNameWithoutExtension(name);
      if (it.isFile() && it.name.endsWith(".json")) {
        if (JsonFileOutput.unsupport.contains(toolId)) {
          logger.info("unsupported {}", toolId);
          return;
        }
        if (!todo.contains(toolId)) {
          logger.info("skip {}", toolId);
          return;
        }
        logger.info("reading {}", it);
        def json = new JsonSlurper().parseText(
                RewriteUpdateSiteJson.stripJsCallbackFunction(it.text));

        assert json.list instanceof List;

        List<String> list = [];
        json.list.each { item ->
          list.add(item.url);
        }
        File oneTool = new File(config.getWorkingDir(), "${toolId}.txt");
        oneTool.text = list.join("\n");
        logger.info("save {}, contains {} urls", oneTool, list.size());
        idSet.add(toolId);
      }
    }
    File toolIndex = new File(config.getWorkingDir(), "tools.txt");
    toolIndex.text = idSet.join("\n");
    logger.info("save {}, contains {} tools", toolIndex, idSet.size());
  }

  public String from_base64_to_sha1hex(base64EncodedSha1) {
    return Hex.encodeHexString(Base64.decoder.decode(base64EncodedSha1))
  };
}
