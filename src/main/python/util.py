#!/usr/bin/python
# -*- coding: utf-8 -*-
import base64
import binascii
import errno
import json
import os
import re
import sys
import urllib
from urlparse import urlparse;

# jenkins update-center.json的url
update_json_url = "http://updates.jenkins-ci.org/update-center.json?id=default&version=2.71"
# 本地工作目录
work_dir = os.path.abspath("cache")

url = urlparse(update_json_url)

schema = "http://"

# 第一步，准备好工作目录，下载update-center.json和update-center.actual.json
try:
    os.makedirs(work_dir)
except OSError, e:
    if e.errno != errno.EEXIST:
        raise


def links_file_loc():
    return os.path.join(work_dir, "plugins.txt")


def sha1_file_loc():
    return os.path.join(work_dir, "sha1.txt")


def update_center_actual_json_loc():
    return os.path.join(work_dir, "update-center.actual.json")


#
# 下载另存
#

def download_json():
    three_lines = urllib.urlopen(update_json_url).readlines()
    # replace connectionCheckUrl
    three_lines[1] = three_lines[1]

    with open(os.path.join(work_dir, "update-center.json"), 'w') as fp:
        fp.writelines(three_lines)
    with open(update_center_actual_json_loc(), 'w') as fp:
        fp.write(three_lines[1])


#
# 第二步，转换出文件下载列表和sha1 文件
#
def transform_links():
    def base64_hex(x): return binascii.hexlify(base64.b64decode(x))

    meta = json.load(open(update_center_actual_json_loc()))
    links = []
    hashes = []
    # jenkins.war
    links.append(meta['core']['url'])
    hashes.append(base64_hex(meta['core']['sha1']) + "  " + urlparse(meta['core']['url']).path.replace('/', '', 1))
    # plugins
    maps = meta['plugins']
    keys = maps.keys()
    keys.sort()
    plugins = [maps[key] for key in keys]
    for item in plugins:
        links.append(item['url'])
        # shasum 的check文件要求是每行一个sum值+两个空格+文件相对路径
        hashes.append(base64_hex(item['sha1']) + "  " + urlparse(item['url']).path.replace('/', '', 1))
    with open(links_file_loc(), 'w') as fp:
        fp.write("\n".join(links))
    with open(sha1_file_loc(), 'w') as fp:
        fp.write("\n".join(hashes))


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
                break_files.append(line.split(":")[0])
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
    "pull": download_json,
    "update": transform_links,
    "diff": check_sum
}
cmd = sys.argv[1] if len(sys.argv) > 1 else ""
try:
    switch[cmd]()
except KeyError as e:
    print "argument: pull|fetch|diff"
    sys.exit(1)
