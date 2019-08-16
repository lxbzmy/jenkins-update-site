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