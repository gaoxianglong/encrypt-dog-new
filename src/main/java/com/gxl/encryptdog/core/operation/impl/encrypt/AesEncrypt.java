package com.gxl.encryptdog.core.operation.impl.encrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.EncryptException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.core.operation.type.Aes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * AES-256加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/8/30 15:50
 */
public class AesEncrypt extends AbstractEncrypt implements Aes {
    public AesEncrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @param iv
     * @return
     * @throws EncryptException
     */
    @Override
    public byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv) throws EncryptException {
        try {
            // 获取秘钥key
            var key = getGenerateKey(secretKey);
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            // 执行数据加密
            return cipher.doFinal(content);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 获取加密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.AES_ENCRYPT;
    }

    /**
     * 初始化向量IV
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void initVector(EncryptContext encryptContext) throws OperationException {
        try {
            // 生成128bit的随机IV
            var iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 向加/解密领域模型中添加IV
            encryptContext.getOperationVO().setIv(iv);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }

        // 调用父类initVector函数向文件头中写入IV
        super.initVector(encryptContext);
    }
}