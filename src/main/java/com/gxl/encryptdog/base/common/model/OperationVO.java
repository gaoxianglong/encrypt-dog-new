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

package com.gxl.encryptdog.base.common.model;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import com.gxl.encryptdog.utils.Utils;
import com.gxl.encryptdog.utils.uuid.impl.SecretkeyWorker;
import lombok.Getter;
import lombok.ToString;

import java.io.*;
import java.util.Objects;

/**
 * 加/解密领域模型
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/2 14:13
 */
@ToString
@Getter
public class OperationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5939081010595585358L;

    /**
     * 源文件全限定名
     */
    private String            sourceFilePath;
    /**
     * 目标文件全限定名
     */
    private String            targetFilePath;
    /**
     * 是否是加密
     */
    private boolean           isEncrypt;
    /**
     * 是否删除源文件
     */
    private boolean           isDelete;
    /**
     * 是否仅限加/解密操作在同一台物理设备上
     */
    private boolean           isOnlyLocal;
    /**
     * 加解密算法类型
     */
    private String            encryptAlgorithm;
    /**
     * 秘钥
     */
    private char[]            secretKey;
    /**
     * 源秘钥,仅限于--only-local场景
     */
    private char[]            sourceSecretKey;
    /**
     * 源文件容量大小
     */
    private long              sourceFileCapacity;

    /**
     * 设置目标文件的全限定名
     * @param request
     * @param sourceFile
     */
    public void setTargetFile(ConsoleRequest request, String sourceFile) {
        // 解析源文件名称和目录
        var fileStructure = Utils.parseFileStructure(sourceFile);
        var sourceFileName = fileStructure.getFileName();
        var sourceFileDir = fileStructure.getFileDir();

        // 获取用户指定的目标目录
        var targetFileDir = request.getTargetPath();
        if (Objects.isNull(targetFileDir) || targetFileDir.isBlank()) {
            // 如果用户未指定,则使用源文件目录
            targetFileDir = sourceFileDir;
        }
        // 加密操作时源文件后缀拼接.dog
        if (request.isEncrypt()) {
            this.targetFilePath = String.format("%s%s%s%s", targetFileDir, Constants.FILE_SEPARATOR, sourceFileName, Constants.DEFAULT_SUFFIX);
        } else {
            // 解密操作时源文件去掉.dog后缀
            this.targetFilePath = String.format("%s%s%s", targetFileDir, Constants.FILE_SEPARATOR,
                sourceFileName.substring(0, sourceFileName.lastIndexOf(Constants.DEFAULT_SUFFIX)));
        }
        // 检查目标文件是否存在，如果已存在则变更目标文件名称避免数据覆盖
        this.targetFilePath = fileIsExists(getTargetFilePath());
    }

    /**
     * 检查目标文件是否存在，如果已存在则变更目标文件名称避免数据覆盖
     * @param targetFilePath
     * @return
     */
    private String fileIsExists(String targetFilePath) {
        var file = new File(targetFilePath);
        if (!file.exists()) {
            return targetFilePath;
        }
        var targetFileName = file.getName();
        var targetFileDir = file.getParent();
        var suffix = Utils.parseFileSuffix(targetFileName);
        // 格式为dir/fileName-时间戳.suffix
        return String.format("%s%s%s-%s%s", targetFileDir, Constants.FILE_SEPARATOR, targetFileName.substring(0, targetFileName.lastIndexOf(suffix)), System.nanoTime(), suffix);
    }

    /**
     * 获取秘钥key,如果开启了--only-local命令时,secretKey=真实秘钥
     * @param secretKey
     */
    public void setSecretKey(char[] secretKey) {
        if (isEncrypt() && isOnlyLocal()) {
            try {
                this.sourceSecretKey = secretKey;
                this.secretKey = new SecretkeyWorker().getId().toCharArray();
            } catch (OperationException e) {
                //...
            }
            return;
        }
        this.secretKey = secretKey;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public void setEncrypt(Boolean encrypt) {
        isEncrypt = encrypt;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public void setOnlyLocal(Boolean onlyLocal) {
        isOnlyLocal = onlyLocal;
    }

    public void setEncryptAlgorithm(String encryptAlgorithm) {
        this.encryptAlgorithm = encryptAlgorithm;
    }

    public void setSourceFileCapacity(long sourceFileCapacity) {
        this.sourceFileCapacity = sourceFileCapacity;
    }
}
