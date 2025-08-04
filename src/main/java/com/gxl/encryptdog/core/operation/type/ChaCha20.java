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
 * ChaCha20算法加密标记接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/29 15:34
 */
public interface ChaCha20 {
    /**
     * 加密算法名称
     */
    String ALGORITHM_NAME     = EncryptTypeEnum.CHACHA20.getAlgorithmName();

    /**
     * AES和3DES是分组加密算法，需要加密模式(CBC)+填充(PKCS5Padding)
     * ChaCha20是流加密算法，不分组、不需要填充，因此不需要额外指定模式和填充方式
     */
    String CIPHER_ALGORITHM   = ALGORITHM_NAME;

    /**
     * 基于PBKDF2算法使用密码派生函数
     */
    String KEY_DERIVATION     = "PBKDF2WithHmacSHA256";

    /**
     * chacha20要求IV向量的长度必须是12bytes(96bit)
     * The nonce is exactly 96 bits (12 bytes) long.
     */
    int    IV_LENGTH          = 12;

    /**
     * 密钥长度256bit
     */
    int    KEY_LENGTH         = 256;

    /**
     * PBKDF2的迭代次数
     */
    int    ITERATION_COUNT    = 100_000;
    /**
     * ChaCha20-Poly1305的认证标签长度
     */
    int    AUTHENTICATION_TAG = 128;
}
