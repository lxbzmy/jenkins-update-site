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

import com.google.common.io.Closer
import com.google.common.io.Resources

import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
                try {
                    if (name == 'latest/') {
                        //skip latest is just Permalinks to latest files

                    } else if (name == 'updates/') {

                    } else if (isDirectory(name)) {
                        //warning .
                    } else if (name.endsWith(".json.html")) {
                        println "Skip ${name}"
                    } else {
                        //save file to directory.
                        //TODO 重构下载过程，以方便mock和测试。
                        File file = File.createTempFile("wget", "download")
                        println "Downloading ${name}"
                        Resources.copy(new URL(index, name),
                                closer.register(new FileOutputStream(file)))
                        Files.move(file.toPath(),
                                new File(pwd,name).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    //continue.
                    println "Error while downloading ${name}"

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
