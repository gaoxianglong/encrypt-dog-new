package com.gxl.encryptdog.core.operation.impl.encrypt;

import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.EncryptException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.AbstractEncrypt;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.core.operation.type.Xor;
import com.gxl.encryptdog.utils.Utils;

/**
 * XOR加密
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/8/30 15:42
 */
public class XorEncrypt extends AbstractEncrypt implements Xor {
    public XorEncrypt(ObServerContext obServer) {
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
        // 声明加密后的结果集
        var rlt = new byte[content.length];
        // 将密钥转换为字节数组
        var key = Utils.chars2Bytes(secretKey);

        for (var i = 0; i < content.length; i++) {
            // 通过异或运算对源数据进行混淆
            rlt[i] = (byte) (content[i] ^ key[i % key.length]);
        }
        return rlt;
    }

    /**
     * XOR算法不进行IV混淆
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void initVector(EncryptContext encryptContext) throws OperationException {
        // 空重写
    }

    /**
     * 获取加密渠道
     * @return
     */
    @Override
    public ChannelEnum getChannel() {
        return ChannelEnum.XOR_ENCRYPT;
    }
}