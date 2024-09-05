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
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.core.operation.type.TripleDes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * TripleDes加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:35
 */
public class TripleDesEncrypt extends AbstractEncrypt implements TripleDes {
    public TripleDesEncrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @param iv
     * @return
     * @throws EncryptException
     */
    @Override
    public byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv) throws EncryptException {
        try {
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getGenerateKey(secretKey), new IvParameterSpec(iv));
            // 执行数据加密
            return cipher.doFinal(content);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 初始化向量IV
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void initVector(EncryptContext encryptContext) throws OperationException {
        try {
            // 生成64bit的随机IV
            var iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 向加/解密领域模型中添加IV
            encryptContext.getOperationVO().setIv(iv);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }

        // 调用父类initVector函数向文件头中写入IV
        super.initVector(encryptContext);
    }

    /**
     * 获取加密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum._3DES_ENCRYPT;
    }
}
