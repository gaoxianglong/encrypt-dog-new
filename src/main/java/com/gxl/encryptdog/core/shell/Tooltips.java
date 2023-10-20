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

import com.gxl.encryptdog.base.error.ViewRenderException;
import com.gxl.encryptdog.core.shell.view.ViewRenderUtil;
import com.gxl.encryptdog.utils.ConsoleWriter;

import java.util.List;

/**
 * 控制台工具提示栏
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/2 10:42
 */
public class Tooltips {
    /**
     * 控制台输出源文件列表信息
     * @param sourceFilePaths
     * @throws ViewRenderException
     */
    public static void writeSourceFileList(List<String> sourceFilePaths) throws ViewRenderException {
        ConsoleWriter.write("Source file list:", true);
        // 输出渲染后的源文件列表
        ConsoleWriter.write(ViewRenderUtil.drawReminderInfo(sourceFilePaths), false);
        ConsoleWriter.write("Please confirm whether it is these files [Y/N]", true);
    }

    /**
     * 控制台输出删除源文件提示信息
     */
    public static void writeDeleteSourceFile() {
        ConsoleWriter.write("Please confirm whether the source file needs to be deleted？[Y/N]", true);
    }

    /**
     * 输出错误信息
     * @param msg
     */
    public static void writeErrorMsg(String msg) {
        writeErrorMsg(null, msg);
    }

    /**
     * 输出错误信息
     * @param sourceFile
     * @param msg
     */
    public static void writeErrorMsg(String sourceFile, String msg) {
        try {
            ConsoleWriter.write(ViewRenderUtil.drawErrorInfo("FAILED", sourceFile, msg), true);
        } catch (ViewRenderException e) {
            //...
        }
    }
}
