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
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.event.EstimatedTimeEvent;
import com.gxl.encryptdog.core.event.ProgressEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.shell.command.HardwareCommand;
import com.gxl.encryptdog.core.shell.command.impl.HardwareCommandImpl;
import com.gxl.encryptdog.utils.Utils;
import com.gxl.encryptdog.utils.uuid.IdWorker;
import com.gxl.encryptdog.utils.uuid.impl.SnowflakeIdWorker;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * 数据加密超类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:13
 */
public abstract class AbstractEncrypt extends AbstractOperationTemplate {
    /**
     * 获取设备唯一标识命令
     */
    private HardwareCommand    hardwareCommand              = new HardwareCommandImpl();
    public static final long   IDC_ID                       = (long) (Math.random() * (~(-1L << 5L)));
    public static final long   WORKER_ID                    = (long) (Math.random() * (~(-1L << 5L)));
    /**
     * 雪花id
     */
    private IdWorker<Long>     idWorker                     = new SnowflakeIdWorker(IDC_ID, WORKER_ID);

    /**
     * 加密时缺省每次写入10MB,和读取不同,写入量是一致的
     */
    protected static final int DEFAULT_ENCRYPT_CONTENT_SIZE = 0xa00000;

    public AbstractEncrypt(ObServerContext obServer) {
        super(obServer);
    }

    /**
     * 魔术检查
     *
     * @param encryptContext
     * @throws MagicNumberException
     */
    @Override
    public void checkMagicNumber(EncryptContext encryptContext) throws MagicNumberException {
        var out = encryptContext.getOutputStream();
        var magicNumber = Utils.int2Bytes(MAGIC_NUMBER);
        try {
            // 文件起始位写入u4/32bit魔术码
            out.write(magicNumber, 0, magicNumber.length);
            out.flush();
        } catch (Throwable e) {
            throw new MagicNumberException("Magic write failed", e);
        }
    }

    /**
     * 加密类型验证,这里主要是在魔术后追加u1/8bit的加密类型
     * @param encryptContext
     * @throws ValidateException
     */
    @Override
    public void checkEncryptType(EncryptContext encryptContext) throws ValidateException, EncryptAlgorithmException {
        // 获得文件句柄
        var out = encryptContext.getOutputStream();

        // 获取加密算法类型
        var encryptAlgorithm = encryptContext.getOperationVO().getEncryptAlgorithm();
        if (Objects.isNull(encryptAlgorithm)) {
            throw new ValidateException("You cannot use a non-existent encryption algorithm.");
        }
        try {
            // 写入u1/8bit的加密类型
            out.write(Utils.int2Byte(encryptAlgorithm.getId()));
            out.flush();
        } catch (Throwable e) {
            throw new EncryptAlgorithmException("Encrypt algorithm write failed", e);
        }
    }

    /**
     * 由具体的加密算法生成IV向量,这里在加密验证后追加u1/8bit向量长度和具体的向量值
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void initVector(EncryptContext encryptContext) throws OperationException {
        // 获取文件句柄
        var out = encryptContext.getOutputStream();
        try {
            // 获取IV
            var iv = encryptContext.getOperationVO().getIv();
            // 获取IV长度
            var ivLength = iv.length;

            // 写入u1/8bit的IV向量长度
            out.write(Utils.int2Byte(ivLength));
            // 写入具体向量值
            out.write(iv, 0, ivLength);
            out.flush();
        } catch (Throwable e) {
            throw new EncryptException("IV write failed", e);
        }
    }

    /**
     * 是否仅限在相同的物理设备上完成加/解密操作
     *
     * @param encryptContext
     * @throws EncryptException
     */
    @Override
    public void bind(EncryptContext encryptContext) throws EncryptException {
        var out = encryptContext.getOutputStream();
        try {
            if (encryptContext.getOperationVO().isOnlyLocal()) {
                // 获取物理设备id
                var hardwareId = getHardwareId();
                // 写入u1/8bit的hardwareId长度
                out.write(Utils.int2Byte(hardwareId.length));
                // 写入物理设备id
                out.write(hardwareId, 0, hardwareId.length);
                // 写入文件唯一id
                out.write(Utils.long2Bytes(encryptContext.setFileId(idWorker.getId()).getFileId()));
            } else {
                // 未开启OnlyLocal时写入一个空字节
                out.write(new byte[1]);
            }
            out.flush();
        } catch (Throwable e) {
            throw new EncryptException("Device binding failed", e);
        }
    }

    /**
     * 转储加/解密数据
     * @param encryptContext
     * @throws EncryptException
     */
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
                // 获取加密后的数据
                var encryptData = dataEncrypt(content, encryptContext.getOperationVO().getSecretKey(), encryptContext.getOperationVO().getIv());
                out.write(encryptData, 0, encryptData.length);
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
            throw new EncryptException(String.format("Target file encryption failed"), e);
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

        // 固化--only-local场景下的真实秘钥
        saveSecretKey(encryptContext);
    }

    /**
     * 获取分段操作容量
     *
     * @param capacity
     * @return
     */
    @Override
    public int getDefaultCapacity(long capacity) {
        return capacity <= DEFAULT_ENCRYPT_CONTENT_SIZE ? (int) capacity : DEFAULT_ENCRYPT_CONTENT_SIZE;
    }

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @param iv
     * @return
     * @throws EncryptException
     */
    protected abstract byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv) throws EncryptException;

    /**
     * 存储--only-local场景下的真实秘钥
     * @param encryptContext
     * @throws ResourceException
     */
    private synchronized void saveSecretKey(EncryptContext encryptContext) throws ResourceException {
        if (!encryptContext.getOperationVO().isOnlyLocal()) {
            return;
        }
        var properties = new Properties();
        try (var in = new BufferedInputStream(new FileInputStream(SECRET_KEY_FILE))) {
            properties.load(in);
            // 以fileId为key
            var key = String.valueOf(encryptContext.getFileId());
            // 源秘钥
            var sourceSecretKey = encryptContext.getOperationVO().getSourceSecretKey();
            // 真实秘钥
            var secretKey = new String(encryptContext.getOperationVO().getSecretKey());
            // 使用源秘钥加密真实秘钥
            secretKey = new String(dataEncrypt(secretKey.getBytes(Charsets.UTF_8), sourceSecretKey, encryptContext.getOperationVO().getIv()), Charsets.UTF_8);
            // 固化加密后的真实秘钥
            properties.put(key, secretKey);
            properties.store(new BufferedOutputStream(new FileOutputStream(SECRET_KEY_FILE)), null);
        } catch (Throwable e) {
            throw new ResourceException(e);
        }
    }

    /**
     * 获取物理设备UUID
     * @return
     * @throws CommandException
     * @throws UnsupportedEncodingException
     */
    private byte[] getHardwareId() throws CommandException, UnsupportedEncodingException {
        return Utils.toBase64Encode(hardwareCommand.getHardwareId().getBytes(Charsets.UTF_8)).getBytes(Charsets.UTF_8);
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
