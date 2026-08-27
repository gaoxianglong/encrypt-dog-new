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
 * GUI表单数据模型,与终端命令参数一一对应
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
@Data
public class EncryptFormDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8835494071288198521L;

    /**
     * 需要加/解密的目标文件或目录(原始输入,支持目录递归解析),对应-s
     */
    private List<String>      sourceFilePaths   = new ArrayList<>();
    /**
     * true为加密,false为解密,对应-e
     */
    private boolean           isEncrypt         = true;
    /**
     * 秘钥,对应-k
     */
    private char[]            secretKey;
    /**
     * 确认秘钥,仅加密模式使用,对应终端的二次确认
     */
    private char[]            confirmSecretKey;
    /**
     * 加/解密算法类型,缺省为AES,对应-a
     */
    private String            encryptAlgorithm  = "AES";
    /**
     * 加/解密操作结束后是否删除源文件,对应-d
     */
    private boolean           isDelete;
    /**
     * 仅限加/解密操作在同一台物理设备上,对应-o
     */
    private boolean           isOnlyLocal;
    /**
     * 加/解密内容的转储目录,缺省存储在原目录下,对应-t
     */
    private String            targetPath;
}
