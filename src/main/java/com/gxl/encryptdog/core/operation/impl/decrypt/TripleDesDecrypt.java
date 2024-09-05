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
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.core.operation.type.TripleDes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * TripleDes解密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:35
 */
public class TripleDesDecrypt extends AbstractDecrypt implements TripleDes {
    /**
     * 解密时缺省每次读取 10485768 bytes
     */
    public static final int DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00008;

    public TripleDesDecrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 数据解密操作
     * @param content
     * @param secretKey
     * @param iv
     * @return
     * @throws DecryptException
     */
    @Override
    public byte[] dataDecrypt(byte[] content, char[] secretKey, byte[] iv) throws DecryptException {
        try {
            var dc = Cipher.getInstance(CIPHER_ALGORITHM);
            dc.init(Cipher.DECRYPT_MODE, getGenerateKey(secretKey), new IvParameterSpec(iv));
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
}
