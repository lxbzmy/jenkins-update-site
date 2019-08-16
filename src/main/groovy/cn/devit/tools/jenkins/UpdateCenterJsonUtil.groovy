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

/**
 * @author lxb
 */
class UpdateCenterJsonUtil {

    /**官方json文件带着callback，要先去了*/
    public static String stripJsCallbackFunction(String input) {
        return input.substring(input.indexOf("{"),
                input.lastIndexOf("}") + 1);
    }

}
