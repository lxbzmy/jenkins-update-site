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

/**
 * @author lxb
 */
class RewriteUpdateSiteJson {

    Map<String, Object> json;

    String jenkinsUpdateSite = new URL('http://updates.jenkins-ci.org')
    URL url

    public RewriteUpdateSiteJson(Map json) {
        this.json = json;
        assert json.updateCenterVersion == "1";
    }

    /**官方是http://www.google.com/你可以改成其他的*/
    public void setConnectionCheckUrl(String newUrl) {
        json.put("connectionCheckUrl", newUrl);
    }

    /**官方的是<code>http://updates.jenkins-ci.org</code>，作镜像需要改成内网能够访问的地址*/
    public void rewritePluginDownloadHost(String host) {
        this.url = new URL(host);
        String match = jenkinsUpdateSite.toString();
        String to = url.toString();

        json.core.url = json.core.url.replace(match, to);
        Map<String, Object> pluginsMap = json.plugins;
        pluginsMap.each { k, v ->
            v.url = v.url.replace(match, to);
        }
    }

    public Map<String, Object> getJson() {
        return json;
    }

    //TODO plugin list(name,url,sha1)

    /**官方json文件带着callback，要先去了*/
    public static String stripJsCallbackFunction(String input) {
        return input.substring(input.indexOf("{"),
                input.lastIndexOf("}") + 1);
    }
}
