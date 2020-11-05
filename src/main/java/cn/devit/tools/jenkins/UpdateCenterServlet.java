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

import static org.springframework.util.StringUtils.hasText;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import cn.devit.tools.jenkins.util.ComparableVersion;
import groovy.json.JsonSlurper;
import net.sf.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UpdateCenterServlet {
    
    static final Logger logger = LoggerFactory
            .getLogger(UpdateCenterServlet.class);

    @Autowired
    Config config;

    @Autowired
    ServerProperties server;

    @RequestMapping("/")
    public ModelAndView index(){
        final ModelAndView view = new ModelAndView("index");
        view.addObject("url",getHost());

        File[] files = new File(config.getDownloadDir(),"war").listFiles(File::isDirectory);
        List<String> path = new ArrayList<>();
        if(files!=null){
            for(File f:files ){
                path.add("/download/war/"+f.getName()+"/jenkins.war");
            }
        }
        view.addObject("list",path);

        return view;
    }

    @RequestMapping("/get_cert")
    @ResponseBody
    public FileSystemResource cert(){
        return new FileSystemResource(
                new File(config.getCertificateDir(),
                        "jenkins-update-site-cert.pem"));
    }


    @RequestMapping("/update-center.json")
    @ResponseBody
    public String updateCenterJson(
            @RequestParam(required = false, defaultValue = "default") String id,
            @RequestParam(required = false) String version) {
        
        if (hasText(version)) {
            File file = selectVersion(version);
            return readAndReSign(file);
        }
        return readAndReSign(
                new File(config.getCacheDir(), "current/update-center.actual.json"));
    }

    @RequestMapping("/latestCore.txt")
    public String latestCore(
            @RequestParam(required = false, defaultValue = "default") String id,
            @RequestParam(required = false) String version) {
        return forward(version, "latestCore.txt");
    }

    @RequestMapping("/plugin-documentation-urls.json")
    public String documentation(
            @RequestParam(required = false, defaultValue = "default") String id,
            @RequestParam(required = false) String version) {
        return forward(version, "plugin-documentation-urls.json");
    }

    String forward(String version, String fileName) {
        if (hasText(version)) {
            File file = selectVersion(version);
            return "forward:/cache/" + file.getName() + "/" + fileName;
        }
        return "forward:/cache/current/" + fileName;
    }

    public String getHost() {
        if (server.getPort() != null) {
            return config.getHost() + ":" + server.getPort();
        }
        return config.getHost() + ":8080";
    }

//    @Autowired
//    ApplicationContext applicationContext;

    @RequestMapping(value = "/config", produces = "text/plain")
    @ResponseBody
    public String getConfig() {
//        applicationContext.getEmbeddedServletContainer().getPort();
        if (server.getPort() != null) {
            return config.getHost() + ":" + server.getPort();
        }
        return config.getHost() + ":8080";
    }

    @Autowired
    Signer signer;

    public String readAndReSign(File file) {
        File fileActual;
        if(file.isDirectory()){
            fileActual = new File(file, "update-center.actual.json");
        }else{
            fileActual = new File(file.getParentFile(), "update-center.actual.json");
        }
        logger.info("return {}",fileActual);
        String content;
        try {
            content = Files.toString(fileActual,Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RewriteUpdateSiteJson rewrite = new RewriteUpdateSiteJson(
                (Map) new JsonSlurper().parseText(content));
        rewrite.setConnectionCheckUrl("http://www.baidu.com");
        rewrite.rewritePluginDownloadHost("http://" + getHost());
        Map<String, Object> mapJson = rewrite.getJson();
        try {
            JSONObject jsonObject = JSONObject.fromObject(mapJson);
            signer.sign(jsonObject);
            return jsonObject.toString();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    Pattern dev = Pattern.compile("\\d+\\.\\d+");

    Pattern lts = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    public String getHostAndPort() {
        InetAddress address = server.getAddress();
        System.out.println(address);
        return address.toString();
    }

    /**
     * see: https://github.com/jenkins-infra/update-center2/tree/master/site
     */
    @NonNull
    private File selectVersion(String version) {
        File[] files = config.getCacheDir().listFiles(File::isDirectory);
        if(files==null){
            throw new RuntimeException("没有匹配到合适的版本");
        }

        Matcher matcher = dev.matcher(version);
        if (matcher.matches()) {
            List<ComparableVersion> list = new ArrayList<>();
            for (File file : files) {
                if (dev.matcher(file.getName()).matches()) {
                    list.add(new ComparableVersion(file.getName()));
                }else if (file.getName().startsWith("dynamic-stable")) {
                    //skip
                }else if (file.getName().startsWith("dynamic-")) {
                    list.add(new ComparableVersion(
                        file.getName().replace("dynamic-", "")));
                }
            }
            Collections.sort(list);
            ComparableVersion inputVersion = new ComparableVersion(version);
            String select = null;
            for (ComparableVersion branch : list) {
                if (inputVersion.compareTo(branch) > 0) {
                    //branch < input version ,next.
                } else {
                    select = branch.toString();
                    break;
                }
            }
            if (select == null) {
                select = "current";
            }
            return new File(config.getCacheDir(), select);
        }
        //LTS selection.
        matcher = lts.matcher(version);
        if (matcher.matches()) {
            List<ComparableVersion> list = new ArrayList<>();
            for (File file : files) {
                if (file.getName().startsWith("stable-")) {
                    list.add(new ComparableVersion(
                            file.getName().replace("stable-", "")));
                }else if(file.getName().startsWith("dynamic-stable-")){
                    list.add(new ComparableVersion(
                        file.getName().replace("dynamic-stable-", "")));
                }
            }
            Collections.sort(list);
//            Collections.reverse(list);
            ComparableVersion inputVersion = new ComparableVersion(version);
            String selection = null;
            for (ComparableVersion branch : list) {
                if (inputVersion.compareTo(branch) >= 0) {
                    selection = branch.toString();
                }
            }
            if(selection==null){
                return new File(config.getCacheDir(), "stable");
            }
            return new File(config.getCacheDir(), "dynamic-stable-"+selection);
        }
        return new File(config.getCacheDir(), "current");
    }

}
