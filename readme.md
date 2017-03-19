# Jenkins插件镜像工具，可以用来升级内网中的jenkins

## 使用方法

### 批量下载插件
下载目录设置为：`cache/updates.jenkins-ci.org`

    #第一步，下载好update-center.json
    ./util.sh pull
    #第二步，从json中取出下载链接和sha1
    ./util.sh update
    #第三步，使用wget下载所有链接
    ./util.sh wget
    #第四步，检查sha1，从下载列表中去掉下载成功的链接
    ./util.sh check
    #第五步，重复3，4直到所有文件下载成功

以后每次更新升级的时候都可以从第一步开始。 
    
### 搭建Http Server

使用任意http 文件服务器，比如node http-server 创建一个http 文件服务器
将jenkins master机上的host文件中的updates.jenkins-ci.org指向你启动的服务器，注意要用默认的80端口

    cd cache/updates.jenkins-ci.org
    sudo python -m SimpleHTTPServer 80
    #其他http服务
    #npm install http-server
    #http-server
    #nginx/apach

在jenkins主机上，添加一行host解析，将updates.jenkins-ci.org指向你准备好的http服务器，要注意端口必须是80。


## Jenkins update site 工作原理

###插件下载

1. jenkins启动过程中会访问：*http://updates.jenkins-ci.org/update-center.json?id=default&version=2.32.3*
2. url中的版本号会将url 重定向到一个镜像服务器，并且url会根据版本是否stable而发生变化，
   例如302到：https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/stable-2.32/update-center.json
3. 这个json文件实际上是一个jsonp，里面记录了jenkins程序的新版本url，所有插件的新版本的url和sha1值
4. jenkins会下载从第三步得到的url，并解压安装

###Tools下载

除了插件以外，tools的更新也是通过update site来进行的。
例如这个地址：https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/stable/updates/

1. 每一种build工具会在update site中登记，例如maven
2. https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/stable/updates/hudson.tasks.Maven.MavenInstaller.json
3. 里面记录了每一个版本的maven从哪里下载
4. jenkins自己处理下载和解压
