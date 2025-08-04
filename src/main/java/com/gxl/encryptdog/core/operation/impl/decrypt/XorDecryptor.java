package com.gxl.encryptdog.core.operation.impl.decrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.DecryptException;
import com.gxl.encryptdog.base.error.HeaderParseException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractDecrypt;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.core.operation.type.Xor;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.spec.SecretKeySpec;

/**
 * XOR解密器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/2 22:42
 */
public class XorDecryptor extends AbstractDecrypt implements Xor {
    /**
     * 解密时缺省每次读取 10485760 bytes
     */
    public static final int DEFAULT_DECRYPT_CONTENT_SIZE = 0xa00000;

    public XorDecryptor(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * XOR算法不进行IV混淆
     * 
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        // 空重写
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

    @Override
    protected SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt) throws OperationException {
        return null;
    }

    /**
     * 数据解密操作
     * @param content 数据
     * @param secretKey 秘钥
     * @param iv IV向量
     * @param salt
     * @return
     * @throws DecryptException
     */
    @Override
    protected byte[] dataDecrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws DecryptException {
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
}