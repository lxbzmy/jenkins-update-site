/*
 * Copyright 2017-2019 lxb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2017-2019 lxb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.devit.tools.jenkins


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Pattern

/**
 * 检查sha1sum删除损坏的文件
 *
 * @author lxb
 */
public class CheckAndRmBroken {

    Logger logger = LoggerFactory.getLogger(CheckAndRmBroken.class);

    public void run(Config config) {
        logger.info("检查文件sha1 {}", config.getWorkingDir())
        List<String> result = "shasum -c sha1.txt".execute([],config.getWorkingDir()).text.readLines();
        /*# shasum 的输出格式
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
        */
        int error_count = 0;
        Pattern re_sum = Pattern.compile('^\\w+\\s+');
        Pattern pattern_ok = Pattern.compile(': OK')
        Pattern pattern_failed = Pattern.compile(": FAILED")
        def break_files = [];
        for(String line : result){
            if(!line.startsWith("shasum:")){
                if(pattern_failed.matcher(line).find()){
                    error_count += 1
                    def breakFile = line.split(":")[0];
                    break_files.add(breakFile)
                    logger.info("rm {}",breakFile);
                    try {
                        new File(config.getWorkingDir(),breakFile).delete();
                    } catch (FileNotFoundException e) {
                        //file not exists.
                    }
                }
            }
        }

        //重写sha1 和 下载列表
        def break_hashes = []
        if(break_files.isEmpty()){
            logger.info("All files download correct.");
        }else{
            logger.info("${break_files.size()} files break.")
            new File(config.getWorkingDir(),"sha1.txt").readLines().each {line ->
                break_files.each {b->
                    if(line.indexOf(b)>=0){
                        break_hashes.add(line);
                    }
                }
            }
        }
        new File(config.getWorkingDir(),"sha1.txt").withWriter {writer->
            break_hashes.each {line->
                writer.writeLine(line);
            }
        }
        new File(config.getWorkingDir(),"plugins.txt").withWriter {writer->
            break_files.each {line->
                writer.writeLine("http://updates.jenkins-ci.org/"+ line);
            }
        }
    }

}
