package com.gxl.encryptdog.core.operation.type;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;

/**
 * XOR混淆加密
 * 在-d模式下,如果秘钥错误也能够正常进行异或运算,但是会损坏源文件,慎用
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/4 15:39
 */
public interface Xor {
    /**
     * 加密算法名称
     */
    String ALGORITHM_TYPE = EncryptTypeEnum.XOR.getAlgorithmType();
}
