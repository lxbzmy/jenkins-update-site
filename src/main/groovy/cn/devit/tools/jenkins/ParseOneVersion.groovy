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

import cn.devit.tools.jenkins.util.FileDownload
import cn.devit.tools.jenkins.util.UrlByteSourceWithTimeout
import com.google.common.io.Closer
import com.google.common.io.Resources

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration

/**
 * Parse one jenkins update json.
 * <p>
 *
 * @author lxb
 */
class ParseOneVersion {


    File pwd;

    /** Input index url and save to file*/
    public void parseAndSave(File saveHere, URL index) {
        this.pwd = saveHere;

        //what if 302 happened?
        List<IndexFileParser.IndexItem> elements = new IndexFileParser().parseIndexFile(index);
        Closer closer = Closer.create();
        try {
            //TODO memoto
            elements.each { it ->
                String name = it.name;
                String href = it.href;
                if (name == 'latest/') {
                    //skip latest,it is just permalinks to latest files

                } else if (name == 'updates/') {

                } else if (isDirectory(name)) {
                    //warning .
                } else if (name.endsWith(".json.html")) {
                    println "Skip ${name}"
                } else {
                    //save file to directory.
                    //TODO 重构下载过程，以方便mock和测试。
                    FileDownload.download(new URL(index, name),new File(pwd,name));
                    Thread.sleep(1000)
                }

            }
        } finally {
            closer.close();
        }
    }

    boolean isDirectory(String name) {
        return name.endsWith("/")
    }
}
