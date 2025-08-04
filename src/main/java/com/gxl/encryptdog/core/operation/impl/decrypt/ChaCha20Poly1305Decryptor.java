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
package com.gxl.encryptdog.core.operation.impl.decrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.core.operation.type.ChaCha20;

import javax.crypto.spec.SecretKeySpec;

/**
 * ChaCha20算法解密器,结合Poly1305防密文篡改校验
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/29 17:58
 */
public class ChaCha20Poly1305Decryptor extends AbstractDecrypt implements ChaCha20 {
    /**
     * 解密时缺省每次读取 10485776 bytes
     */
    public static final int DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00010;

    public ChaCha20Poly1305Decryptor(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 获取渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.CHACHA20_DECRYPT;
    }

    /**
     * 获取默认的读取数据块大小
     * @param capacity
     * @return
     */
    @Override
    public int getDefaultCapacity(long capacity) {
        return capacity <= DEFAULT_DECRYPT_CONTENT_SIZE ? (int) capacity : DEFAULT_DECRYPT_CONTENT_SIZE;
    }

    /**
     * 获取密钥
     * @param secretKey
     * @param salt
     * @return
     * @throws OperationException
     */
    @Override
    protected SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt) throws OperationException {
        return super.getGenerateKey(secretKey, salt, KEY_DERIVATION, ITERATION_COUNT, KEY_LENGTH, ALGORITHM_NAME);
    }

    /**
     * 执行数据解密操作
     * @param content   数据
     * @param secretKey 秘钥
     * @param iv        IV向量
     * @param salt
     * @return
     * @throws DecryptException
     */
    @Override
    protected byte[] dataDecrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws DecryptException {
        return super.dataDecrypt(content, secretKey, iv, salt, CIPHER_ALGORITHM);
    }
}