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

package com.gxl.encryptdog.gui.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 加/解密执行结果,由ACL从执行结果上下文中提取
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
@Data
public class OperationResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8043293554330642963L;

    /**
     * 操作类型,ENCRYPT/DECRYPT
     */
    private String             operation;
    /**
     * 加密算法类型
     */
    private String             encryptAlgorithm;
    /**
     * 任务文件总数
     */
    private int                totalFiles;
    /**
     * 操作成功的文件个数
     */
    private int                successCount;
    /**
     * 操作失败的文件个数
     */
    private int                failedCount;
    /**
     * 成功率
     */
    private String             successRate;
    /**
     * 失败率
     */
    private String             failedRate;
    /**
     * 总耗时
     */
    private String             timeConsuming;
    /**
     * 各文件执行结果
     */
    private List<FileResult>   fileResults = new ArrayList<>();

    /**
     * 单文件执行结果
     */
    @Data
    public static class FileResult implements Serializable {
        @Serial
        private static final long serialVersionUID = 4553772724785200797L;

        /**
         * 源文件全限定名
         */
        private String          sourceFile;
        /**
         * 目标文件全限定名
         */
        private String          targetFile;
        /**
         * 执行结果,SUCCESS/FAILED
         */
        private String          result;
        /**
         * 失败原因
         */
        private String          errorMsg;
        /**
         * 源文件大小
         */
        private String          sourceFileSize;
        /**
         * 目标文件大小
         */
        private String          targetFileSize;
    }
}
