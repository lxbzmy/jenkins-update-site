package cn.devit.tools.jenkins

/**
 *
 * <p>
 *
 * @author lxb
 */
class UpdateCenterJsonUtil {

    /**官方json文件带着callback，要先去了*/
    public static String stripJsCallbackFunction(String input) {
        return input.substring(input.indexOf("{"),
                input.lastIndexOf("}") + 1);
    }

}
