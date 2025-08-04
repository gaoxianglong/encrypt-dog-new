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

import lombok.Getter;

/**
 * 加密类型枚举类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 13:30
 */
@Getter
public enum EncryptTypeEnum {
                             /**
                              * 3DES对称加密算法
                              */
                             TRIPLE_DES(0, "3DES", "DESede", "3DES对称加密算法"),

                             /**
                              * XOR异或加密算法,主要是做混淆,使用请注意
                              */
                             XOR(1, "XOR", "XOR", "XOR异或加密算法"),

                             /**
                              * AES对称加密算法,安全性高,速度快(约DESede的5倍),加解密时默认的算法
                              */
                             AES(2, "AES", "AES", "AES256对称加密算法"),

                             /**
                              * CHACHA20对称加密算法
                              * 在 ARM 架构下（尤其是没有 AES 硬件加速的设备），ChaCha20 通常比 AES 更快，两者安全性相当；
                              */
                             CHACHA20(3, "CHACHA20", "ChaCha20-Poly1305", "CHACHA20对称加密算法");

    private int    id;
    /**
     * 算法类型
     */
    private String algorithmType;
    /**
     * 算法名称,主要是设置Cipher.getInstance时使用
     */
    private String algorithmName;
    /**
     * 描述信息
     */
    private String desc;

    EncryptTypeEnum(int id, String algorithmType, String algorithmName, String desc) {
        this.id = id;
        this.algorithmType = algorithmType;
        this.algorithmName = algorithmName;
        this.desc = desc;
    }

    /**
     * 算法类型验证
     * @param name
     * @return
     */
    public static EncryptTypeEnum check(String name) {
        for (var typeEnum : EncryptTypeEnum.values()) {
            if (typeEnum.algorithmType.equalsIgnoreCase(name)) {
                return typeEnum;
            }
        }
        return null;
    }
}
