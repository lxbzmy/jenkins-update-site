#!/bin/sh

bin="python src/main/python/util.py"
case $1 in
pull)
    $bin pull
    ;;
update)
    $bin update
    ;;
wget)
    cd cache
    wget -x -nH -nc -i plugins.txt
    #-x 保持url中的目录结构
    #-nc 不重复下载
    #-i 指定一个文件，内容是一个url列表
    #--cut-dirs=0
    #-nH, --no-host-directories 创建文件夹的时候不要带上主机名字
    #--unlink 覆盖旧文件
    #--tries=NUMBER 下载失败重试次数，0：无限制
    cd ..
    ;;
check)
    $bin diff
    ;;
server)
    cd cache
    python -m SimpleHTTPServer
    ;;
*)
    echo '参数：
pull: 从updates.jenkins-ci.org拉取update-center.json
update: 将本地update-center.json转换成可下载的链接
wget: 下载插件
check: 检查下载文件的sha1
server: Simple HTTP Server
'
    ;;
esac