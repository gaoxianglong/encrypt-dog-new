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
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * TripleDes算法解密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:35
 */
public class TripleDesDecrypt extends AbstractDecrypt {
    /**
     * 加密算法名称
     */
    private static final String ALGORITHM_TYPE               = EncryptTypeEnum.TRIPLE_DES.getAlgorithmType();
    /**
     * 解密时缺省每次读取 10485768 bytes
     */
    public static final int     DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00008;
    /**
     * 加密算法名称/分组加密/分组填充
     */
    private static final String CIPHER_ALGORITHM             = String.format("%s/ECB/PKCS5Padding", ALGORITHM_TYPE);
    /**
     * SecureRandom使用SHA1PRNG加密算法
     */
    private static final String SECURE_RANDOM_ALGORITHM      = "SHA1PRNG";

    public TripleDesDecrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 数据解密操作
     * @param content
     * @param secretKey
     * @return
     * @throws DecryptException
     */
    @Override
    public byte[] dataDecrypt(byte[] content, char[] secretKey) throws DecryptException {
        try {
            var dc = Cipher.getInstance(CIPHER_ALGORITHM);
            dc.init(Cipher.DECRYPT_MODE, getSecretKeySpec(secretKey));
            return dc.doFinal(content);
        } catch (Throwable e) {
            throw new DecryptException("The key is incorrect,Try Again", e);
        }
    }

    /**
     * 获取分段操作容量
     *
     * @param capacity
     * @return
     */
    @Override
    public int getDefaultCapacity(long capacity) {
        return capacity <= DEFAULT_DECRYPT_CONTENT_SIZE ? (int) capacity : DEFAULT_DECRYPT_CONTENT_SIZE;
    }

    /**
     * 获取解密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum._3DES_DECRYPT;
    }

    /**
     * 返回秘钥器
     * @param secretKey
     * @return
     * @throws DecryptException
     */
    private SecretKeySpec getSecretKeySpec(char[] secretKey) throws DecryptException {
        try {
            var secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
            secureRandom.setSeed(Utils.chars2Bytes(secretKey));
            var kg = KeyGenerator.getInstance(ALGORITHM_TYPE);
            kg.init(secureRandom);
            var generateKey = kg.generateKey();

            // 当秘钥不足192bit时会自动补全,超出则截取前192bit数据
            return new SecretKeySpec(generateKey.getEncoded(), ALGORITHM_TYPE);
        } catch (Throwable e) {
            throw new DecryptException(e);
        }
    }
}
