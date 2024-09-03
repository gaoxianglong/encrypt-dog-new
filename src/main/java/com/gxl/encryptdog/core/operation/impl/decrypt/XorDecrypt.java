package com.gxl.encryptdog.core.operation.impl.decrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.utils.Utils;

/**
 * XOR算法解密
 * 在-d模式下,如果秘钥错误也能够正常进行异或运算,但是会损坏源文件,慎用
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/2 22:42
 */
public class XorDecrypt extends AbstractDecrypt {
    /**
     * 加密算法名称
     */
    private static final String ALGORITHM_TYPE               = EncryptTypeEnum.XOR.getAlgorithmType();

    /**
     * 解密时缺省每次读取 10485760 bytes
     */
    public static final int     DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00000;

    public XorDecrypt(ObServerContext obServer) {
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
            var rlt = new byte[content.length];
            // 将密钥转换为字节数组
            var key = Utils.chars2Bytes(secretKey);
            for (var i = 0; i < content.length; i++) {
                // 异或解密
                rlt[i] = (byte) (content[i] ^ key[i % key.length]);
            }
            return rlt;
        } catch (Throwable e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 获取解密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.XOR_DECRYPT;
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
}