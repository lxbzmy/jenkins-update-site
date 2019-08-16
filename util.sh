#!/bin/sh

bin="python src/main/python/util.py"
case $1 in
wget)
    cd jenkins-update-site
    wget -x -nH -nc -i plugins.txt
    #-x 保持url中的目录结构
    #-nc 不重复下载
    #-i 指定一个文件，内容是一个url列表
    #--cut-dirs=0
    #-nH, --no-host-directories 创建文件夹的时候不要带上主机名字
    #--unlink 覆盖旧文件
    #--tries=NUMBER 下载失败重试次数，0：无限制
    echo "download tools"
    file="tools.txt"
    while IFS= read -r line
    do
      echo $line;
      #-c 继续下载未完成的文件（需要服务器支持particial-content)
      wget -x -nH -c -i "${line}.txt"
    done <"$file"
    cd ..
    ;;
check)
    $bin diff
    ;;
*)
    echo '参数：
wget: 下载插件
check: 检查下载文件的sha1，删除损坏的文件，之后可以再次wget
'
    ;;
esac