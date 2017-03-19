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
    wget -x -i links.txt
    cd ..
    ;;
check)
    $bin diff
    ;;    
*)
    echo '参数：
pull: 从updates.jenkins-ci.org拉取update-center.json
fetch: 将本地update-center.json转换成可下载的链接
wget: 下载插件
check: 检查下载文件的sha1
'
    ;;
esac