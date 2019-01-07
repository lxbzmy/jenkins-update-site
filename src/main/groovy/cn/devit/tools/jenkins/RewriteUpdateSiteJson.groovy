package cn.devit.tools.jenkins

/**
 *
 * <p>
 *
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
