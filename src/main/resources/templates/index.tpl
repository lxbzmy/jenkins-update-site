yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('Jenkins Update Site')
    }
    body {
        h1('Jenkins 插件镜像站')
        p('使用方法：')
        p {
            yield "下载"
            a(href:'get_cert', "cert")
            yield '放到 ${JENKINS_HOME}/update-center-rootCAs/'
        }
        p("在插件管理、高级中，将https://updates.jenkins.io替换为：http://${url}")

        h2('如何在新装jenkins时使用？')
        p("首先启动一次jenkins，等待完成工作目录创建")
        p("然后ctrl+c关闭，不必在web安装界面操作")
        pre("""cd ~/.jenkins
mkdir update-center-rootCAs
cd update-center-rootCAs
wget http://${url}/get_cert -O custom-update-center-cert.pem
cd ..
rm -r updates
sed  -i -e  's/https:\\/\\/updates.jenkins.io/http:\\/\\/${url}/' hudson.model.UpdateCenter.xml
""")
        p("java -jar jenkins.war")
        p("继续完成安装")
        p("如果url中IP地址不对，那么在启动时请添加--config.host=xxx.xxx.xxx.xxx")
        hr()
        p("LTS 三个版本号，weekly 两个版本号")
        ul {
            list.each{ item->
                li {
                    a(href:"http://${url}${item}", item)
                }
            }
        }
    }
}