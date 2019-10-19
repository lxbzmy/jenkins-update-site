# Jenkins插件镜像工具

可以用来升级内网中的jenkins

## 功能

- 和官方更新站点功能一致
- 提前下载插件
- 提前下载jenkins.war
- 重新生成签名
- 清理过期插件

## 工作目录

所有下载好的文件都放在 ./jenkins-update-site里面

jenkins-update-site
- cache 从updates.jenkins-ci.org下载到的文件
- download 插件目录
- tools.txt 在其中写你要下载的工具的id
- plugins.txt 待下载的插件列表
- sha1.txt 检查下载文件
- *.*.*.txt 各个工具下载地址


## 安装需求

- jdk8   ( to run and cmopile java app )
- maven  ( to compile jar )
- wget   ( to download file )
- shasum ( to execute sha1sum )

 
## 使用方法

### 安装

    mvn package 

### 得到插件列表

    java -jar target/*.jar pull

将会连接到jenkins主站下载插件列表, cache文件夹中会下载好东西
   
### 生成下载清单

    java -jar target/*.jar update

将所有插件整理成文件下载清单，plugins.txt sha1.txt会保存待下载文件列表
  
### 下载插件

    sh util.sh wget
调用wget批量下载文件  

### 检查下载文件完整性

    sh util.sh check  

检查下载文件的完整性（通过sha1sum）,plugins.txt sha1.txt会删除下载完毕的文件，保留需要重新下载的文件。
 
重复执行`wget`和`check`直到所有文件下载成功。 

### 清理插件

    java -jar target/*.jar clean

清理过期的不在清单中的插件
  
### 启动http服务

    java -jar target/*.jar server
    
启动http服务，供jenkins下载插件用。

访问首页会给出操作指导

改端口

    java -jar target/*.jar server --server.port=8081

## 操作jenkins

1. 复制`certs/jenkins-update-site-cert.pem`到 `${JENKINS_HOME}/update-center-rootCAs/`
    
2. 打开jenkins，插件，advance，将 http://${host}:8080/update-center.json写到更新站点文本框，保存，立即检查

## 安装jenkins插件的其他方法：

除了网络直连，通过代理服务器连接以外还能通过以下方法更新插件：

人工下载 update-center.json，访问 http://updates.jenkins-ci.org/update-center.actual.json?id=default&version=2.121.1。

下载好文件之后，打开，将所有链接提取出来，使用wget下载好，放到任意http服务器上。

编辑json文件，将 jons['signature'] 删除，将所有url替换成你的http服务器地址。

将这个文件放在 ${JENKINS_HOME}/updates/default.json

重启jenkins，此时能够使用这个缓存的文件来下载插件。


## 参考资料

镜像站点： https://updates.jenkins-ci.org/

[官方的update site 源码](https://github.com/jenkins-infra/update-center2/blob/master/site/README.md)

[介绍update site 目录结构](https://github.com/jenkins-infra/update-center2/blob/master/site/LAYOUT.md)


### 插件下载原理

1. jenkins启动过程中会访问：*http://updates.jenkins-ci.org/update-center.json?id=default&version=2.32.3*
2. url中的版本号会将url 重定向到一个镜像服务器，并且url会根据版本是否stable而发生变化，
   例如302到：https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/stable-2.32/update-center.json
3. 这个json文件实际上是一个jsonp，里面记录了jenkins程序的新版本url，所有插件的新版本的url和sha1值
4. jenkins会下载从第三步得到的url，并解压安装

###Tools自动安装原理

tools的自动安装也是通过update site来进行的。
例如这个地址：https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/stable/updates/

以maven举例：

1. 每种build工具在设计时会继承自动安装能力，生成id：hudson.tasks.Maven.MavenInstaller
2. 服务启动时会尝试访问：https://updates.jenkins-ci.org/updates/hudson.tasks.Maven.MavenInstaller.json.html
3. 从html中取出json，里面记录了版本号和下载地址
4. 每当一个新的node要使用工具时会预先下载并解压（在pipeline脚本中还会提前设置path，以便shell中调用）

感谢[清华大学 TUNA 协会](url "https://mirrors.tuna.tsinghua.edu.cn/") 提供jenkins site mirror 