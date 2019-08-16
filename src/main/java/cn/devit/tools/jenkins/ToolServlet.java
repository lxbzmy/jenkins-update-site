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
package cn.devit.tools.jenkins;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import net.sf.json.JSONObject;

/**
 * Handler /update/xxx.yy.json.html
 * <p>
 *
 * @author lxb
 */
@Controller
@RequestMapping("/updates/")
public class ToolServlet {

    @Autowired
    Config config;

    @Autowired
    Signer signer;

    @RequestMapping(value = "{id}.json.html", produces = "text/html")
    @ResponseBody
    public String html(@PathVariable("id") String id) throws Exception {
        /*option 1 : json file to json html*/
        File dir = new File(config.getWorkingDir(), "updates");
        if (dir.exists()) {
            File pre = new File(dir, id + ".json");
            if (pre.exists()) {
                JSONObject json = JSONObject.fromObject(
                        UpdateCenterJsonUtil.stripJsCallbackFunction(
                                Files.toString(pre, Charsets.UTF_8)));
                signer.sign(json);
                return new JsonFileOutput().toHtml(json);
            }
        }
        /* TODO option2 : zip list to json html */
        throw new FileNotFoundException();
    }

}
