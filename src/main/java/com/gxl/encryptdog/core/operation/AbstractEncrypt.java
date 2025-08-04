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

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Charsets;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.event.EstimatedTimeEvent;
import com.gxl.encryptdog.core.event.ProgressEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.shell.command.HardwareCommand;
import com.gxl.encryptdog.core.shell.command.impl.HardwareCommandImpl;
import com.gxl.encryptdog.utils.Utils;
import com.gxl.encryptdog.utils.uuid.IdWorker;
import com.gxl.encryptdog.utils.uuid.impl.SnowflakeIdWorker;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.SecureRandom;
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
     * 加密操作往文件头写入魔术码
     *
     * @param encryptContext
     * @throws MagicNumberParseException
     */
    @Override
    public void parseMagicNumber(EncryptContext encryptContext) throws MagicNumberParseException {
        // 获取文件句柄
        var out = encryptContext.getOutputStream();
        // 将magic转为4bytes
        var magicNumber = Utils.int2Bytes(MAGIC_NUMBER);
        try {
            // 文件起始位写入u4/32bit魔术码
            out.write(magicNumber, 0, magicNumber.length);
            out.flush();
        } catch (Throwable e) {
            throw new MagicNumberParseException("Magic write failed", e);
        }
    }

    /**
     * 将加密算法类型、IV向量、Salt等信息记录到FileHeader中
     *
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    @Override
    public void parseHeader(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        encryptContext.setFileHeader(new FileHeader());

        // 写入加密算法类型
        parseEncryptType(encryptContext);

        // 写入IV向量
        parseVector(encryptContext);

        // 写入16bytes随机盐值
        parseSalt(encryptContext);
    }

    /**
     * 生成16bytes随机盐值
     * @param encryptContext
     * @throws HeaderParseException
     */
    @Override
    public void parseSalt(EncryptContext encryptContext) throws HeaderParseException {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SecureRandom random = new SecureRandom();
        // 生成随机盐值
        random.nextBytes(salt);

        // 将salt记录到领域模型中 
        encryptContext.getOperationVO().setSalt(salt);

        // 将salt进行Base64编码后设置到文件头中,以便于后续写入到加密文件头中
        encryptContext.getFileHeader().setSalt(Utils.toBase64Encode(salt));
    }

    /**
     * 文件头中指定其加密类型
     *
     * @param context
     * @throws HeaderParseException
     */
    @Override
    public void parseEncryptType(EncryptContext context) throws HeaderParseException {
        // 获取加密算法类型
        var encryptAlgorithm = context.getOperationVO().getEncryptAlgorithm();
        if (Objects.isNull(encryptAlgorithm)) {
            throw new HeaderParseException("You cannot use a non-existent encryption algorithm.");
        }
        context.getFileHeader().setEncryptTypeId(encryptAlgorithm.getId());
    }

    /**
     * 由具体的加密算法生成IV向量
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {
        var iv = encryptContext.getOperationVO().getIv();
        try {
            // 将IV向量进行Base64编码后写入FileHeader
            encryptContext.getFileHeader().setIv(Utils.toBase64Encode(iv));
        } catch (Throwable e) {
            throw new OperationException("IV writing failed.", e);
        }
    }

    /**
     * 是否仅限在相同的物理设备上完成加/解密操作
     *
     * @param context
     * @throws OperationException
     */
    @Override
    public void bind(EncryptContext context) throws OperationException {
        if (!context.getOperationVO().isOnlyLocal()) {
            return;
        }
        try {
            // 获取物理设备id
            var hardwareId = getHardwareId();
            var headerFile = context.getFileHeader();
            headerFile.setOnlyLocal(true);
            headerFile.setHardwareId(hardwareId);
            headerFile.setFileId(Utils.long2Bytes(context.setFileId(idWorker.getId()).getFileId()));
        } catch (Throwable e) {
            throw new OperationException("Failed to bind the device", e);
        }
    }

    /**
     * 保存文件头
     * 
     * @param encryptContext
     * @throws HeaderParseException
     */
    @Override
    public void saveFileHeader(EncryptContext encryptContext) throws HeaderParseException {
        // 获取文件句柄
        var out = encryptContext.getOutputStream();
        try {
            // 将文件头对象进行序列化
            var header = Utils.str2Bytes(JSON.toJSONString(encryptContext.getFileHeader()));
            var headerSize = Utils.int2Bytes(header.length);

            // 写入文件头大小
            out.write(headerSize);

            // 写入文件头信息
            out.write(header);
            out.flush();
        } catch (Throwable e) {
            throw new HeaderParseException("File header write failed", e);
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

                OperationVO operationVO = encryptContext.getOperationVO();
                // 获取加密后的数据
                var encryptData = dataEncrypt(content, operationVO.getSecretKey(), operationVO.getIv(), operationVO.getSalt());
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
     * @param salt
     * @return
     * @throws EncryptException
     */
    protected abstract byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt) throws EncryptException;

    /**
     * 数据加密操作
     * @param content
     * @param secretKey
     * @param iv
     * @param salt
     * @return
     * @throws EncryptException
     */
    protected byte[] dataEncrypt(byte[] content, char[] secretKey, byte[] iv, byte[] salt, String cipherAlgorithm) throws EncryptException {
        try {
            // 获取PBKDF2增强的秘钥
            var key = getGenerateKey(secretKey, salt);
            var cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            // 执行数据加密
            return cipher.doFinal(content);
        } catch (Throwable e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 生成随机IV向量,不同的算法IV向量长度存在差异
     * 
     * @param ivLength
     * @param encryptContext
     * @throws OperationException
     */
    protected void createVector(int ivLength, EncryptContext encryptContext) throws OperationException {
        try {
            // 生成随机IV
            var iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);

            // 向加/解密领域模型中添加IV
            encryptContext.getOperationVO().setIv(iv);
        } catch (Throwable e) {
            throw new OperationException("IV generation failed.", e);
        }
    }

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
            secretKey = new String(
                dataEncrypt(secretKey.getBytes(Charsets.UTF_8), sourceSecretKey, encryptContext.getOperationVO().getIv(), encryptContext.getOperationVO().getSalt()),
                Charsets.UTF_8);
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
