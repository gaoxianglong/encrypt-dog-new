package com.gxl.encryptdog.core.operation;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件头
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/31 16:13
 */
@Data
public class FileHeader implements Serializable {
    @Serial
    private static final long serialVersionUID = -6178342497475445355L;

    /**
     * 加密算法ID
     */
    private int               encryptTypeId;

    /**
     * Base64编码后的IV向量
     */
    private String            iv;

    /**
     * Base64编码后的盐值
     */
    private String            salt;

    /**
     * 物理设备的UUID
     */
    private byte[]            hardwareId;

    /**
     * 文件ID
     */
    private byte[]            fileId;

    /**
     * 加密文件版本
     */
    private String            version;

    /**
     * 加密操作是否绑定指定物理设备
     */
    private boolean           isOnlyLocal;
}
