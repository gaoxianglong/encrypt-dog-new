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

import com.google.common.base.Charsets;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.EncryptException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * TripleDes算法加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:35
 */
public class TripleDesEecrypt extends AbstractEncrypt {
    /**
     * 加密算法名称
     */
    private static final String ALGORITHM_TYPE               = EncryptTypeEnum.TRIPLE_DES.getAlgorithmType();

    /**
     * 加密时缺省每次读取10MB
     */
    private static final int    DEFAULT_ENCRYPT_CONTENT_SIZE = 0xa00000;

    /**
     * 加密算法名称/分组加密/分组填充
     */
    private static final String CIPHER_ALGORITHM             = String.format("%s/ECB/PKCS5Padding", ALGORITHM_TYPE);
    /**
     * SecureRandom使用SHA1PRNG加密算法
     */
    private static final String SECURE_RANDOM_ALGORITHM      = "SHA1PRNG";

    public TripleDesEecrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 获取分段操作容量
     *
     * @param capacity
     * @return
     */
    @Override
    public int getDefaultCapacity(long capacity) {
        return capacity <= DEFAULT_ENCRYPT_CONTENT_SIZE ? (int) capacity : DEFAULT_ENCRYPT_CONTENT_SIZE;
    }

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @return
     * @throws EncryptException
     */
    @Override
    public byte[] dataEncrypt(byte[] content, char[] secretKey) throws EncryptException {
        try {
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(secretKey));
            // 执行数据加密
            var data = cipher.doFinal(content);
            // 对加密结果进行base64编码
            return Utils.toBase64Encode(data).getBytes(Charsets.UTF_8);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 返回秘钥器
     * @param secretKey
     * @return
     * @throws EncryptException
     */
    private SecretKeySpec getSecretKeySpec(char[] secretKey) throws EncryptException {
        try {
            var secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
            secureRandom.setSeed(Utils.chars2Bytes(secretKey));
            var kg = KeyGenerator.getInstance(ALGORITHM_TYPE);
            kg.init(secureRandom);
            var generateKey = kg.generateKey();

            // 当秘钥不足192bit时会自动补全,超出则截取前192bit数据
            return new SecretKeySpec(generateKey.getEncoded(), ALGORITHM_TYPE);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 是否支持
     * @return
     */
    @Override
    public boolean isSupport(String encryptAlgorithm, boolean isEncrypt) {
        // 校验是否是加密操作&算法是否存在&当前加密器是否支持
        return isEncrypt && EncryptTypeEnum.check(encryptAlgorithm) && ALGORITHM_TYPE.equalsIgnoreCase(encryptAlgorithm);
    }
}
