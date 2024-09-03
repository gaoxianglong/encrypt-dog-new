package com.gxl.encryptdog.base.error;

import java.io.Serial;

/**
 * 加密算法异常,这个异常主要用于使用不存在的算法
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/2 20:20
 */
public class EncryptAlgorithmException extends EncryptException {
    @Serial
    private static final long serialVersionUID = 705106819979193456L;

    public EncryptAlgorithmException(String sourceFile, String message) {
        super(sourceFile, message);
    }

    public EncryptAlgorithmException(String message) {
        super(message);
    }

    public EncryptAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptAlgorithmException(String sourceFile, String message, Throwable cause) {
        super(sourceFile, message, cause);
    }

    public EncryptAlgorithmException(Throwable cause) {
        super(cause);
    }

    public EncryptAlgorithmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
