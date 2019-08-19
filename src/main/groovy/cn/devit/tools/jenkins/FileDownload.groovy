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

import com.google.common.io.Resources
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Open remote url save to file.
 * <p>
 *
 * @author lxb
 */
public class FileDownload {

    Logger logger = LoggerFactory.getLogger(FileDownload.classes);


    public void download(URL remote, File saveTo) {
        logger.info("downloading {}", remote)
        File temp = new File(saveTo.getParentFile(), saveTo.getName() + ".downloading");
        Resources.copy(remote, temp);
        Files.move(temp.toPath(),
                saveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}
