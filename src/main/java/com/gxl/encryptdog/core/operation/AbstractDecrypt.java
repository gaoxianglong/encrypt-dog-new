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

import com.google.common.base.Charsets;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.event.EstimatedTimeEvent;
import com.gxl.encryptdog.core.event.ProgressEvent;
import com.gxl.encryptdog.core.event.ResultEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.shell.command.HardwareCommand;
import com.gxl.encryptdog.core.shell.command.impl.HardwareCommandImpl;
import com.gxl.encryptdog.utils.Utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
     * UUID长度1bytes
     */
    private static final int UUID_BYTES           = 1;
    /**
     * 加密类型长度1bytes
     */
    private static final int ENCRYPT_LENGTH_BYTES = 1;
    /**
     * 文件唯一id长度为64bit
     */
    private static final int FILE_ID_BYTES        = 8;
    protected Long           fileId;
    /**
     * 获取设备唯一标识命令接口
     */
    private HardwareCommand  command              = new HardwareCommandImpl();

    public AbstractDecrypt(ObServerContext obServer) {
        super(obServer);
    }

    @Override
    public void checkMagicNumber(EncryptContext encryptContext) throws MagicNumberException {
        var in = encryptContext.getInputStream();
        var magicNumber = new byte[MAGIC_NUMBER_BYTES];
        try {
            in.read(magicNumber);
        } catch (Throwable e) {
            throw new MagicNumberException(e.getMessage(), e);
        }
        // 魔术判断
        if (Utils._4bytes2Int(magicNumber) != MAGIC_NUMBER) {
            throw new MagicNumberException("Bad magic number");
        }
    }

    /**
     * 加密类型验证
     * @param encryptContext
     * @throws ValidateException
     * @throws EncryptAlgorithmException
     */
    @Override
    public void checkEncryptType(EncryptContext encryptContext) throws ValidateException, EncryptAlgorithmException {
        // 获取文件句柄
        var in = encryptContext.getInputStream();
        var encryptType = new byte[ENCRYPT_LENGTH_BYTES];
        try {
            in.read(encryptType);
        } catch (Throwable e) {
            throw new EncryptAlgorithmException(e.getMessage(), e);
        }
        // 验证加密算法跟文件头的加密算法是否一致
        if (Utils._1byte2Int(encryptType) != encryptContext.getOperationVO().getEncryptAlgorithm().getId()) {
            throw new EncryptAlgorithmException("The algorithm used is not the encryption algorithm specified during encryption.");
        }
    }

    @Override
    public void bind(EncryptContext encryptContext) throws EncryptException {
        var in = encryptContext.getInputStream();
        var hardwareLength = new byte[UUID_BYTES];
        try {
            // 读取hardwareUUID长度
            in.read(hardwareLength);
            var length = Utils._1byte2Int(hardwareLength);
            // 如果没有开启--onlyLocal命令则hardwareLength=0
            if (0 == length) {
                return;
            }
            var hardware = new byte[length];
            // 读取hardwareUUID
            in.read(hardware);
            var uuid = new String(Utils.toBase64Decode(hardware), Charsets.UTF_8);
            if (!command.getHardwareId().equals(uuid)) {
                throw new OperationException("The UUID does not match,Please decrypt on the same physical device");
            }
            var fileId = new byte[FILE_ID_BYTES];
            // 读取fileUUID
            in.read(fileId);
            this.fileId = Utils.bytes2Long(fileId);

            var operationVO = encryptContext.getOperationVO();
            // 还原真实秘钥
            restoreKey(Utils.bytes2Long(fileId), operationVO);
        } catch (Throwable e) {
            throw new EncryptException(e.getMessage(), e);
        }
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
                // 获取解密后的数据
                var decryptData = dataDecrypt(content, encryptContext.getOperationVO().getSecretKey());
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
            var sk = dataDecrypt(rsk.getBytes(Charsets.UTF_8), operationVO.getSecretKey());
            operationVO.setSecretKey(new String(sk, Charsets.UTF_8).toCharArray());
        } catch (Throwable e) {
            throw new OperationException(e.getMessage(), e);
        }
    }

    /**
     * 数据解密操作
     * @param content
     * @param secretKey
     * @return
     * @throws DecryptException
     */
    protected abstract byte[] dataDecrypt(byte[] content, char[] secretKey) throws DecryptException;

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
