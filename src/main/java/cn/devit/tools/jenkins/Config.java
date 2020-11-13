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
package cn.devit.tools.jenkins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public class Config {

    private void foo(){
        System.out.println("");
    }

    /** <code>http://updates.jenkins-ci.org/</code> */
    public static final URL jenkins_update_url = url(
            "http://updates.jenkins-ci.org/");

    private static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**将插件下载地址替换成镜像地址*/
    public String useMirror(String url){
        //http://updates.jenkins-ci.org/download/
        //https://mirrors.tuna.tsinghua.edu.cn/jenkins/
        return url.replace(jenkins_update_url+"download/",
                "https://mirrors.tuna.tsinghua.edu.cn/jenkins/");
    }

//  static 

    /**
     * servlet doc root
     */
    File workingDir;

    /**
     * save download files from updates.jenkins-ci.org to here
     */
    File cacheDir;

    List<String> tools;

    /** used at download url, must be a public accessible host */
    String host;
    
    private List<String> blackList;
    
    

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public List<String> getTools() {
        return tools;
    }

    /**
     * Mirrored plugins hpi file stored here. path same as official url.
     */
    public File getDownloadDir() {
        final File download = new File(workingDir, "download");
        download.mkdir();
        return download;
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public File getCertificateDir() {
        return new File("certs");
    }

    public List<String> getBlackList() {
      return blackList;
    }

    public void setBlackList(List<String> blackList) {
      this.blackList = blackList;
    }

}
