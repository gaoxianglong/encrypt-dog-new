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
package com.gxl.encryptdog.base.enums;

import lombok.Data;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加解密渠道枚举
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/2 20:46
 */
@Getter
public enum ChannelEnum {
                         /**
                          * 3des加密
                          */
                         _3DES_ENCRYPT(1, true, EncryptTypeEnum.TRIPLE_DES.getAlgorithmType(), "3DES算法加密"),
                         /**
                          * 3des解密
                          */
                         _3DES_DECRYPT(2, false, EncryptTypeEnum.TRIPLE_DES.getAlgorithmType(), "3des算法解密"),

                         /**
                          * XOR混淆加密
                          */
                         XOR_ENCRYPT(3, true, EncryptTypeEnum.XOR.getAlgorithmType(), "XOR混淆加密"),
                         /**
                          * XOR混淆解密
                          */
                         XOR_DECRYPT(4, false, EncryptTypeEnum.XOR.getAlgorithmType(), "XOR混淆解密"),

                         /**
                          * AES-256算法加密
                          */
                         AES_ENCRYPT(5, true, EncryptTypeEnum.AES.getAlgorithmType(), "AES算法加密"),
                         /**
                          * AES-256算法解密
                          */
                         AES_DECRYPT(6, false, EncryptTypeEnum.AES.getAlgorithmType(), "AES算法解密"),

                         /**
                          * ChaCha20算法加密
                          */
                         CHACHA20_ENCRYPT(7, true, EncryptTypeEnum.CHACHA20.getAlgorithmType(), "ChaCha20算法加密"),
                         /**
                          * ChaCha20算法解密
                          */
                         CHACHA20_DECRYPT(8, false, EncryptTypeEnum.CHACHA20.getAlgorithmType(), "ChaCha20算法解密");

    /**
     * id
     */
    private int                                 id;
    /**
     * 是否是加密,加密为true，解密为false
     */
    private boolean                             isEncrypt;
    /**
     * 算法类型
     */
    private String                              algorithmType;
    /**
     * 描述信息
     */
    private String                              desc;

    private static Map<ChannelKey, ChannelEnum> maps = new ConcurrentHashMap<>() {
                                                         {
                                                             for (var c : ChannelEnum.values()) {
                                                                 put(new ChannelKey(c.isEncrypt, c.getAlgorithmType().toUpperCase()), c);
                                                             }
                                                         }
                                                     };

    ChannelEnum(int id, boolean isEncrypt, String algorithmType, String desc) {
        this.id = id;
        this.isEncrypt = isEncrypt;
        this.algorithmType = algorithmType;
        this.desc = desc;
    }

    /**
     * 根据isEncrypt和algorithmType获取指定渠道
     * @param isEncrypt
     * @param algorithmType
     * @return
     */
    public static ChannelEnum getChannel(boolean isEncrypt, String algorithmType) {
        return maps.get(new ChannelKey(isEncrypt, algorithmType.toUpperCase()));
    }

    @Data
    static class ChannelKey {
        private boolean isEncrypt;
        private String  algorithmType;

        private ChannelKey(boolean isEncrypt, String algorithmType) {
            this.isEncrypt = isEncrypt;
            this.algorithmType = algorithmType;
        }
    }
}
