package com.gxl.encryptdog.core.operation.impl.encrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.EncryptException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256算法加密,安全性高,速度快
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/8/30 15:50
 */
public class AesEncrypt extends AbstractEncrypt {
    /**
     * 加密算法名称
     */
    private static final String ALGORITHM_TYPE   = EncryptTypeEnum.AES.getAlgorithmType();

    /**
     * 加密算法名称/分组加密/分组填充
     * 即使在ECB模式下，AES-256的密钥长度确保了非常高的加密强度，暴力破解几乎是不可能的
     * 使用向量IV需要切换到CBC、GCM模式
     */
    private static final String CIPHER_ALGORITHM = String.format("%s/ECB/PKCS5Padding", ALGORITHM_TYPE);

    /**
     * 基于PBKDF2算法使用密码派生函数
     */
    private static final String KEY_DERIVATION   = "PBKDF2WithHmacSHA256";

    public AesEncrypt(ObServerContext obServer) {
        super(obServer);
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
            // 获取秘钥key
            var key = getGenerateKey(secretKey);
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

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
     * 返回秘钥器
     * @param secretKey
     * @return
     * @throws EncryptException
     */
    private SecretKeySpec getGenerateKey(char[] secretKey) throws EncryptException {
        try {
            // 生成随机盐值
            var salt = Utils.chars2Bytes(secretKey);
            // 迭代次数，越大越安全
            var iterationCount = 65536;
            // 秘钥长度
            var keyLength = 256;

            // 基于PBKDF2算法使用密码派生函数
            var factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
            var spec = new PBEKeySpec(secretKey, salt, iterationCount, keyLength);
            // 返回秘钥key
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }
}