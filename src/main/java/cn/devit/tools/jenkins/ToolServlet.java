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
