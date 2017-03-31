import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Field

import java.util.regex.Pattern

/**
 * 下载jenkins tools 设置中的各种工具的安装包，并且重新设置url
 * <p>
 *
 * @author lxb
 */
/**
 * 你Jenkins现在的版本号
 */
@Field jenkinsVersion = '2.32.3'
/*
 * 要下载的工具包列表，ID从插件中找，或者在https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/updates/找
 */
@Field tools = ['hudson.tasks.Ant.AntInstaller',
                'hudson.tools.JDKInstaller',
                'hudson.tasks.Maven.MavenInstaller',
                'hudson.plugins.sonar.SonarRunnerInstaller']

@Field String update_center_json_url = "http://updates.jenkins-ci.org/update-center.json?id=default&version=${jenkinsVersion}";

/**
 * 工作目录
 */
@Field File dir = new File("cache");

@Field point = new URL(update_center_json_url);
/**
 * Jenkins工作目录在工作目录的下一层
 */
@Field File jenkinsWorkDir = new File(dir);

URL urlTemplate(String id) {
    return new URL("http://updates.jenkins-ci.org/updates/${id}.json.html?id=${id}&version=${jenkinsVersion}");
}

//https://repo1.maven.org/maven2
//https://archive.apache.org/dist/
//https://repo.maven.apache.org/maven2
def downloadUrl() {

    List<URL> urls = tools.collect { id -> return urlTemplate(id) }
    urls.each { url ->
        println "Fetch $url."
        String content = url.getText('utf-8')
        new File(jenkinsWorkDir, url.path).text = content;
    }
}


List<Tool> stripVersions(List<Tool> list) {
    //TODO 机智的只下载稳定版本，patch 版本中只下载最新的，中间的去掉
    Pattern majorMinor = Pattern.compile("^(\\d+\\.\\d+)\\.\\d+\$")
    majorMinor.matcher("1.2").toString()
    Map<String, List<Tool>> groups = list.groupBy { item ->
        m = majorMinor.matcher(item.id);
        if (m.find()) {
            return m.group(1)
        }
        return item.id
    }
    List<Tool> stripedList = groups.collect { k, v ->
        return v[0]
    }
    return stripedList;
}

/**
 * 工具包的下载列表有好多种，这种最简单。
 *
 * @param json
 * @return
 */
boolean isSimpleUrlList(Object json) {
    return json?.list?.url != null
}

def update_links() {
    File dir = new File(jenkinsWorkDir, "updates");
    List<File> fileList = dir.listFiles();
    File links = new File(jenkinsWorkDir, "tools.txt")
    fileList.each {
        List<String> lines = it.readLines();
        String jsonString = lines[1..-2].join('');
        def json = new JsonSlurper().parseText(jsonString);
        if (isSimpleUrlList(json)) {
            List<Tool> list = json.list;
            links << list*.url.join("\n")
            //改写成本地URL，然后保存
            list.each({ entry ->
                entry.url = entry.url//.replaceFirst("https?://", "http://updates.jenkins-ci.org/")
            })
            jsonString = JsonOutput.prettyPrint(JsonOutput.toJson(json));
            it.text = lines[0] + '\n' + jsonString + '\n' + lines[-1]
        } else {
            //TODO
        }

    }
}

class Tool {
    String id;
    String name;
    String url;
}
'''
下载方法
cd cache/updates.jenkins-ci.org
wget -nc -nH -x -i ../tools.txt
'''

downloadUrl()
update_links()
