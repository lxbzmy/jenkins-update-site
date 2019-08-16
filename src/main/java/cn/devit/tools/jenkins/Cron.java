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
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * <p>
 *
 * @author lxb
 */
public class Cron {


    File pwd;

    File memo;

    public Cron() throws CertificateException {
        pwd = new File(".");
    }

    public void run() throws Exception {
        readKeypair();

        memo = new File(pwd, "memo.txt");

        //read index
        //create directorys
        //create temp directory
//        Resources.rea
        //read memo
        //pull update site index stable and latest
        //pull plugin-json
        //pull updates folder
        //save new plugin.json and sign
        // download each plugin and run check sum, write memo
        //pull tools list
        // save new tools json
        // download each tools zip and save to local file
        // rewrite to use local nexus repository
        // or put zip on nexus.
    }

    Signer signer;

    public void readKeypair() throws CertificateException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        URL pub = getClass().getResource("/cert/ca.pem");

    }


}
