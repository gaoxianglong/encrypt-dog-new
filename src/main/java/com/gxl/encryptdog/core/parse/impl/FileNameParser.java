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

package com.gxl.encryptdog.core.parse.impl;

import com.gxl.encryptdog.base.error.CapacityException;
import com.gxl.encryptdog.base.error.ParseException;
import com.gxl.encryptdog.base.error.ValidateException;
import com.gxl.encryptdog.core.parse.Parser;
import com.gxl.encryptdog.core.shell.request.ConsoleParamValidator;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import com.gxl.encryptdog.utils.Utils;

import java.io.File;
import java.util.*;

/**
 * 文件名称解析器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/1 10:05
 */
public class FileNameParser implements Parser {
    /**
     * 执行文件解析操作,如果是目录则解析目录下面所有的文件
     * @param request
     * @return
     * @throws ParseException
     * @throws CapacityException
     */
    @Override
    public List<String> parse(ConsoleRequest request) throws ParseException, CapacityException {
        // 解析规则文件
        var files = parseRule(request.getSourceFiles());

        // 源文件后缀校验
        ConsoleParamValidator.suffixValidate(files, request.isEncrypt());

        // 目标文件不能为null
        if (files.isEmpty()) {
            throw new ParseException("No files available");
        }

        for (var f : files) {
            // 文件容量验证
            ConsoleParamValidator.capacityValidate(f);
        }
        return files;
    }

    /**
     * 规则解析
     * @param sourceFilePaths
     * @return
     * @throws ParseException
     */
    private List<String> parseRule(List<String> sourceFilePaths) throws ParseException {
        var rlt = new ArrayList<String>();
        for (var path : sourceFilePaths) {
            var sourFile = new File(path);

            // 文件或者文件夹是否存在校验
            checkFileExist(sourFile);

            // 判断是否是文件
            if (sourFile.isFile()) {
                rlt.add(path);
                continue;
            }

            // 目录则解析目录下的所有文件
            parseFiles(sourFile, rlt);
        }
        return rlt;
    }

    /**
     * 文件是否存在校验
     * @param sourceFile
     * @throws ParseException
     */
    private void checkFileExist(File sourceFile) throws ParseException {
        if (!sourceFile.exists()) {
            var filePath = Utils.lengthShear(sourceFile.getPath(), 25);
            throw new ParseException(String.format("File or directory %s does not exist", filePath));
        }
    }

    /**
     * 解析目标目录以及子目录下的所有文件
     * @param sourceFile
     * @param list
     */
    private void parseFiles(File sourceFile, List<String> list) {
        for (var f : sourceFile.listFiles()) {
            if (f.isDirectory()) {
                parseFiles(f, list);
                continue;
            }
            list.add(f.getPath());
        }
    }
}
