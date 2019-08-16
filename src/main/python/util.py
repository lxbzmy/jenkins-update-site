#!/usr/bin/python
# -*- coding: utf-8 -*-
import errno
import os
import re
import sys

# 本地工作目录
work_dir = os.path.abspath("jenkins-update-site")

def links_file_loc():
    return os.path.join(work_dir, "plugins.txt")


def sha1_file_loc():
    return os.path.join(work_dir, "sha1.txt")


# todo check sum
#
# 第三步，使用wget下载所有链接
#
"""cd cache
wget  -x -i plugins.txt """


#
# 第四步，执行sha1检查，保留没有下载成功的文件链接和sha1
#
def check_sum():
    os.chdir(work_dir)
    result = os.popen("shasum -c sha1.txt").readlines()
    # shasum 的输出格式
    # 每行结果的格式是：文件路径冒号空格结果
    # shasum: 开头的信息表示异常或者不匹配
    # 有三种case
    # 匹配成功：
    # path: OK
    # 匹配失败：
    # plugins.txt: FAILED
    # 如果文件读取异常（不存在，没权限）
    # shasum: links1.txt:
    # links1.txt: FAILED open or read
    # 末尾的异常信息可能是
    # shasum: WARNING: 1 line is improperly formatted
    # shasum: WARNING: 1 listed file could not be read
    # shasum: WARNING: 1 computed checksum did NOT match
    error_count = 0;
    re_sum = re.compile('^\w+\s+')
    pattern_ok = re.compile(': OK')
    pattern_failed = re.compile(": FAILED")
    break_files = []
    for line in result:
        if not line.startswith("shasum: "):
            if pattern_failed.search(line):
                error_count += 1
                breakFile = line.split(":")[0];
                break_files.append(breakFile)
                #TODO os.delete
                print "rm " + breakFile
                os.remove(breakFile)
    break_hashes = []
    for line in open(sha1_file_loc(), 'r'):
        for b in break_files:
            if line.find(b) >= 0:
                break_hashes.append(line)
    if len(break_files) == 0:
        print "All files download correct."
    else:
        print str(len(break_files)) + " files break."
    with open(sha1_file_loc(), 'w') as fp:
        fp.writelines(break_hashes)
    with open(links_file_loc(), 'w') as fp:
        for line in break_files:
            fp.write(schema + url.hostname + "/" + line + "\n")


#
# 第五步，重复执行3，4直到全部下载完成。
#

switch = {
    "diff": check_sum
}
cmd = sys.argv[1] if len(sys.argv) > 1 else ""
try:
    switch[cmd]()
except KeyError as e:
    print "argument: pull|fetch|diff"
    sys.exit(1)
