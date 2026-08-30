package com.gxl.encryptdog.core.operation.impl.decrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.core.operation.type.Aes;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * AES-256解密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/3 17:00
 */
public class AesDecrypt extends AbstractDecrypt implements Aes {
    /**
     * 解密时缺省每次读取 10485776 bytes
     */
    public static final int DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00010;

    public AesDecrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 构建派生密钥,每文件仅派生一次
     * @param secretKey
     * @return
     * @throws OperationException
     */
    @Override
    protected SecretKey buildKey(char[] secretKey) throws OperationException {
        return getGenerateKey(secretKey);
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
     * 数据解密操作
     * @param content
     * @param secretKey
     * @param keySpec 派生密钥,每文件仅派生一次
     * @param iv
     * @return
     * @throws DecryptException
     */
    @Override
    protected byte[] dataDecrypt(byte[] content, char[] secretKey, SecretKey keySpec, byte[] iv) throws DecryptException {
        try {
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            // 执行数据解密
            return cipher.doFinal(content);
        } catch (Throwable e) {
            throw new DecryptException(e);
        }
    }
}
