/*
 *
 *  * Copyright 2019-2119 gao_xianglong@sina.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.gxl.encryptdog.core.operation;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Charsets;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.event.EstimatedTimeEvent;
import com.gxl.encryptdog.core.event.ProgressEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.shell.command.HardwareCommand;
import com.gxl.encryptdog.core.shell.command.impl.HardwareCommandImpl;
import com.gxl.encryptdog.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * 数据解密超类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:12
 */
public abstract class AbstractDecrypt extends AbstractOperationTemplate {
    /**
     * 获取设备唯一标识命令接口
     */
    private HardwareCommand command = new HardwareCommandImpl();

    public AbstractDecrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 解密操作进行魔术检查
     * @param encryptContext
     * @throws MagicNumberParseException
     */
    @Override
    public void parseMagicNumber(EncryptContext encryptContext) throws MagicNumberParseException {
        // 获取文件句柄
        var in = encryptContext.getInputStream();
        var magicNumber = new byte[MAGIC_NUMBER_BYTES];
        try {
            in.read(magicNumber);
        } catch (IOException e) {
            throw new MagicNumberParseException("Magic read failed.", e);
        }
        // 魔术判断
        if (Utils._4bytes2Int(magicNumber) != MAGIC_NUMBER) {
            throw new MagicNumberParseException("Bad magic number.");
        }
    }

    /**
     * 解析文件头中的加密算法、IV向量、Salt、Version等信息进行验证
     * 
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseHeader(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        // 获取文件句柄
        var in = encryptContext.getInputStream();
        var headerSize = new byte[HEADER_LENGTH_BYTES];
        try {
            // 解析文件头长度
            in.read(headerSize);
            var header = new byte[Utils._4bytes2Int(headerSize)];

            // 解析文件头
            in.read(header);

            // 将文件头反序列化
            FileHeader fileHeader = JSONObject.parseObject(Utils.bytes2Str(header), FileHeader.class);

            // 将FileHeader设置到上下文中
            encryptContext.setFileHeader(fileHeader);

            // 1.验证加密算法跟文件头的加密算法是否一致
            parseEncryptType(encryptContext);

            // 2.获取IV向量
            parseVector(encryptContext);

            // 3.获取Salt
            parseSalt(encryptContext);
        } catch (Throwable e) {
            throw new HeaderParseException("Failed to parse file header.", e);
        }
    }

    /**
     * 初始化盐值
     * @param encryptContext
     * @throws HeaderParseException
     */
    @Override
    public void parseSalt(EncryptContext encryptContext) throws HeaderParseException {
        var fileHeader = encryptContext.getFileHeader();
        var salt = fileHeader.getSalt();
        if (Objects.isNull(salt) || salt.isBlank()) {
            throw new HeaderParseException("File header integrity compromised; Salt is missing.");
        }
        // 将salt进行Base64解码后写入到领域模型中
        encryptContext.getOperationVO().setSalt(Utils.toBase64Decode(salt));
    }

    /**
     * 验证加密算法跟文件头的加密算法是否一致
     * 
     * @param encryptContext
     * @throws HeaderParseException
     */
    @Override
    public void parseEncryptType(EncryptContext encryptContext) throws HeaderParseException {
        // 获取加解密领域模型
        var operationVO = encryptContext.getOperationVO();
        // 获取文件头
        var fileHeader = encryptContext.getFileHeader();

        // 验证加密算法跟文件头的加密算法是否一致
        if (operationVO.getEncryptAlgorithm().getId() != fileHeader.getEncryptTypeId()) {
            throw new HeaderParseException("The encryption and decryption algorithms are inconsistent.");
        }
    }

