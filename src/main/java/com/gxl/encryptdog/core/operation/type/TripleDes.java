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
package com.gxl.encryptdog.core.operation.type;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;

/**
 * TripleDes算法加密标记接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/4 15:39
 */
public interface TripleDes {
    /**
     * 加密算法名称
     */
    String ALGORITHM_NAME   = EncryptTypeEnum.TRIPLE_DES.getAlgorithmName();

    /**
     * 加密算法名称/分组加密/分组填充
     */
    String CIPHER_ALGORITHM = String.format("%s/CBC/PKCS5Padding", ALGORITHM_NAME);

    /**
     * 基于PBKDF2算法使用密码派生函数
     */
    String KEY_DERIVATION   = "PBKDF2WithHmacSHA256";
    /**
     * 向量长度
     */
    int    IV_LENGTH        = 8;
    /**
     * 密钥长度192bit
     */
    int    KEY_LENGTH       = 192;

    /**
     * PBKDF2的迭代次数
     */
    int    ITERATION_COUNT  = 100_000;
}
