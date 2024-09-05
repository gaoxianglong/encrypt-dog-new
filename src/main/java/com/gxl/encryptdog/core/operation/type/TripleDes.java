package com.gxl.encryptdog.core.operation.type;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * TripleDes算法加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/4 15:39
 */
public interface TripleDes {
    /**
     * 加密算法名称
     */
    String ALGORITHM_TYPE          = EncryptTypeEnum.TRIPLE_DES.getAlgorithmType();

    /**
     * 加密算法名称/分组加密/分组填充
     */
    String CIPHER_ALGORITHM        = String.format("%s/CBC/PKCS5Padding", ALGORITHM_TYPE);

    /**
     * SecureRandom使用SHA1PRNG加密算法
     */
    String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    /**
     * 向量长度
     */
    int    IV_LENGTH               = 8;

    /**
     * 返回秘钥器
     * @param secretKey
     * @return
     * @throws OperationException
     */
    default SecretKeySpec getGenerateKey(char[] secretKey) throws OperationException {
        try {
            var secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
            secureRandom.setSeed(Utils.chars2Bytes(secretKey));
            var kg = KeyGenerator.getInstance(ALGORITHM_TYPE);
            kg.init(secureRandom);
            var generateKey = kg.generateKey();

            // 当秘钥不足192bit时会自动补全,超出则截取前192bit数据
            return new SecretKeySpec(generateKey.getEncoded(), ALGORITHM_TYPE);
        } catch (Throwable e) {
            throw new OperationException(e);
        }
    }
}
