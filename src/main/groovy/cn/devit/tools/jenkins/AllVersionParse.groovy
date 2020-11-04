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
import com.google.common.io.Closer
import com.google.common.io.Resources
import groovy.json.JsonSlurper

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 *
 * <p>
 *
 * @author lxb
 */
class AllVersionParse {

    File pwd;

    String mirror = "https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/"

    void parseAndDownload(File downloadFolder, URL index) {
        pwd = downloadFolder;

        ParseOneVersion job = new ParseOneVersion();

        def tiersJson = new JsonSlurper().parse(new URL(index,"tiers.json"));
        //ses:http://updates.jenkins-ci.org/tiers.json
        tiersJson.stableCores.each{ it->
            def name = "dynamic-stable-${it}/"
            println "Entering ${name}"
            File dir = mkdir(name)
            job.parseAndSave(dir, new URL(useMirror(index), name));
        }

        tiersJson.weeklyCores.each{it->
            def name = "dynamic-${it}/"
            println "Entering ${name}"
            File dir = mkdir(name)
            job.parseAndSave(dir, new URL(useMirror(index), name));
        }

        //make stable folder
        ['stable','current','update'].each {it->
            def name = "${it}/";
            println "Entering ${name}"
            File dir = mkdir(name)
            job.parseAndSave(dir, new URL(useMirror(index), name));
        }


    }

    public File mkdir(String name) {
        File dir = new File(pwd, name);
        dir.mkdir();
        return dir
    }

    URL useMirror(URL input){
        return new URL(input.toString().replace(Config.jenkins_update_url.toString(),mirror));
    }

    void folderUpdateIsToolsInstallIndex(File pwd, URL index) {
        def list = new IndexFileParser().parseIndexFile(index);
        Closer closer = Closer.create();
        try {
            list.each { it ->
                String name = it.name;
                try {
                    if (name.endsWith(".json")) {
                        //download to temp file then move to desire location.
                        File file = File.createTempFile("wget", "download")
                        println "Downloading ${name}"
                        FileDownload.download(new URL(index, name),new File(pwd,name));
                        Thread.sleep(1000)
                    } else {
                        println "skip ${name}"
                    }
                } catch (IOException e) {
                    //continue;
                }
            }
        } finally {
            closer.close();
        }
    }
}
