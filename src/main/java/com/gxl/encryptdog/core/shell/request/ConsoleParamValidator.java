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

import java.util.List;
import java.util.Objects;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.CapacityException;
import com.gxl.encryptdog.base.error.ValidateException;
import com.gxl.encryptdog.utils.Utils;

/**
 * 控制台参数校验器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 16:22
 */
public class ConsoleParamValidator {
    /**
     * 参数校验
     * @param request
     */
    public static void validate(ConsoleRequest request) throws ValidateException {
        var secretKey = request.getSecretKey();

        // 密码不能为空
        if (Objects.isNull(secretKey) || secretKey.length < 1) {
            throw new ValidateException("Secret-key cannot be empty");
        }

        // 密码长度校验
        if (secretKey.length < Constants.DEFAULT_PWD_LENGTH) {
            throw new ValidateException("The length of the secret key shall be at least 6 digits");
        }

        // pwd double check
        doubleCheckSecretKey(request);
    }

    /**
     * 源文件后缀校验,过滤掉非正常后缀
     * @param files
     * @param isEncrypt
     */
    public static void suffixValidate(List<String> files, boolean isEncrypt) {
        var iterator = files.iterator();
        while (iterator.hasNext()) {
            var sourceFile = iterator.next();
            var suffix = Utils.parseFileSuffix(sourceFile);
            // 加密操作文件后缀不能是.dog
            if (isEncrypt) {
                if (suffix.equalsIgnoreCase(Constants.DEFAULT_SUFFIX)) {
                    iterator.remove();
                }
                continue;
            }
            // 解密操作文件后缀必须是.dog
            if (!suffix.equalsIgnoreCase(Constants.DEFAULT_SUFFIX)) {
                iterator.remove();
            }
        }
    }

    /**
     * 源文件容量验证
     * @param sourceFilePath
     * @return
     * @throws CapacityException
     */
    public static void capacityValidate(String sourceFilePath) throws CapacityException {
        // 获取文件总大小
        var sourceFileSize = Utils.getFileCapacity(sourceFilePath);
        if (sourceFileSize < 1) {
            throw new CapacityException("There is nothing in the target file");
        }
    }

    /**
     * pwd double check
     * @param request
     * @throws ValidateException
     */
    private static void doubleCheckSecretKey(ConsoleRequest request) throws ValidateException {
        // 解密不验证
        if (!request.isEncrypt()) {
            return;
        }

        // 非console环境不验证,否则会异常
        var console = System.console();
        if (Objects.isNull(console)) {
            return;
        }

        var newSecretKey = console.readPassword("Enter the secret-key again: ");
        if (Objects.isNull(newSecretKey) || !(Objects.equals(new String(request.getSecretKey()), new String(newSecretKey)))) {
            throw new ValidateException("The two secret-key do not match");
        }
    }
}