    /**
     * 读取向量IV
     * 
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        var fileHeader = encryptContext.getFileHeader();
        var iv = fileHeader.getIv();
        if (Objects.isNull(iv) || iv.isBlank()) {
            throw new HeaderParseException("File header integrity compromised; IV (Initialization Vector) is missing.");
        }

        // 将iv进行Base64解码后写入到领域模型中
        encryptContext.getOperationVO().setIv(Utils.toBase64Decode(iv));
    }

    @Override
    public void bind(EncryptContext encryptContext) throws OperationException {
        var fileHeader = encryptContext.getFileHeader();
        var operationVO = encryptContext.getOperationVO();
        if (!fileHeader.isOnlyLocal()) {
            return;
        }

        // 获取物理设备UUID
        var hardware = fileHeader.getHardwareId();
        if (Objects.isNull(hardware)) {
            throw new OperationException("File header integrity compromised; Hardware ID is missing.");
        }
        var uuid = new String(Utils.toBase64Decode(hardware), Charsets.UTF_8);
        if (!command.getHardwareId().equals(uuid)) {
            throw new OperationException("The UUID does not match,Please decrypt on the same physical device");
        }

        // 获取文件唯一ID
        var fileId = fileHeader.getFileId();
        if (Objects.isNull(fileId)) {
            throw new OperationException("File header integrity compromised; File ID is missing.");
        }

        // 还原真实秘钥
        restoreKey(Utils.bytes2Long(fileId), operationVO);
    }

    @Override
    public void store(EncryptContext encryptContext) throws EncryptException {
        // 获取每次加密读取的数据块容量大小
        var defaultCapacity = encryptContext.getDefaultCapacity();
        var content = new byte[defaultCapacity];

        // 获取读/写文件句柄
        var in = encryptContext.getInputStream();
        var out = encryptContext.getOutputStream();

        // 每次读取的字节数
        var len = -1;
        // 当前已处理的容量
        var currentCapacity = 0L;
        var isFirst = true;
        // 任务的开始时间
        var begin = System.currentTimeMillis();
        try {
            while ((len = in.read(content)) != -1) {
                // 当前容量计算
                currentCapacity += len;
                if (len < defaultCapacity) {
                    var temp = new byte[len];
                    System.arraycopy(content, 0, temp, 0, len);
                    content = temp;
                }

                // 获取加解密领域模型
                OperationVO operationVO = encryptContext.getOperationVO();

                // 获取解密后的数据
                var decryptData = dataDecrypt(content, operationVO.getSecretKey(), operationVO.getIv(), operationVO.getSalt());
                out.write(decryptData, 0, decryptData.length);
                out.flush();

                // 计算预计耗时
                if (isFirst) {
                    // 数据块的操作执行耗时
                    var time = calculationEstimatedTime(System.currentTimeMillis() - begin, encryptContext);
                    // 下发EstimatedTimeEvent事件
                    getObServer().fireEvent(buildEstimatedTimeEvent(time, encryptContext));
                    isFirst = false;
                }
                // 下发ProgressEvent事件
                getObServer().fireEvent(buildProgressEvent(currentCapacity, encryptContext));
            }
        } catch (Throwable e) {
            throw new EncryptException(String.format("Target file decryption failed"), e);
        }
    }

    /**
     * 成功处理
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void onSuccess(EncryptContext encryptContext) throws OperationException {
        // 获取源文件容量大小
        var sourceFileCapacity = encryptContext.getSourceFileCapacity();
        // 下发ProgressEvent事件,任务结束后将任务进度追加到100%
        getObServer().fireEvent(buildProgressEvent(sourceFileCapacity, encryptContext));

        super.onSuccess(encryptContext);
    }

    @Override
    public void onFailure(EncryptContext encryptContext) throws OperationException {
        super.onFailure(encryptContext);
    }

    /**
     * 还原真实秘钥
     * @param fileId
     * @param operationVO
     * @throws OperationException
     */
    public void restoreKey(long fileId, OperationVO operationVO) throws OperationException {
        try (var in = new BufferedInputStream(new FileInputStream(SECRET_KEY_FILE))) {
            var properties = new Properties();
            properties.load(in);
            var key = String.valueOf(fileId);
            // 获取随机秘钥
            var rsk = properties.getProperty(key);
            if (Objects.isNull(rsk)) {
                return;
            }
            // 使用原秘钥解密对应的随机秘钥
            var sk = dataDecrypt(rsk.getBytes(Charsets.UTF_8), operationVO.getSecretKey(), operationVO.getIv(), operationVO.getSalt());
            operationVO.setSecretKey(new String(sk, Charsets.UTF_8).toCharArray());
        } catch (Throwable e) {
            throw new OperationException(e.getMessage(), e);
        }
    }

    /**
     * 数据解密操作
     * @param content   数据
     * @param secretKey 秘钥
     * @param iv        IV向量
     * @param salt
     * @return
     * @throws DecryptException
     */
    protected abstract byte[] dataDecrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws DecryptException;

    /**
     * 数据解密操作
     * @param content
     * @param secretKey
     * @param iv
     * @param salt
     * @param cipherAlgorithm
     * @return
     * @throws DecryptException
     */
    protected byte[] dataDecrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt, String cipherAlgorithm) throws DecryptException {
        try {
            // 获取秘钥器
            var key = getGenerateKey(secretKey, salt);
            var cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            // 执行数据解密
            return cipher.doFinal(content);
        } catch (Throwable e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 构建EstimatedTimeEvent
     * @param estimatedTime
     * @param context
     * @return
     */
    private EstimatedTimeEvent buildEstimatedTimeEvent(String estimatedTime, EncryptContext context) {
        var event = new EstimatedTimeEvent(null, System.currentTimeMillis(), context);
        event.setEstimatedTime(estimatedTime);
        return event;
    }

    /**
     * 构建ProgressEvent
     * @param capacity
     * @param context
     * @return
     */
    private ProgressEvent buildProgressEvent(long capacity, EncryptContext context) {
        var event = new ProgressEvent(null, System.currentTimeMillis(), context);
        event.setProgress((int) (((double) capacity / context.getSourceFileCapacity()) * 100) + "%");
        return event;
    }
}
