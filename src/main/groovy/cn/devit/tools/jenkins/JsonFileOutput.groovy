package cn.devit.tools.jenkins

import com.google.common.io.Files

import cn.devit.tools.jenkins.util.KeyTool
import groovy.json.JsonSlurper
import net.sf.json.JSONObject

/**
 *
 * <p>
 *
 * @author lxb
 */
class JsonFileOutput {

    /**
     * 自动下载这4个常用的工具链
     */
    static List<String> toolsDownload = [
            'hudson.plugins.flyway.FlywayInstaller',
            'hudson.plugins.sonar.SonarRunnerInstaller',
            'hudson.tasks.Ant.AntInstaller',
            'hudson.tasks.Maven.MavenInstaller',
    ]

    /**目前无法支持到的工具*/
    static List<String> unsupport = [
            'hudson.plugins.cmake.CmakeInstaller',
            'hudson.tools.JDKInstaller',
            'org.jenkinsci.plugins.golang.GolangInstaller',
            'hudson.plugins.nodejs.tools.NodeJSInstaller',
            'org.jenkinsci.plugins.scriptler.CentralScriptJsonCatalog',
            'hudson.plugins.buckminster.BuckminsterInstallation.BuckminsterInstaller'
    ]

    /**使用m2风格存储，将来有特别处理*/
    List<String> m2Style = [
            'sp.sd.flywayrunner.installation.FlywayInstaller',
            'ru.yandex.qatools.allure.jenkins.tools.AllureCommandlineInstaller',
            'hudson.tasks.Maven.MavenInstaller',
            'hudson.plugins.sonar.SonarRunnerInstaller',
            'hudson.plugins.flyway.FlywayInstaller'
    ]

    File pwd;

    public void saveToFile() {
        File pwd = new File("target/temp")
        String content = new File(pwd, "update-center.actual.json").text;
        RewriteUpdateSiteJson rewrite = new RewriteUpdateSiteJson((Map) new JsonSlurper().parseText(content));
        rewrite.setConnectionCheckUrl("http://www.baidu.com");
        rewrite.rewritePluginDownloadHost("http://127.0.0.1:8080");
        Map<String, Object> mapJson = rewrite.getJson();

        URL privateFile = getClass().getResource("/cert/jenkins-update-site-key.pem");
        URL publicFile = getClass().getResource("/cert/jenkins-update-site-cert.pem");

        def certs = KeyTool.readCertificatePem(publicFile)
        def key = KeyTool.readPrivateKey(privateFile);
        def keyPair = KeyTool.findKeyPair(certs, key);

        Signer signer = new Signer();
        signer.setCertificates(certs);
        signer.setKeyPair(keyPair);

        File cache2 = new File("target/cache2")

        File downloaded = new File('target/temp')

        //changeUpdateCenterJson(downloaded, signer, cache2)

        List<String> toBeDownload = toolsDownload;

        URL myUrl = new URL("http://192.168.1.1/")

        File updates = new File(downloaded, 'updates')
        File[] tools = updates.listFiles();

        File updatesDir = new File(cache2, "updates");
        updatesDir.mkdirs();
        tools.each { item ->
            if (item.isFile() && item.name.endsWith("json")) {
                String id = Files.getNameWithoutExtension(item.name);
                if (unsupport.contains(id)) {
                    println "unsupported ${id} current now.";
                } else {
                    if (toBeDownload.contains(id)) {
                        println "convert ${id}"
                        Map json = new JsonSlurper().parseText(
                                RewriteUpdateSiteJson.stripJsCallbackFunction(item.text));
                        RewriteToolInstallJson util = new RewriteToolInstallJson(json);
                        util.rewriteUrlSchemaAndHost(myUrl);
                        def newJson = JSONObject.fromObject(util.json);
                        signer.sign(newJson)
//                        new File(updatesDir, id + ".json").text = "downloadService.post('${id}'," + newJson.toString(2) + ")";
                        new File(updatesDir, id + ".json.html").text = toHtml(newJson);
                    }
                }
            }
        }
    }

    String toHtml(JSONObject json) {
        String template = """<!DOCTYPE html><html>
<head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8' /></head>
<body><script>window.onload = function () { window.parent.postMessage(JSON.stringify(${json.toString(4)}),'*'); };</script></body>
</html>"""
        return template;

    }

    public void changeUpdateCenterJson(File downloaded, Signer signer, File cache2) {
        def inputJson = JSONObject.fromObject(
                new JsonSlurper().parse(
                        new File(downloaded, "update-center.actual.json")))

        //replace host
        def tool = new RewriteUpdateSiteJson(inputJson)
        tool.setConnectionCheckUrl("http://www.baidu.com");
        tool.rewritePluginDownloadHost("http://127.0.0.1:8080")

        def newJson = tool.getJson();

        signer.sign(newJson)

        new File(cache2, "update-center.json").text = "updateCenter.post(\n" + newJson.toString() + ")";
    }
}
