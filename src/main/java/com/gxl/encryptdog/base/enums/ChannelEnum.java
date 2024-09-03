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
                         AES_DECRYPT(6, false, EncryptTypeEnum.AES.getAlgorithmType(), "AES算法解密");

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
