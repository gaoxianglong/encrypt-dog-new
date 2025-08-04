package com.gxl.encryptdog.base.error;

import java.io.Serial;

/**
 * 头文件解析异常类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/31 15:51
 */
public class HeaderParseException extends ParseException {
    @Serial
    private static final long serialVersionUID = -1535782183181298637L;

    public HeaderParseException(String sourceFile, String message) {
        super(sourceFile, message);
    }

    public HeaderParseException(String message) {
        super(message);
    }

    public HeaderParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeaderParseException(String sourceFile, String message, Throwable cause) {
        super(sourceFile, message, cause);
    }

    public HeaderParseException(Throwable cause) {
        super(cause);
    }

    public HeaderParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
