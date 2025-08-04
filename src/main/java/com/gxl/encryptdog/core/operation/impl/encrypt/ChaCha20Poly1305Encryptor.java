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
package com.gxl.encryptdog.core.operation.impl.encrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.EncryptException;
import com.gxl.encryptdog.base.error.HeaderParseException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.core.operation.type.ChaCha20;

import javax.crypto.spec.SecretKeySpec;

/**
 * ChaCha20加密器,结合Poly1305防密文篡改校验
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/29 15:25
 */
public class ChaCha20Poly1305Encryptor extends AbstractEncrypt implements ChaCha20 {
    public ChaCha20Poly1305Encryptor(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 生成IV向量
     * 
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        // 生成12bytes的随机IV, ChaCha20算法的IV长度必须是12bytes
        createVector(IV_LENGTH, encryptContext);

        // 调用父类initVector函数向文件头中写入IV
        super.parseVector(encryptContext);
    }

    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.CHACHA20_ENCRYPT;
    }

    /**
     * 数据加密操作
     * @param content   待加密内容
     * @param secretKey 用户输入明文密码
     * @param iv        IV向量
     * @param salt      盐值
     * @return
     * @throws EncryptException
     */
    @Override
    protected byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws EncryptException {
        return super.dataEncrypt(content, secretKey, iv, salt, CIPHER_ALGORITHM);
    }

    /**
     * 获取密钥
     * @param secretKey 用户输入明文密码
     * @param salt      盐值
     * @return
     * @throws OperationException
     */
    @Override
    protected SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt) throws OperationException {
        return super.getGenerateKey(secretKey, salt,
            // 基于PBKDF2算法使用密码派生函数
            KEY_DERIVATION,
            // PBKDF2的迭代次数
            ITERATION_COUNT,
            // 密钥长度
            KEY_LENGTH,
            // 加密算法名称
            ALGORITHM_NAME);
    }
}
