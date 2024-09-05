package com.gxl.encryptdog.core.operation.type;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256算法加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/4 15:24
 */
public interface Aes {
    /**
     * 加密算法名称
     */
    String ALGORITHM_TYPE   = EncryptTypeEnum.AES.getAlgorithmType();

    /**
     * 加密算法名称/分组加密/分组填充
     */
    String CIPHER_ALGORITHM = String.format("%s/CBC/PKCS5Padding", ALGORITHM_TYPE);

    /**
     * 基于PBKDF2算法使用密码派生函数
     */
    String KEY_DERIVATION   = "PBKDF2WithHmacSHA256";
    /**
     * 向量长度
     */
    int    IV_LENGTH        = 16;
    /**
     * 密钥长度256bit
     */
    int    KEY_LENGTH       = 256;
    /**
     * PBKDF2的迭代次数
     */
    int    ITERATION_COUNT  = 65536;

    /**
     * 返回秘钥器
     * @param secretKey
     * @return
     * @throws OperationException
     */
    default SecretKeySpec getGenerateKey(char[] secretKey) throws OperationException {
        try {
            // 根据密码生成盐值
            var salt = Utils.chars2Bytes(secretKey);

            // 基于PBKDF2算法使用密码派生函数
            var factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
            var spec = new PBEKeySpec(secretKey, salt, ITERATION_COUNT, KEY_LENGTH);
            // 返回秘钥key
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
        } catch (Throwable e) {
            throw new OperationException(e);
        }
    }
}
