package cn.devit.tools.jenkins

import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Hex
import org.junit.Before
import org.junit.Test

import java.util.regex.Pattern

/**
 *
 * <p>
 *
 * @author lxb
 */
class JenkinsMirrorDemo {

    final File dir = new File("cache");

    /**
     * update-center.json 带上版本号查询
     */
    String update_center_json_url = "http://updates.jenkins-ci.org/update-center.json?id=default&version=2.32.3";


    File downloadDir;
    URL point;

    @Before()
    public void setup() {
        point = new URL(update_center_json_url);
        //
        downloadDir = new File(dir, point.getHost());
        downloadDir.mkdirs();
    }

    @Test
    def void "第一步，检查文件夹，并下载update-center.json"() {
        String content = point.getText("utf-8");
        content = content.replace('http://www.google.com/', 'http://www.baidu.com')

        File file = new File(downloadDir, point.getPath());
        file.text = content;

        File actualJson = new File(downloadDir, point.getPath().replace(".json", ".actual.json"))
        //update-center.json实际上是一个jsonp，第二行是一个完整的json。
        actualJson.text = content.readLines()[1];

    }

    @Test
    def void "第二步，从JSON中生成文件列表和sha1"() {

        File update_center_json = new File(downloadDir, "update-center.actual.json");

        def json = new JsonSlurper().parse(update_center_json)

        File urls = new File(dir, "plugins.txt")
        File hashes = new File(dir, "sha1.txt")

        //第一行是新版jenkins的下载地址
        urls.text = json.core.url
        hashes.text = from_base64_to_sha1hex(json.core.sha1) + "  " + json.core.url.replace("http://", "")

        //剩下的是插件的地址和sha1
        Collection<Object> plugins = (json.plugins.values());
        hashes << "\n"
        hashes << plugins.collect({ plugin ->
            URL url = new URL(plugin.url);
            return from_base64_to_sha1hex(plugin.sha1) + "  " + url.host + url.path
        })
                .join("\n")
        urls << "\n"
        urls << plugins*.url.join("\n")

    }

    public String from_base64_to_sha1hex(base64EncodedSha1) {
        return Hex.encodeHexString(Base64.decoder.decode(base64EncodedSha1))
    }

    def void '第三步，使用wget下载所有连接'() {
        '''
        cd cache
 wget --directory-prefix=updates.jenkins-ci.org/ --cut-dirs=0 -x -i plugins.txt
有用的参数：
-nc, --no-clobber 不要重新下载
--unlink 覆盖旧文件
--tries=NUMBER 下载失败重试次数，0：无限制
'''
    }

    @Test
    def void sha1校验删除下载成功的url() {

        Pattern re_sum = ~'^\\w+\\s+'
        Pattern patternSha1CheckOk = ~': OK$';
        Pattern sha1CheckFailed = ~': FAILED$';

        File hashes = new File(dir, "sha1.txt");
        List<String> lines = "shasum -c ${hashes.getAbsolutePath()}".execute([], dir).text.readLines();
        int errorCount = 0;
        List<String> breakFiles = []
        lines.each { String line ->
            if (line.startsWith("shasum: ")) {
                return;
            }
            if (sha1CheckFailed.matcher(line).find()) {
                errorCount++;
                breakFiles.add(line.split(":")[0]);
            }
            //TODO 这种逻辑并不安全，如果前面调用有异常，或者新的未知输出的话，不能捕获。
        }

        List<String> breakHashes = []


        hashes.readLines().each { line ->
            for (String file in breakFiles) {
                if (line.indexOf(file) > 0) {
                    breakHashes.add(line)
                }
            }
        }

        if (breakHashes.isEmpty()) {
            println "All files download success."
        } else {
            println "${breakFiles.size()} files broken."
        }
        File links = new File(dir, "plugins.txt");
        links.text = breakFiles.collect({ noSchemaUrl ->
            return point.protocol+"://"+ noSchemaUrl;
        }).join("\n")
        hashes.text = breakHashes.join("\n")
    }

    @Test
    def void "第四步校验sha1，删除plugins.txt中下载成功的文件url"() {
        //TODO 校验文件的sha1值如果下载成功则删除它在plugins.txt中的记录
        File links = new File(dir, "plugins.txt");
        File hashs = new File(dir, "sha1.txt");
        List<String> expectHashs = hashs.readLines();

        int i = 0;
        List<String> breakFiles = [];
        List<String> breakHashes = [];
        links.eachLine { line ->
            URL url = new URL(line);
            File localFile = new File(downloadDir, url.getPath());
            String expect = expectHashs.remove(0)
            if (!localFile.exists()) {
                breakFiles << url.toString()
                breakHashes << expect;
                println "$url download failed."
                return;
            }
            if (sha1(localFile) != expect) {
                localFile.delete();

                breakFiles << url.toString()
                breakHashes << expect;
                println "$url download failed(check sum miss match)."
            }
        }
        if (breakFiles.isEmpty()) {
            println "文件下载全部完成。"
            links.text = "";
            hashs.text = "";
        } else {
            println "These file is break:"
            breakFiles.each { it -> println it }
            links.text = breakFiles.join("\n");
            hashs.text = breakHashes.join("\n");
        }
    }

    String sha1(File file) {
        return "shasum ${file.getAbsolutePath()}".execute().text.split(' ')[0];
    }

}
