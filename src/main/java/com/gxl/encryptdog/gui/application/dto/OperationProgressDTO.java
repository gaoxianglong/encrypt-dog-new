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
 * 加/解密执行进度快照,由ACL从执行结果上下文中提取
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
@Data
public class OperationProgressDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1568479010585152007L;

    /**
     * 操作类型,ENCRYPT/DECRYPT
     */
    private String            operation;
    /**
     * 加密算法类型
     */
    private String            encryptAlgorithm;
    /**
     * 任务文件总数
     */
    private int               totalFiles;
    /**
     * 各文件执行进度
     */
    private List<FileProgress> fileProgressList = new ArrayList<>();

    /**
     * 单文件进度信息
     */
    @Data
    public static class FileProgress implements Serializable {
        @Serial
        private static final long serialVersionUID = 5401983682322794248L;

        /**
         * 源文件全限定名
         */
        private String         sourceFile;
        /**
         * 执行进度,未开始为-
         */
        private String         progress;
        /**
         * 任务预计执行时间
         */
        private String         estimatedTime;
        /**
         * 源文件大小
         */
        private String         sourceFileSize;
        /**
         * 目标文件全限定名,解析完成后即有值
         */
        private String         targetFile;
        /**
         * 目标文件大小,文件处理完成后回填,处理中为-
         */
        private String         targetFileSize;
        /**
         * 执行结果,未开始为-
         */
        private String         result;
        /**
         * 任务执行状态,取核心EncryptStateEnum.state值(WAITING/RUNNING/FINISHED)
         */
        private String         state;
    }
}
