package cn.devit.tools.jenkins

import cn.devit.tools.jenkins.model.ToolLink

/**
 *
 * <p>
 *
 * @author lxb
 */
class RewriteToolInstallJson {

    Map<String, Object> json;
    URL mavenMirror;

    public RewriteToolInstallJson(Map json) {
        this.json = json;
        assert json.list instanceof List
    }

//    /**有部分工具通过maven center发布（例如：maven，sonar runner，），那么只需要设置为内网maven center镜像就能直接下载了。*/
//    public setMavenMirrorUrl(String nexusCenterMirror) {
//        mavenMirror = new URL(nexusCenterMirror);
//    }

    /**tool download url是工具自制的，各式各样的都有。*/
    public void rewriteToolDownloadUrl(String find, String replaceWith) {
        List<ToolLink> pluginsMap = json.list;
        pluginsMap.each { v ->
            v.url = v.url.replace(find, replaceWith);
        }
    }

    public void rewriteUrlSchemaAndHost(URL newHost) {
        List<ToolLink> pluginsMap = json.list;
        assert pluginsMap.isEmpty() == false;
        String find = new URL(new URL(pluginsMap[0].url), '/').toString();
        String to = newHost.toString();
        pluginsMap.each { v ->
            v.url = v.url.replace(find, to);
        }
    }


    public Map<String, Object> getJson() {
        return json;
    }


}
