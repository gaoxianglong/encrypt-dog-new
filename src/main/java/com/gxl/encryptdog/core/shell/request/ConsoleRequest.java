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

package com.gxl.encryptdog.core.shell.request;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import lombok.Data;
import picocli.CommandLine;

/**
 * 控制台命令请求参数
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 16:46
 */
@Data
public class ConsoleRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1743917858617456728L;
    /**
     * 需要加/解密的目标文件
     */
    @CommandLine.Option(names = { "-s", "--source-file" }, // 命令
            paramLabel = "<source file>", // 标签
            required = true, // 必填参数
            split = ",", // 分隔符
            arity = "1..*", // 多参数
            description = "Target files that need to be encrypt and decrypt,Wildcards are supported."// 注释
    )
    private List<String>      sourceFiles;

    /**
     * 加/解密内容的转储目录,非必填选项，缺省存储在原目录下
     */
    @CommandLine.Option(names = { "-t",
                                  "--target-path" }, paramLabel = "<storage path>", description = "The storage path after the operation is stored in the original path by default.")
    private String            targetPath;

    /**
     * 秘钥,采用密码选项，不在控制台回显密码
     */
    @CommandLine.Option(names = { "-k",
                                  "--secret-key" }, paramLabel = "<secret key>", required = true, description = "Both encrypt and decrypt require the same secret key", interactive = true)
    private char[]            secretKey;

    /**
     * true为加密,false为解密
     */
    @CommandLine.Option(names = { "-e", "--encrypt" }, description = "The default is decryption mode.")
    private boolean           isEncrypt;

    /**
     * 加/解密操作结束后是否删除源文件
     */
    @CommandLine.Option(names = { "-d", "--delete" }, description = "The source file is not deleted after the default operation.")
    private boolean           isDelete;

    /**
     * 仅限加/解密操作在同一台物理设备上,最高安全性
     */
    @CommandLine.Option(names = { "-o",
                                  "--only-local" }, description = "Encryption and decryption operations can only be performed on the same physical device.Only Apple Mac is supported")
    private boolean           isOnlyLocal;

    /**
     * 加/解密算法类型,缺省为DESede
     */
    @CommandLine.Option(names = { "-a", "--encrypt-algorithm" }, description = "Set the algorithm type to use DESede encryption algorithm by default.")
    private String            encryptAlgorithm = EncryptTypeEnum.TRIPLE_DES.getAlgorithmType();

    /**
     * 获取ConsoleRequest实例
     * @return
     */
    public ConsoleRequest getConsoleRequest() {
        return this;
    }
}
