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
import com.gxl.encryptdog.core.operation.type.Xor;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.spec.SecretKeySpec;

/**
 * XOR加密器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/8/30 15:42
 */
public class XorEncryptor extends AbstractEncrypt implements Xor {
    public XorEncryptor(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * XOR算法不进行IV混淆
     * 
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        // 空重写
    }

    /**
     * 获取加密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.XOR_ENCRYPT;
    }

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @param iv
     * @param salt
     * @return
     * @throws EncryptException
     */
    @Override
    protected byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws EncryptException {
        // 声明加密后的结果集
        var rlt = new byte[content.length];
        // 将密钥转换为字节数组
        var key = Utils.chars2Bytes(secretKey);

        for (var i = 0; i < content.length; i++) {
            // 通过异或运算对源数据进行混淆
            rlt[i] = (byte) (content[i] ^ key[i % key.length]);
        }
        return rlt;
    }

    @Override
    protected SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt) throws OperationException {
        return null;
    }
}