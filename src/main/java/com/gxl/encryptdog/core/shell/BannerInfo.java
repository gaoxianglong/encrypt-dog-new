/*
 *
 *  * Copyright 2019-2119 gao_xianglong@sina.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.gxl.encryptdog.core.shell;

import java.io.IOException;
import java.util.Properties;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.ResourceException;
import com.gxl.encryptdog.utils.ConsoleWriter;
import com.gxl.encryptdog.utils.Utils;

/**
 * Banner信息
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 22:16
 */
public class BannerInfo {
    /**
     * 版本key
     */
    private static final String   KEY    = "project.version";

    /**
     * 缺省输出的banner信息
     */
    private static final String[] BANNER = { "   ____                       __  _           ___           ", "  / __/__  __________ _____  / /_(_)__  ___  / _ \\___  ___ _",
                                             " / _// _ \\/ __/ __/ // / _ \\/ __/ / _ \\/ _ \\/ // / _ \\/ _ `/",
                                             "/___/_//_/\\__/_/  \\_, / .__/\\__/_/\\___/_//_/____/\\___/\\_, / ", "                 /___/_/                             /___/  " };

    /**
     * 向控制台输出banner信息
     */
    public static void drawBanner() throws ResourceException {
        var warp = Constants.WRAP;
        var strBuilder = new StringBuilder();
        strBuilder.append(String.format("Welcome to %s", warp));
        for (var b : BANNER) {
            strBuilder.append(b + warp);
        }
        String version = null;
        try {
            // 获取当前项目的版本信息
            version = getProjectVersion();
        } catch (IOException e) {
            throw new ResourceException("Failed to start obtaining resource information", e);
        }
        strBuilder.append(String.format("\tversion: %s", version));
        // 输出banner信息后换2行
        ConsoleWriter.write(strBuilder.toString(), 0, 2);
    }

    /**
     * 获取当前项目的版本信息
     *
     * @return
     * @throws IOException
     */
    private static String getProjectVersion() throws IOException {
        var properties = new Properties();

        // 获取version.properties文件路径
        var path = Utils.getResourceStream(Constants.DEFAULT_CONFIG_PATH);

        // 加载version.properties文件
        properties.load(path);
        return properties.getProperty(KEY);
    }
}
