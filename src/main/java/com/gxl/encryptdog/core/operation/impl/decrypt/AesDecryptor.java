package com.gxl.encryptdog.core.operation.impl.decrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.core.operation.type.Aes;

import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256解密器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/3 17:00
 */
public class AesDecryptor extends AbstractDecrypt implements Aes {
    /**
     * 解密时缺省每次读取 10485776 bytes
     */
    public static final int DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00010;

    public AesDecryptor(ObServerContext obServer) {
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
        return capacity <= DEFAULT_DECRYPT_CONTENT_SIZE ? (int) capacity : DEFAULT_DECRYPT_CONTENT_SIZE;
    }

    /**
     * 获取加密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.AES_DECRYPT;
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
        return super.getGenerateKey(secretKey, salt,
            // 基于PBKDF2算法使用密码派生函数
            KEY_DERIVATION,
            // PBKDF2的迭代次数
            ITERATION_COUNT,
            // 密钥长度
            KEY_LENGTH,
            // 加密算法名称
            ALGORITHM_NAME);
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
